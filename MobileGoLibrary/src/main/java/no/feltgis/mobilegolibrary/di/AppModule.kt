package no.feltgis.mobilegolibrary.di

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import mobile.Mobile
import mobile.Mobile_
import no.feltgis.mobilego.app.network.Api
import no.feltgis.mobilego.app.network.ApiManager
import no.feltgis.mobilegolibrary.MobileGoCallbacks
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppModule(
        private val application: Application,
        private val environment: String = "",
        private val release: String = "",
        private val mobileGoCallbacks: MobileGoCallbacks? = null) {

    private val logTag = "MobileGo"
    private fun handleGoLog(it: String){
        if (it.startsWith("DEBUG")) Log.d(logTag, it.removePrefix("DEBUG\t"))
        else if (it.startsWith("WARNING")) Log.w(logTag, it.removePrefix("WARNING\t"))
        else if (it.startsWith("ERROR")) Log.e(logTag, it.removePrefix("ERROR\t"))
        else if (it.startsWith("INFO")) Log.i(logTag, it.removePrefix("INFO\t"))
        else Log.v(logTag, it)
    }

    fun provideGoMobile(apiManager: ApiManager): Mobile_ {

        if (go !== null) {
            return go as Mobile_
        }
        // (folder string, skogdataEnv string, release string, logger IMobileLogger) *MobileResult


        val res = Mobile.initAndStartMobile(application.filesDir.absolutePath, environment, release, /*isAndroid*/true, {
            handleGoLog(it)

            mobileGoCallbacks?.sendLog(it)
        }, {
            apiManager.executeRequest(it, go)
            // create PBCallback from it-bytes

            // PBCallback value is an enum, and the only supported type is currently PBFileUploadStateCallback

            // When the PBFileUploadStateCallback is received, update the job list view
        })
        if (res?.failure?.isNotEmpty() == true) {
            // TODO This is bad - Go was not able to initialize. Not sure what to do here - stuff will not work.
            Log.e(AppModule::class.java.name, res.failure)
        }
        go = res.success
        return go as Mobile_
    }

    companion object {
        var go: Mobile_? = null
    }

    fun provideApiManager(api: Api, shortTimeoutApi: Api) = ApiManager(api, shortTimeoutApi, mobileGoCallbacks)

    fun provideApi(): Api {
        val restAdapterFeltLogg = Retrofit.Builder()
                .client(getClientBuilder().build())
                .baseUrl("https://www.google.com")
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return restAdapterFeltLogg.create(Api::class.java)
    }

    fun provideShortTimeoutApi(): Api {
        val restAdapterFeltLogg = Retrofit.Builder()
                .client(getClientBuilder(timeout = 5).build())
                .baseUrl("https://www.google.com")
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return restAdapterFeltLogg.create(Api::class.java)
    }

    private fun getClientBuilder(timeout: Long = 120): OkHttpClient.Builder {
        val client = OkHttpClient()
                .newBuilder()
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
        //        if (BuildConfig.DEBUG) {
        //            client.addInterceptor(getDebugLoggingInterceptor())
        //        }
        //        client.addInterceptor(getFLLoggingInterceptor(log))
        return client
    }

    private fun getGson(): Gson {
        return GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") //2018-05-17T19:51:58
                .create()
    }
}