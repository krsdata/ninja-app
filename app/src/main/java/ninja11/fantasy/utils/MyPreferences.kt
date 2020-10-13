package ninja11.fantasy.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

/**
 * @author Manoj Prasad Helper class for shared preferences
 */
object MyPreferences {
    val KEY_PREF_VALUE = "testPreferences"
    private val KEY_PREF_LOGIN_STATUS = "isloginstatus"
    private val KEY_PREF_USERINFO = "userinfo"
    private val KEY_WALLET_INFO = "wallet_info"
    private val KEY_DEVICEID = "deviceid"
    private val KEY_PREF_USERID = "userId"
    private val KEY_PREF_UPDATED_APKS = "updatedapks"
    private val KEY_PREF_PROFILE_PIC = "profilepic"
    private val KEY_PREF_EMAIL = "emails"
    private val KEY_PREF_MOBILE = "mobile"
    private val KEY_PREF_NOTIFICATION_TOKEN = "notification_token"
    private val KEY_PREF_APP_TOKEN = "token"
    private val KEY_PREF_APP_WALLET_AMOUNT = "wallet"
    private val KEY_PREF_APP_WALLET_BONUS = "bwallet"

    /**
     * @author Manoj Prasad
     * List of all constants for API's Caching
     */
    val KEY_UPCOMING_MATCHES = "upcoming_matches_list"
    val KEY_TRANSACTION_HISTORY = "transaction_history"

    private fun getPreferences(context: Context?): SharedPreferences? {
        return context?.getSharedPreferences("testpref", Context.MODE_PRIVATE)
    }

    fun putLong(context: Context, key: String, value: Long) {
        getPreferences(context)!!.edit().putLong(key, value).commit()
    }

    fun getLong(context: Context, key: String, value: Long): Long {
        return getPreferences(context)!!.getLong(key, value)
    }

    private fun putString(context: Context, key: String, value: String) {
        val pref = getPreferences(context)
        pref?.edit()?.putString(key, value)?.commit()
    }

    private fun getString(context: Context, key: String, defValue: String): String? {
        val pref = getPreferences(context)
        return if (pref != null) {
            pref.getString(key, defValue)
        } else ""

    }

    fun getInt(context: Context, key: String): Int {
        val pref = getPreferences(context)
        return pref?.getInt(KEY_PREF_VALUE + key, 0) ?: 0
    }

    fun putInt(context: Context, key: String, value: Int) {
        val pref = getPreferences(context)
        if (pref != null) {
            getPreferences(context)!!.edit().putInt(
                KEY_PREF_VALUE + key, value).commit()
        }
    }

    fun getBoolean(context: Context, key: String, def: Boolean): Boolean {
        val pref = getPreferences(context)
        return if (pref != null) {
            getPreferences(context)!!.getBoolean(
                KEY_PREF_VALUE + key, def)
        } else false
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        val pref = getPreferences(context)
        if (pref != null) {
            getPreferences(context)!!.edit().putBoolean(
                KEY_PREF_VALUE + key, value).commit()
        }
    }

    fun getStringPrefrence(context: Context, type: String): String? {
        return getString(
            context,
            KEY_PREF_VALUE + type,
            ""
        )
    }

    fun setStringPrefrence(context: Context, type: String, value: String) {
        putString(
            context,
            KEY_PREF_VALUE + type,
            value
        )
    }

    fun clear(context: Context) {
        val pref = getPreferences(context)
        if (pref != null) {
            getPreferences(context)!!.edit().clear().commit()
        }
    }

    fun setLoginStatus(context: Context, value: Boolean) {
        putBoolean(
            context,
            KEY_PREF_LOGIN_STATUS,
            value
        )
    }

    fun getLoginStatus(context: Context): Boolean? {
        return getBoolean(context, KEY_PREF_LOGIN_STATUS,false)
    }


    fun saveUserInformations(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_USERINFO,
            value
        )
    }

    fun getUserInformations(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_USERINFO
        )
    }


    fun setAndroidID(context: Context, deviceId: String) {
        if (!TextUtils.isEmpty(deviceId)) {
            setStringPrefrence(
                context,
                KEY_DEVICEID,
                deviceId
            )
        }
    }

    fun getAndroidId(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_DEVICEID
        )
    }




    fun setNotificationToken(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_NOTIFICATION_TOKEN,
            value
        )
    }

    fun getNotificationToken(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_NOTIFICATION_TOKEN
        )
    }


    fun setToken(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_APP_TOKEN,
            value
        )
    }

    fun getToken(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_APP_TOKEN
        )
    }


    fun setEmail(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_EMAIL,
            value
        )
    }

    fun getEmail(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_EMAIL
        )
    }

    fun setMobile(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_MOBILE,
            value
        )
    }

    fun getMobile(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_MOBILE
        )
    }


    fun setUserID(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_USERID,
            value
        )
    }

    fun getUserID(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_USERID
        )
    }

//    fun setWalletBalance(context: Context, value: String) {
//        setStringPrefrence(
//            context,
//            KEY_PREF_APP_WALLET_AMOUNT,
//            value
//        )
//    }
//
//    fun getWalletBalance(context: Context): Double? {
//        var value = getStringPrefrence(
//            context,
//            KEY_PREF_APP_WALLET_AMOUNT
//        )
//        if(TextUtils.isEmpty(value)){
//             return 0.0
//        }
//        return value!!.toDouble()
//    }

//    fun setBonusWalletBalance(context: Context, value: String) {
//        setStringPrefrence(
//            context,
//            KEY_PREF_APP_WALLET_BONUS,
//            value
//        )
//    }
//
//    fun getBonusWalletBalance(context: Context): Double? {
//        var value = getStringPrefrence(
//            context,
//            KEY_PREF_APP_WALLET_BONUS
//        )
//        if(TextUtils.isEmpty(value)){
//            return 0.0
//        }
//        return value!!.toDouble()
//    }

    fun setProfilePicture(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_PROFILE_PIC,
            value
        )
    }

    fun getProfilePicture(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_PROFILE_PIC
        )
    }

    fun setUpdatedApkLinks(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_PREF_UPDATED_APKS,
            value
        )
    }

    fun getUpdatedApkLinks(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_PREF_UPDATED_APKS
        )
    }


    fun saveWalletInformation(context: Context, value: String) {
        setStringPrefrence(
            context,
            KEY_WALLET_INFO,
            value
        )
    }

    fun getWalletInformation(context: Context): String? {
        return getStringPrefrence(
            context,
            KEY_WALLET_INFO
        )
    }


    fun setSFApiCaches(context: Context, key:String,value: String) {
        setStringPrefrence(
            context,
            key,
            value
        )
    }

    fun getSFApiCaches(context: Context,key:String): String? {
        return getStringPrefrence(
            context,
            key
        )
    }

}
