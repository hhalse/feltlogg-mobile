package no.feltgis.mobilegolibrary

import common.Db
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

typealias PrepareOfflineStatusListener = (prepareOffline: Db.PBPrepareOffline) -> Unit
typealias SyncCollectionListener = (refreshMap: Db.PBSyncCollectionUpdate) -> Unit

interface MobileGoCallbacks {
    fun sendLog(log: String)
    fun prepareOffline(fileUploadState: Db.PBPrepareOffline)
    fun listenToPrepareOffline(listener: PrepareOfflineStatusListener?)
    fun fileUploadState(fileUploadState: Db.PBFileUploadStateCallback)
    fun collectionChange(collectionChange:Db.PBSyncCollectionUpdate)
    fun setCollectionChangeListener(listener: SyncCollectionListener?)
    fun doHttpRequest(method: String, url: String, headers: Map<String, String>, body: RequestBody): Single<Response<ResponseBody>>
}