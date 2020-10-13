package ninja11.fantasy.utils

import android.os.Build
import ninja11.fantasy.BuildConfig
import java.io.Serializable

class HardwareInfo : Serializable {
    private var os: String? = BuildConfig.VERSION_NAME
    var brand: String? = null
    var model: String? = Build.MODEL
    var os_sdk_int: Int? = Build.VERSION.SDK_INT
    var os_version: String? = Build.VERSION.RELEASE
    var device_id: String? = null
    var manufacturer: String? = Build.MANUFACTURER
    var appVersion: String? = Build.VERSION.RELEASE
    /*
     * public void setCountry(String country) { this.country = country; }
	 */

    var timeZone: String? = null
    var country: String? = null
    var noOfSIM: Int = 0
    var adId: String? = null



    fun setOs(os: String) {
        val version = String.format("(%s)", BuildConfig.VERSION_NAME)
        this.os = os + version
    }


    companion object {
        private const val serialVersionUID = 1L
    }
}
