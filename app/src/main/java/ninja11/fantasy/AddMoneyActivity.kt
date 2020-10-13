package ninja11.fantasy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import ninja11.fantasy.R
import ninja11.fantasy.databinding.ActivityAddMoneyBinding
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.payments.PaytmHandler
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.BindingUtils.Companion.GOOGLE_TEZ_PACKAGE_NAME
import ninja11.fantasy.utils.BindingUtils.Companion.PAYMENT_GOOGLEPAY_UPI
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddMoneyActivity : AppCompatActivity() {


    private var mBinding: ActivityAddMoneyBinding? = null
    var paymentMode = ""
    var transactionId = ""

    companion object {
        val ADD_EXTRA_AMOUNT: String? = "add_extra_amount"
        val PAYEMENT_TYPE_PAYTM: String = "paytm"
        val PAYEMENT_TYPE_GPAY: String = "gpay"
        val PAYEMENT_TYPE_PHONEPAY: String = "phonepay"
        private const val TEZ_REQUEST_CODE = 10013
        private const val UPI_REQUEST_CODE = 10014
        private const val PAYTM_REQUEST_CODE = 10015
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEZ_REQUEST_CODE) {
            if (data != null && data.extras != null) {
                if (data.extras!!.getString("Status").equals("SUCCESS", ignoreCase = true)) {
//                    val rand = Random()
//                    val rand_int1 = rand.nextInt(1000000)
                    transactionId = data.extras!!.getString("txnId")!!
                    addWalletBalance()
                } else {

                    MyUtils.showToast(
                        this@AddMoneyActivity,
                        "Payment not completed, if any amount deducted, please contact us on our support system within 24hr with proof"
                    )
                    Toast.makeText(
                        this,
                        "Payment not completed, if any amount deducted, please contact us on our support system within 24hr with proof"
                        ,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                MyUtils.showToast(this@AddMoneyActivity, "Payment not completed please check")
                Toast.makeText(this, "Payment not completed please check", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_add_money
        )


        /*val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/
        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })


        if (intent.hasExtra(ADD_EXTRA_AMOUNT)) {
            var additionalAmount = intent.getDoubleExtra(ADD_EXTRA_AMOUNT, 0.0)
            mBinding!!.editAmounts.setText("" + additionalAmount)

        }

        initWalletInfo()

        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        getWalletBalances()


        mBinding!!.add100rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("100")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.rounded_btn_pink)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.text))

        })
        mBinding!!.add200rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("200")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.rounded_btn_pink)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.text))


        })
        mBinding!!.add300rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("300")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.rounded_btn_pink)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.text))
        })
        mBinding!!.add500rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("500")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.text))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.rounded_btn_pink)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.white))
        })
//
//        mBinding!!.btPaytm.setOnClickListener(View.OnClickListener {
//            mBinding!!.btPaytm.setBackgroundResource(R.drawable.btn_selector_purple)
//            mBinding!!.btGoogle.setBackgroundResource(R.drawable.button_selector)
//            var amount = mBinding!!.editAmounts.text.toString()
//                var amt = amount.toDouble()
//                if(amt>0) {
//
//                    payUsingPaytm(amt)
//
//                    }
//
//        })
//
//        mBinding!!.btGoogle.setOnClickListener(View.OnClickListener {
//            mBinding!!.btGoogle.setBackgroundResource(R.drawable.btn_selector_purple)
//            mBinding!!.btPaytm.setBackgroundResource(R.drawable.button_selector)
//            var amount = mBinding!!.editAmounts.text.toString()
//            var amt = amount.toDouble()
//            if(amt>0) {
//
//                payUsingGooglePay(amt)
//
//            }
//
//        })


//        mBinding?.rb1?.setOnClickListener {
//            val amount = mBinding!!.editAmounts.text.toString()
//            val amt = amount.toDouble()
//            if (amt > 0) {
//
//                payUsingPaytm(amt)
//
//
//            }
//        }
//        mBinding?.rb2?.setOnClickListener {
//            val amount = mBinding!!.editAmounts.text.toString()
//            val amt = amount.toDouble()
//            if (amt > 0) {
//
//                payUsingGooglePay(amt)
//
//
//            }
//        }
        /* mBinding!!.fancyRadioGroup.setOnCheckedChangeListener(
             RadioGroup.OnCheckedChangeListener { group, checkedId ->
                 if (mBinding!!.rb1.isChecked) {
                     mBinding!!.rb1.setBackgroundResource(R.drawable.purple_sqaure)
                     mBinding!!.rb2.setBackgroundResource(R.drawable.button_selector)


                 } else if (mBinding!!.rb2.isChecked) {
                     mBinding!!.rb2.setBackgroundResource(R.drawable.purple_sqaure)
                     mBinding!!.rb1.setBackgroundResource(R.drawable.button_selector)


                     var amount = mBinding!!.editAmounts.text.toString()
                     var amt = amount.toDouble()
                     if (amt > 0) {

                         payUsingGooglePay(amt)


                     }

                 }
             })*/

        mBinding!!.btAdd.setOnClickListener(View.OnClickListener {
            if (mBinding!!.rb1.isChecked==true){
            var amount = mBinding!!.editAmounts.text.toString()
            var amt = amount.toDouble()
            if(amt>0) {

                payUsingPaytm(amt)

            }
        } else if (mBinding!!.rb2.isChecked==true){

            var amount = mBinding!!.editAmounts.text.toString()
            var amt = amount.toDouble()
            if(amt>0) {

                payUsingGooglePay(amt)

            }

        }

        })

//        mBinding!!.addCash.setOnClickListener(View.OnClickListener {
//            var amount = mBinding!!.editAmounts.text.toString()
//            if(!TextUtils.isEmpty(amount)) {
//                var amt = amount.toDouble()
//                if(amt>0) {
//                    if(mBinding!!.usePaytmWallet.isChecked) {
//                        payUsingPaytm(amt)
//                    }else if(mBinding!!.useWalletGpay.isChecked) {
//                        payUsingGooglePay(amt)
//                    }else if(mBinding!!.useWalletPhonepay.isChecked) {
//                        payUsingPhonePay(amt)
//                    }
//                }else {
//                    MyUtils.showMessage(this@AddMoneyActivity,"Deposit amount cannot be less than 0 Rs.")
//                }
//            }else {
//                MyUtils.showMessage(this@AddMoneyActivity,"Please enter amount")
//            }
//
//        })
    }


    private fun payUsingPaytm(amount: Double) {

        paymentMode = PAYEMENT_TYPE_PAYTM
        PaytmHandler(this, object : PaytmHandler.OnCheckSumGenerated {
            override fun payNow(pmap: HashMap<String, String>) {
                val order = PaytmOrder(pmap)
                //PaytmPGService Service = PaytmPGService.getStagingService();
                val service = PaytmPGService.getProductionService()
                service.initialize(order, null)
                service.startPaymentTransaction(this@AddMoneyActivity,
                    true, true, object : PaytmPaymentTransactionCallback {
                        override fun onTransactionResponse(inResponse: Bundle?) {
                            var status = inResponse!!.getString("STATUS")
                            if (status!!.equals("TXN_SUCCESS", false)) {
                                transactionId = inResponse!!.getString("TXNID")!!
                                addWalletBalance()
                            } else {

                                MyUtils.showToast(
                                    this@AddMoneyActivity,
                                    "Unable to process the payment"
                                )
                                Toast.makeText(
                                    this@AddMoneyActivity,
                                    "unable to process the paymnet",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun clientAuthenticationFailed(inErrorMessage: String?) {

                        }

                        override fun someUIErrorOccurred(inErrorMessage: String?) {

                        }

                        override fun onTransactionCancel(
                            inErrorMessage: String?,
                            inResponse: Bundle?
                        ) {

                        }

                        override fun networkNotAvailable() {

                        }

                        override fun onErrorLoadingWebPage(
                            iniErrorCode: Int,
                            inErrorMessage: String?,
                            inFailingUrl: String?
                        ) {

                        }

                        override fun onBackPressedCancelTransaction() {
                            MyUtils.showMessage(
                                this@AddMoneyActivity,
                                "You have cancelled this transactions. Please try again!!"
                            )
                          /*  mBinding!!.rb1.setChecked(false)
                            mBinding!!.rb1.setBackgroundResource(R.drawable.button_selector)*/
                        }

                    })

            }

        }).paytmPayment("paytm" + System.currentTimeMillis(), amount)

    }


    private fun payUsingPhonePay(amount: Double) {
        paymentMode = PAYEMENT_TYPE_PHONEPAY
        MyUtils.showMessage(
            this@AddMoneyActivity,
            "PhonePay coming soon , please soon paytm or Gpay"
        )
    }

    private fun payUsingGooglePay(amount: Double) {
        paymentMode = PAYEMENT_TYPE_GPAY
        if (isAppInstalled(GOOGLE_TEZ_PACKAGE_NAME)) {
            // showProgress();
            var upiId: String = PAYMENT_GOOGLEPAY_UPI

            val uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", "Durgesh Mandal")
                .appendQueryParameter("tn", "Thank you for being our valued customers.")
                .appendQueryParameter("am", amount.toString())
                .appendQueryParameter("cu", "INR")
                .build()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME)
            startActivityForResult(
                intent,
                TEZ_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(
                "https://play.google.com/store/apps/details?id=" + GOOGLE_TEZ_PACKAGE_NAME
            )
            intent.setPackage("com.android.vending")
            startActivity(intent)
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val pm: PackageManager = getPackageManager()
        var installed = false
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return installed
    }

    private fun initWalletInfo() {
        var walletInfo = (application as SportsFightApplication).walletInfo
        var walletAmount = walletInfo.walletAmount
        mBinding!!.walletTotalAmount.text = String.format("₹%.2f", walletAmount)
        //mBinding!!.amountAdded.text = ""
    }

    fun getWalletBalances() {

        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        // models.token = MyPreferences.getToken(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    var res = response!!.body()
                    if (res != null) {
                        var responseModel = res.walletObjects
                        if (responseModel != null) {
                            (application as SportsFightApplication).saveWalletInformation(
                                responseModel
                            )
                            initWalletInfo()

                        }
                    }

                }

            })

    }


    fun addWalletBalance() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }

        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        //models.token = MyPreferences.getToken(this)!!
        models.deposit_amount = mBinding!!.editAmounts.text.toString()
        models.transaction_id = transactionId
        models.payment_mode = paymentMode
        models.payment_status = "success"


        WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {

                    var res = response!!.body()
                    if (res != null) {
                        var responseModel = res.walletObjects
                        if (responseModel != null) {
                            (application as SportsFightApplication).saveWalletInformation(
                                responseModel
                            )
                            MyUtils.showMessage(this@AddMoneyActivity, res.message)
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                }

            })
    }


}
