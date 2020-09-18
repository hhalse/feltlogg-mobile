package no.feltgis.mobile

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import mobile.Mobile_
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
class MobileGo(application: Application,
               environment: String = "",
               release: String = "", ) {
    var mobileGo: Mobile_ by Delegates.notNull()
    //var api: Api by Delegates.notNull()

    init {
    }

}

