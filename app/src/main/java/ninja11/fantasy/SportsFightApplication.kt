package ninja11.fantasy

import android.content.Context
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ninja11.fantasy.R
import ninja11.fantasy.models.MatchesModels
import ninja11.fantasy.models.TransactionModel
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.models.WalletInfo
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyPreferences.KEY_TRANSACTION_HISTORY
import ninja11.fantasy.utils.MyPreferences.KEY_UPCOMING_MATCHES


class SportsFightApplication : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs)
        val config = FontRequestEmojiCompatConfig(this, fontRequest)
            .setReplaceAll(true)
            .setEmojiSpanIndicatorEnabled(true)

        EmojiCompat.init(config)

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)


    }

    fun saveUserInformations(value: UserInfo?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.saveUserInformations(applicationContext, data)
        }
    }

    val userInformations: UserInfo
        get() {
            var mStoreListModels: UserInfo? = null
            val type = object : TypeToken<UserInfo>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getUserInformations(this)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<UserInfo>(gsonObject, type)
            }
            return mStoreListModels ?: UserInfo()
        }



    fun saveWalletInformation(value: WalletInfo?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.saveWalletInformation(applicationContext, data)
        }
    }

    val walletInfo: WalletInfo
        get() {
            var mStoreListModels: WalletInfo? = null
            val type = object : TypeToken<WalletInfo>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getWalletInformation(this)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<WalletInfo>(gsonObject, type)
            }
            return mStoreListModels ?: WalletInfo()
        }

    /**
     * @author Manoj Prasad
     * Saving all upcoming matches in cache
     */
    fun saveUpcomingMatches(value: ArrayList<MatchesModels>?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.setSFApiCaches(applicationContext, KEY_UPCOMING_MATCHES,data)
        }
    }

    val getUpcomingMatches: ArrayList<MatchesModels>
        get() {
            var mStoreListModels: ArrayList<MatchesModels>? = null
            val type = object : TypeToken<ArrayList<MatchesModels>>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getSFApiCaches(this,KEY_UPCOMING_MATCHES)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<ArrayList<MatchesModels>>(gsonObject, type)
            }
            return mStoreListModels ?: ArrayList()
        }


    /**
     * @author Manoj Prasad
     * Saving transactionlist in cache
     */
    fun saveTransactionHistory(value: ArrayList<TransactionModel>?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.setSFApiCaches(applicationContext, data,KEY_TRANSACTION_HISTORY)
        }
    }

    val getTransactionHistory: ArrayList<TransactionModel>
        get() {
            var mStoreListModels: ArrayList<TransactionModel>? = null
            val type = object : TypeToken<ArrayList<TransactionModel>>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getSFApiCaches(this,KEY_TRANSACTION_HISTORY)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<ArrayList<TransactionModel>>(gsonObject, type)
            }
            return mStoreListModels ?: ArrayList()
        }



}