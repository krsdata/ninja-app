package ninja11.fantasy.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.utils.HardwareInfoManager
import com.google.gson.Gson
import ninja11.fantasy.MainActivity
import ninja11.fantasy.R
import ninja11.fantasy.SportsFightApplication
import ninja11.fantasy.models.ResponseModel
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.login.viewmodel.LoginViewModel
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.databinding.ActivityRegisterBinding


class RegisterScreenActivity : BaseActivity(), Callback<ResponseModel> {

    private lateinit var userInfo: UserInfo
    private var photoUrl: String = ""
    private var isActivityRequiredResult: Boolean? = false
    var name = "";
    var binding: ActivityRegisterBinding? = null
    var viewmodel: LoginViewModel? = null

    companion object {
        var ISACTIVITYRESULT = "activityresult"
        val REQUESTCODE_LOGIN: Int = 1005
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = (applicationContext as SportsFightApplication).userInformations
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        MyAsyncTask().execute()

        /*val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/
//        binding!!.toolbar.setTitle(getString(R.string.screen_name_register))
//        binding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(binding!!.toolbar)


//        binding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        if (intent.hasExtra(ISACTIVITYRESULT)) {
            isActivityRequiredResult =
                intent.getBooleanExtra(RegisterScreenActivity.ISACTIVITYRESULT, false)
        }

//        val animation: Animation =
//            AnimationUtils.loadAnimation(this, R.anim.grow_linear_animation)
//        animation.setDuration(500)
//        binding!!.userInfoBar.setAnimation(animation)
//        binding!!.userInfoBar.animate()
//        animation.start()
//
        bindUI()
        initClicks()

    }

    private fun bindUI() {
        if (!userInfo.userId.equals("")) {
            binding!!.inputRefferal.visibility = View.GONE
        } else {
            binding!!.inputRefferal.visibility = View.VISIBLE
        }

        if (!TextUtils.isEmpty(userInfo.userEmail)) {
            binding!!.editEmail.setText(userInfo.userEmail)
            binding!!.editEmail.isEnabled = false
            binding!!.editEmail.isFocusable = false
        }
        if (!TextUtils.isEmpty(userInfo.mobileNumber)) {
            binding!!.editMobile.setText(userInfo.mobileNumber)
            binding!!.editMobile.isEnabled = false
            binding!!.editMobile.isFocusable = false
        }

        if (!TextUtils.isEmpty(userInfo.fullName)) {
            binding!!.editName.setText(userInfo.fullName)
            binding!!.editName.isEnabled = false
            binding!!.editName.isFocusable = false
        }

    }

    private fun initClicks() {
//        binding!!.txtTnc.setOnClickListener(object:View.OnClickListener{
//            override fun onClick(v: View?) {
//
//                val intent = Intent(this@RegisterScreenActivity, WebActivity::class.java)
//                intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
//                if (Build.VERSION.SDK_INT > 20) {
//                    val options =
//                        ActivityOptions.makeSceneTransitionAnimation(this@RegisterScreenActivity)
//                    startActivity(intent, options.toBundle())
//                } else {
//                    startActivity(intent)
//                }
//            }
//
//        })
        binding!!.registerButton.setOnClickListener(View.OnClickListener {

            var referralCode = binding!!.editInvitecode.text.toString()
            var teamName = binding!!.editTeamName.text.toString()
            var editName = binding!!.editName.text.toString()
            var mobileNumber = binding!!.editMobile.text.toString()
            var emailAddress = binding!!.editEmail.text.toString()

            if (TextUtils.isEmpty(teamName)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Enter Your Team Name(Nick Name)")
                return@OnClickListener
            } else if (TextUtils.isEmpty(editName)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter your real name")
                return@OnClickListener
            } else if (TextUtils.isEmpty(mobileNumber)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter valid mobile number")
                return@OnClickListener
            } else if (mobileNumber.length < 10) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter valid mobile number")
                return@OnClickListener
            } else if (TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter valid email address")
                return@OnClickListener
            }
            name = editName
            register(mobileNumber, emailAddress)
        })
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
        this.photoUrl = url
    }

    override fun onStart() {
        super.onStart()
    }

    fun register(
        mobile: String?,
        email: String?
    ) {
        if (!MyUtils.isConnectedWithInternet(this@RegisterScreenActivity)) {
            MyUtils.showToast(this@RegisterScreenActivity, "No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        var request = RequestModel()

        request.user_id = userInfo!!.userId!!.toString()
        request.name = name
        request.image_url = photoUrl
        request.email = email!!
        request.mobile_number = mobile!!
        request.referral_code = binding!!.editInvitecode.text.toString()
        request.team_name = binding!!.editTeamName.text.toString()
        request.device_id = notificationToken
        request.deviceDetails = HardwareInfoManager(this).collectData()
        val  gson = Gson()
        val json = gson.toJson(request)
        println("request to string     $json")
        WebServiceClient(this).client.create(IApiMethod::class.java).customerLogin(request)
            .enqueue(this)
    }

    override fun onResponse(call: Call<ResponseModel>?, response: Response<ResponseModel>?) {
        if (!isFinishing) {
            customeProgressDialog!!.dismiss()
            val responseb = response!!.body()
            val  gson = Gson()
            val json = gson.toJson(responseb)
            println("response to string     $json")
            if (responseb != null) {
                if (responseb.status) {
                    var infoModels = responseb.infomodel
                    if (infoModels != null) {
                        if (TextUtils.isEmpty(responseb.infomodel!!.profileImage)) {
                            MyPreferences.setProfilePicture(this, photoUrl)
                        }
                        MyPreferences.setToken(this, responseb.token)
                        MyPreferences.setUserID(this, "" + responseb.infomodel!!.userId)

                        (applicationContext as SportsFightApplication).saveUserInformations(
                            responseb.infomodel
                        )
                        if (TextUtils.isEmpty(infoModels.mobileNumber) || TextUtils.isEmpty(
                                infoModels.userEmail
                            ) || TextUtils.isEmpty(
                                infoModels.fullName
                            )
                        ) {
                            val intent =
                                Intent(
                                    this@RegisterScreenActivity,
                                    RegisterScreenActivity::class.java
                                )
                            startActivity(intent)
                        } else
//                            if (infoModels.isOtpVerified) {
                                MyPreferences.setLoginStatus(this@RegisterScreenActivity, true)
                                var intent =
                                    Intent(this@RegisterScreenActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                setResult(Activity.RESULT_OK)
                                startActivity(intent)
                                finish()
                         /*   } else {
                                var intent = Intent(this, OtpVerifyActivity::class.java)
                                startActivityForResult(
                                    intent,
                                    RegisterScreenActivity.REQUESTCODE_LOGIN
                                )
                            }*/
                    } else {
                        MyUtils.showToast(this@RegisterScreenActivity, responseb.message)

                        Toast.makeText(this, responseb.message, Toast.LENGTH_LONG).show()

                    }
                } else {
                    MyUtils.showToast(this@RegisterScreenActivity, responseb.message)
                    Toast.makeText(this, responseb.message, Toast.LENGTH_LONG).show()
                }

            } else {
                MyUtils.showToast(
                    this@RegisterScreenActivity,
                    "Something went wrong, please contact Sportsfight team"
                )
                Toast.makeText(this, "something went wrong", Toast.LENGTH_LONG).show()

            }
        }

    }

    override fun onFailure(call: Call<ResponseModel>?, t: Throwable?) {
        customeProgressDialog!!.dismiss()
        Toast.makeText(
            this
            , "Warning , ${t?.message}", Toast.LENGTH_LONG
        ).show()


    }

}
