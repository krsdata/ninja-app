package ninja11.fantasy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.databinding.ActivityOtpVerifyBinding
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.login.RegisterScreenActivity
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class OtpVerifyActivity : AppCompatActivity() {

    companion object{
        val EXTRA_KEY_EDIT_MOBILE_NUMBER : String = "isEditMobileNumber"
    }

    val RESEND_CODE_TIMER:Long = 1 * 60 * 1000
    private lateinit var userInfo: UserInfo
    private var customeProgressDialog: CustomeProgressDialog?=null
    private var mBinding: ActivityOtpVerifyBinding? = null
    private var isActivityRequiredResult: Boolean?=false

    private val countDownTimer: CountDownTimer = object : CountDownTimer(
        RESEND_CODE_TIMER,
        1000
    ) {
        override fun onTick(durationInMillis: Long) {
            val second = durationInMillis / 1000 % 60
            val minute = durationInMillis / (1000 * 60) % 60
            val formattedValues = String.format("%02d:%02d", minute, second)
            Log.d("timeTick", formattedValues)
            mBinding!!.timerOtpDetect.setText(formattedValues)
        }

        override fun onFinish() {
            mBinding!!.timerOtpDetect.setText("00:00")

            enableResendOtp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         mBinding = DataBindingUtil.setContentView(this, R.layout.activity_otp_verify)
        userInfo = (application as SportsFightApplication).userInformations
        customeProgressDialog = CustomeProgressDialog(this)
        mBinding!!.mobileNumber.setText(userInfo.mobileNumber);
        if(intent.hasExtra(RegisterScreenActivity.ISACTIVITYRESULT)) {
            isActivityRequiredResult =
                intent.getBooleanExtra(RegisterScreenActivity.ISACTIVITYRESULT, false)
        }

       /* val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        mBinding!!.toolbar.setTitle("OTP Verification")
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.resendOtp.setOnClickListener(View.OnClickListener {
            if(!MyUtils.isConnectedWithInternet(this)) {
                MyUtils.showToast(this,"No Internet connection found")
            }else {
                resendOtp()
                mBinding!!.resendOtp.setVisibility(View.INVISIBLE)
                countDownTimer.start()
            }
        })
        mBinding!!.btnSubmit.setOnClickListener(View.OnClickListener {
            verifyOtp()
        })

        initData()
        if(intent.hasExtra(EXTRA_KEY_EDIT_MOBILE_NUMBER) && intent.getBooleanExtra(
                EXTRA_KEY_EDIT_MOBILE_NUMBER,false)) {
            mBinding!!.changeMobileNumber.visibility = View.VISIBLE
            mBinding!!.changeMobileNumber.setOnClickListener(View.OnClickListener {
                switchNumber()
            })
        }else {
            mBinding!!.changeMobileNumber.visibility = View.GONE
            mBinding!!.mobileNumber.isEnabled = false
            mBinding!!.mobileNumber.isFocusable = false

        }
    }

    private fun initData() {

        disableResendOtp()
        mBinding!!.resendOtp.setVisibility(View.INVISIBLE)
        countDownTimer.start()
        mBinding!!.otpCode1.addTextChangedListener(RequestNextFocusTextWatcher(mBinding!!.otpCode1))
        mBinding!!.otpCode2.addTextChangedListener(RequestNextFocusTextWatcher(mBinding!!.otpCode2))
        mBinding!!.otpCode3.addTextChangedListener(RequestNextFocusTextWatcher(mBinding!!.otpCode3))
        mBinding!!.otpCode4.addTextChangedListener(RequestNextFocusTextWatcher(mBinding!!.otpCode4))

        mBinding!!.otpCode1.setOnKeyListener(RequestBackKeyPressEvent(mBinding!!.otpCode1));
        mBinding!!.otpCode2.setOnKeyListener(RequestBackKeyPressEvent(mBinding!!.otpCode2));
        mBinding!!.otpCode3.setOnKeyListener(RequestBackKeyPressEvent(mBinding!!.otpCode3));
        mBinding!!.otpCode4.setOnKeyListener(RequestBackKeyPressEvent(mBinding!!.otpCode4));

    }

    private fun disableResendOtp() {
        mBinding!!.resendOtp.setVisibility(View.INVISIBLE)
        mBinding!!.resendOtp.setTextColor(getResources().getColor(R.color.grey))
        mBinding!!.resendOtp.setEnabled(false)
    }
    private fun enableResendOtp() {
        mBinding!!.resendOtp.setVisibility(View.VISIBLE)
        mBinding!!.resendOtp.setTextColor(getResources().getColor(R.color.black))
        mBinding!!.resendOtp.setEnabled(true)
    }


    private fun resendOtp() {
        customeProgressDialog!!.show()
        mBinding!!.progressBar.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.mobile_number = userInfo.mobileNumber

        WebServiceClient(this!!).client.create(IApiMethod::class.java).generateOtp(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.hide()
                    mBinding!!.progressBar.visibility  =View.GONE


                }

            })
    }

    private fun switchNumber() {
        var mobileNumber = mBinding!!.mobileNumber.text.toString()

        if(TextUtils.isEmpty(mobileNumber) || mobileNumber.length<10){
            MyUtils.showToast(this@OtpVerifyActivity,"Please enter valid mobile number , to authenticate with Sportsfight system")
            return
        }

        mBinding!!.progressBar.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.mobile_number = mobileNumber
        customeProgressDialog!!.show()
        WebServiceClient(this!!).client.create(IApiMethod::class.java).switchNumbers(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    t!!.message?.let { MyUtils.showToast(this@OtpVerifyActivity, it) };
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    mBinding!!.progressBar.visibility  =View.GONE
                    var res = response!!.body()
                    if(res!=null && res.status) {
                        userInfo.mobileNumber = mBinding!!.mobileNumber.text.toString()
                        MyUtils.showToast(this@OtpVerifyActivity,"SMS Sent to "+userInfo.mobileNumber +" Please enter OTP to verify that number");
                    }else {
                        MyUtils.showToast(this@OtpVerifyActivity,"unable to change your number");
                    }

                }

            })

    }

    private fun verifyOtp() {
        var otpCode1 = mBinding!!.otpCode1.text.toString()
        var otpCode2 = mBinding!!.otpCode2.text.toString()
        var otpCode3 = mBinding!!.otpCode3.text.toString()
        var otpCode4 = mBinding!!.otpCode4.text.toString()

        if(TextUtils.isEmpty(otpCode1)){
            MyUtils.showToast(this@OtpVerifyActivity,"OTP Cannot be blank")
            return
        }

        if(TextUtils.isEmpty(otpCode2)){
            MyUtils.showToast(this@OtpVerifyActivity,"OTP Cannot be blank")
            return
        }

        if(TextUtils.isEmpty(otpCode3)){
            MyUtils.showToast(this@OtpVerifyActivity,"OTP Cannot be blank")
            return
        }

        if(TextUtils.isEmpty(otpCode4)){
            MyUtils.showToast(this@OtpVerifyActivity,"OTP Cannot be blank")
            return
        }

        mBinding!!.progressBar.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.mobile_number = userInfo.mobileNumber
        models.otp = String.format("%s%s%s%s",otpCode1,otpCode2,otpCode3,otpCode4)


        WebServiceClient(this!!).client.create(IApiMethod::class.java).verifyOtp(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.progressBar.visibility  =View.GONE
                    var res = response!!.body()
                    if(res!=null && res.status) {
                        if(res!=null && res.status) {
                            MyPreferences.setLoginStatus(this@OtpVerifyActivity, true)
                            if(!isActivityRequiredResult!!) {
                                var intent = Intent(this@OtpVerifyActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }else {
                                setResult(Activity.RESULT_OK)
                            }

                            finish()
                        }else {
                            MyUtils.showToast(this@OtpVerifyActivity,"OTP Does not matched");
                        }

                    }

                }

            })

    }

    private fun isOtpValidated(): Boolean {
        val value1: String = mBinding!!.otpCode1.getText().toString()
        val value2: String = mBinding!!.otpCode2.getText().toString()
        val value3: String = mBinding!!.otpCode3.getText().toString()
        val value4: String = mBinding!!.otpCode4.getText().toString()
        if (TextUtils.isEmpty(value1)) {
            return false
        }
        if (TextUtils.isEmpty(value2)) {
            return false
        }
        if (TextUtils.isEmpty(value3)) {
            return false
        }
        return if (TextUtils.isEmpty(value4)) {
            false
        } else true
    }


    inner class RequestBackKeyPressEvent(val view: View) :
        View.OnKeyListener {
        override fun onKey(
            view: View,
            keyCode: Int,
            event: KeyEvent
        ): Boolean {
            Log.d("keyactions", keyCode.toString())
            if (event.action == KeyEvent.ACTION_DOWN) {
                Log.d("keyactions", "ACTION_DOWN")
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    Log.d("keyactions", "KEYCODE_DEL||KEYCODE_BACK")
                    when (view.id) {
                        R.id.otp_code1 ->                             //mBinding.otpObject2.requestFocus();
                            mBinding!!.otpCode1.setText("")
                        R.id.otp_code2 -> {
                            mBinding!!.otpCode2.setText("")
                            mBinding!!.otpCode1.requestFocus()
                        }
                        R.id.otp_code3 -> {
                            mBinding!!.otpCode3.setText("")
                            mBinding!!.otpCode2.requestFocus()
                        }
                        R.id.otp_code4 -> {
                            mBinding!!.otpCode4.setText("")
                            mBinding!!.otpCode3.requestFocus()
                        }

                    }
                    return true
                }
            }
            return false
        }

    }

    inner class RequestNextFocusTextWatcher(val view: View) :
        TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence,i: Int,i1: Int,i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence,i: Int,i1: Int,i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            if (!TextUtils.isEmpty(text) && text.length == 1) {
                when (view.id) {
                    R.id.otp_code1 ->
                        mBinding!!.otpCode2.requestFocus()
                    R.id.otp_code2 ->
                        mBinding!!.otpCode3.requestFocus()
                    R.id.otp_code3 ->
                        mBinding!!.otpCode4.requestFocus()
                    R.id.otp_code4 -> if (isOtpValidated()) {
                        val value1: String = mBinding!!.otpCode1.getText().toString()
                        val value2: String = mBinding!!.otpCode2.getText().toString()
                        val value3: String = mBinding!!.otpCode3.getText().toString()
                        val value4: String = mBinding!!.otpCode4.getText().toString()
                        val otp =
                            String.format("%s%s%s%s", value1, value2, value3, value4)
                        Log.d("OTPVal", otp)
                        verifyOtp()
                    }
                }
            }
        }

    }

}
