package no.feltgis.mobilegolibrary

import android.app.Application
import mobile.Mobile_
import no.feltgis.mobilegolibrary.di.AppModule
import kotlin.properties.Delegates

class MobileGo(application: Application,
               environment: String = "",
               release: String = "",
               mobileGoCallbacks: MobileGoCallbacks? = null) {
    var mobileGo: Mobile_ by Delegates.notNull()
    //var api: Api by Delegates.notNull()

    init {
        val appModule = AppModule(application, environment, release, mobileGoCallbacks)
        val api = appModule.provideApi()
        val shortTimeoutApi = appModule.provideShortTimeoutApi()
        val apiManager = appModule.provideApiManager(api, shortTimeoutApi)
        this.mobileGo = appModule.provideGoMobile(apiManager)
    }

}