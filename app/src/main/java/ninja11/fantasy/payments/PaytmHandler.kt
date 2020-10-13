package ninja11.fantasy.payments

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import ninja11.fantasy.models.ResponseModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.WebServiceClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit

class PaytmHandler(private val mContext: Context, private val mListeners: OnCheckSumGenerated) {
    internal var mAmount = ""
    private var mOrderId: String? = null
    private var callbackUrl: String? = null

    private val client: OkHttpClient
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
            okHttpClient.connectTimeout(30, TimeUnit.SECONDS)
            okHttpClient.readTimeout(30, TimeUnit.SECONDS)
            okHttpClient.writeTimeout(5, TimeUnit.MINUTES)
            okHttpClient.addInterceptor(interceptor)
            return okHttpClient.build()
        }

    fun paytmPayment(id: String, amount: Double) {
        mAmount = amount.toString()
        val rand = Random()
        CUST_ID = "CUST_00" + MyPreferences.getUserID(mContext)!!
        MOBILE_NO = MyPreferences.getMobile(mContext)
        EMAIL_ID = MyPreferences.getEmail(mContext)
        mOrderId = java.lang.String.format("%06d", rand.nextInt(1000000))
        callbackUrl = CALLBACK_URL + mOrderId!!
        val requestModel = EMAIL_ID?.let {
            MOBILE_NO?.let { it1 ->
                RequestPaytmModel(
                    mOrderId!!,
                    CUST_ID,
                    mAmount,
                        it,
                        it1,
                        callbackUrl!!,
                    MID,
                    INDUSTRY_TYPE_ID,
                    CHANNEL_ID, WEBSITE)
            }
        }


        if(!MyUtils.isConnectedWithInternet(mContext!! as AppCompatActivity)) {
            MyUtils.showToast(mContext!! as AppCompatActivity,"No Internet connection found")
            return
        }
        WebServiceClient(mContext).client.create(IApiMethod::class.java).getPaytmChecksum(requestModel!!)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    var res = response!!.body()
                    if(res!=null) {
                        payNow(res!!.checksum)
                    }

                }

            })
//        if (requestModel != null) {
//            createService(mContext).getPaytmChecksum(requestModel)
//                    .enqueue(object : Callback<ProductsResponseModel> {
//                        override fun onResponse(call: Call<ProductsResponseModel>,
//                                                response: Response<ProductsResponseModel>) {
//
//                            if (response.isSuccessful) {
//                                payNow(response.body()!!.checksum)
//                            }
//                        }
//
//                        override fun onFailure(call: Call<ProductsResponseModel>, t: Throwable) {
//                            Log.e("FAILURE_LOGIN", "" + t.message)
//                        }
//                    })
//        }


    }


    private fun payNow(checksum: String?) {
        val pmap =
            HashMap<String, String>()
        pmap["MID"] = MID
        pmap["INDUSTRY_TYPE_ID"] = INDUSTRY_TYPE_ID
        pmap["CHANNEL_ID"] = CHANNEL_ID
        pmap["WEBSITE"] = WEBSITE

        pmap["ORDER_ID"] = mOrderId!!
        pmap["CUST_ID"] = CUST_ID
        pmap["MOBILE_NO"] = MOBILE_NO!!
        pmap["EMAIL"] = EMAIL_ID!!
        pmap["TXN_AMOUNT"] = mAmount
        pmap["CALLBACK_URL"] = callbackUrl!!
        pmap["CHECKSUMHASH"] = checksum!!

        mListeners.payNow(pmap)
    }




    interface OnCheckSumGenerated {


        fun payNow(pmap: HashMap<String, String>)
    }

    companion object {
        // private static String MercahntKey = "NB%nQa#Dwj7r8Dox";  //Production

        private val MID = "LoPyaL46096848169204"
        private val INDUSTRY_TYPE_ID = "Retail"
        private val CHANNEL_ID = "WAP"
        private val WEBSITE = "DEFAULT"
        private var CUST_ID = ""
        private var MOBILE_NO: String? = ""
        private var EMAIL_ID: String? = ""
        private val CALLBACK_URL = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID="
    }

}
