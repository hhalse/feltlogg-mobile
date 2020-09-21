package no.feltgis.mobilego.app.network

import android.util.Log
import com.google.protobuf.ByteString
import common.Db
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import mobile.Mobile_
import no.feltgis.mobilegolibrary.MobileGoCallbacks
import okhttp3.Headers
import okhttp3.RequestBody

class ApiManager(private val api: Api,
                 private val shortTimeoutApi: Api,
                 private val mobileGoCallbacks: MobileGoCallbacks? = null) {

    private val compositeDisposable = CompositeDisposable()

    fun executeRequest(data: ByteArray, go: Mobile_?) {

        try {
            val pbCallback = Db.PBCallback.parseFrom(data)
            when (pbCallback.valueCase) {
                Db.PBCallback.ValueCase.HTTPREQUEST -> handlePBHttpRequest(pbCallback, go)
                Db.PBCallback.ValueCase.FILEUPLOADSTATE -> handlePBFileUploadState(pbCallback, go)
                Db.PBCallback.ValueCase.PREPAREOFFLINE -> handlePBPrepareOffline(pbCallback, go)
                Db.PBCallback.ValueCase.SYNCCOLLECTIONUPDATE -> handleCollectionChanges(pbCallback)
                else -> Log.i("MobileGo", "Unknown PBCallback valueCase")
            }
        } catch (t: Throwable) {
            mobileGoCallbacks?.sendLog("Unable to unmarshal callback: $t")
            Log.w("MobileGo", "Unable to unmarshal callback: $t")
        }
    }

    private fun handlePBPrepareOffline(pbCallback: Db.PBCallback, go: Mobile_?) {
        val prepareOffline = pbCallback.prepareOffline
        mobileGoCallbacks?.prepareOffline(prepareOffline)
    }

    private fun handlePBFileUploadState(pbCallback: Db.PBCallback, go: Mobile_?) {
        val fileUploadState = pbCallback.fileUploadState
        mobileGoCallbacks?.fileUploadState(fileUploadState)
    }

    private fun handleCollectionChanges(pbCallback:Db.PBCallback){
        val collectionChange = pbCallback.syncCollectionUpdate
        mobileGoCallbacks?.collectionChange(collectionChange)
    }

    private fun handlePBHttpRequest(pbCallback: Db.PBCallback, go: Mobile_?) {
        val req = pbCallback.httpRequest
        val body = RequestBody.create(null, req.body.toByteArray())
        val httpRequest = if (req.url.contains("192.168.10.1")) {
            val url = req.url.replace("https", "http")
            if (!url.contains("/ping")) {
                Log.d("MobileGo", "Calling raspberrypi for " + req.method + " " + url + " ")
            }
            mobileGoCallbacks?.doHttpRequest(req.method, url, buildHeaderMap(req.headersList), body)
        } else {
            Log.d("MobileGo", "Calling internet for " + req.method + " " + req.url + " ")
            val defaultApi = if (req.timeout <= 5) shortTimeoutApi else api
            val method = when (req.method) {
                "POST" -> defaultApi.post(req.url, body, buildHeaderMap(req.headersList))
                "PUT" -> defaultApi.put(req.url, body, buildHeaderMap(req.headersList))
                "DELETE" -> defaultApi.delete(req.url, buildHeaderMap(req.headersList))
                "PATCH" -> defaultApi.patch(req.url, body, buildHeaderMap(req.headersList))
                "GET" -> defaultApi.get(req.url, buildHeaderMap(req.headersList))
                else -> throw Exception("Db.PBCallback unknown API method")
            }
            method
        }

        httpRequest?.let { method ->
            compositeDisposable.add(method.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        val bytes = response.body()
                                ?.bytes()
                        val resp = Db.PBHttpResponse.newBuilder()
                                .setRequestId(req.requestId)
                                .setStatusCode(response.code())
                                .addAllHeaders(buildHeaders(response.headers()))
                        if (bytes != null) {
                            resp.body = ByteString.copyFrom(bytes)
                        }
                        go?.handlePBHttpResponse(resp.build()
                                .toByteString()
                                .toByteArray())
                    }, {
                        Log.d("MobileGo", "Error for " + req.method + " " + req.url + " $it")
                        mobileGoCallbacks?.sendLog(it.message ?: "")
                    }))
        }
    }


    private fun buildHeaders(headers: Headers): List<Db.PBPair> {
        return (0 until headers.size()).map { index -> buildHeaderPair(headers.name(index), headers.value(index)) }
    }

    private fun buildHeaderPair(key: String, value: String): Db.PBPair {
        return Db.PBPair.newBuilder()
                .setKey(key)
                .setValue(value)
                .build()
    }

    private fun buildHeaderMap(headers: List<Db.PBPair>) = headers.associateBy({ it.key }, { it.value })

}