package ninja11.fantasy

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.anim.FlashAnim
import ninja11.fantasy.R
import ninja11.fantasy.SportsFightApplication
import ninja11.fantasy.SupportActivity
import ninja11.fantasy.databinding.ActivityWithdrawAmountBinding
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.models.WalletInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WithdrawAmountsActivity : BaseActivity() {

    private var walletInfo: WalletInfo?=null
    private lateinit var userInfo: UserInfo
    private var mBinding: ActivityWithdrawAmountBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(this)
        userInfo = (application as SportsFightApplication).userInformations
        walletInfo = (application as SportsFightApplication).walletInfo
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_withdraw_amount
        )

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })
//        mBinding!!.toolbar.setTitle("Withdraw Money")
//        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(mBinding!!.toolbar)
//        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })

        mBinding!!.winningAmount.setText(String.format("₹ %s",walletInfo!!.walletAmount))

        mBinding!!.submitBtnWithdrawal.setOnClickListener(View.OnClickListener {
            var value =  mBinding!!.editWithdrawalAmount.text.toString()
            if(TextUtils.isEmpty(value)){
                value = "0"
            }
            var amount = value.toInt()
            if(amount >=200){
                showWithdrawalAlert(amount)
            }else {
                MyUtils.showMessage(this@WithdrawAmountsActivity,"You cannot withdraw amount less than 200 INR")
            }
        })

        mBinding!!.contactUs.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@WithdrawAmountsActivity, SupportActivity::class.java)
                startActivity(intent)
        })
    }


    fun showWithdrawalAlert(amount: Int) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
         builder.setTitle("Confirmation")
        //set message for alert dialog
        builder.setMessage(String.format("%d will be transffered to your verified bank accounts",amount ))
        builder.setIcon(android.R.drawable.ic_btn_speak_now)

        //performing positive action
        builder.setPositiveButton("Proceed"){
                dialogInterface, which ->
               withdrawalRequest(amount)
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    fun withdrawalRequest(amount:Int) {
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.token = MyPreferences.getToken(this)!!
        models.withdraw_amount = amount

        WebServiceClient(this).client.create(IApiMethod::class.java).withdrawAmount(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    var res = response!!.body()
                    if(res!=null && res.status) {
                       successAlert(res.message,true)
                    }else {
                        if(res!=null) {
                            successAlert(res.message,false)
                        }else {
                            successAlert("Please try again! Something went wrong",false)
                        }
                    }
                }

            })

    }

    private fun successAlert(message: String, isClose: Boolean) {
        var flashbar = Flashbar.Builder(this@WithdrawAmountsActivity)
            .gravity(Flashbar.Gravity.TOP)
            .title(getString(R.string.app_name))
            .message(message)
            .backgroundDrawable(R.color.red)
            .showIcon()
            .icon(R.drawable.ic_photo_camera_black_24dp)
            .iconAnimation(
                FlashAnim.with(this@WithdrawAmountsActivity)
                    .animateIcon()
                    .pulse()
                    .alpha()
                    .duration(750)
                    .accelerate()
            )
            .build()
        flashbar.show()
        Handler().postDelayed(Runnable {
            if(isClose) {
                setResult(Activity.RESULT_OK)
                finish()
            }

        }, 2000L)
    }


    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {
    }

}
