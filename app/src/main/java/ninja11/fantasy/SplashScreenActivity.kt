package ninja11.fantasy

import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.firebase.BuildConfig
import ninja11.fantasy.utils.HardwareInfoManager
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import ninja11.fantasy.databinding.ActivitySplashBinding
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.login.LoginScreenActivity

import ninja11.fantasy.utils.MyPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class SplashScreenActivity : BaseActivity() {

    private lateinit var mContext: SplashScreenActivity
    private var mBinding: ActivitySplashBinding? = null
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAYED : Long  = 4000
  //  val num = arrayOf(R.drawable.splash)


    internal  val mRunnable : Runnable = Runnable {
         if(!isFinishing){
               checkUserLoggedIn()
         }
    }

    override fun onStart() {

        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        super.onStart()
        MyAsyncTask().execute()
    }

    private fun checkUserLoggedIn() {

        if(!MyPreferences.getLoginStatus(mContext)!!){
            loginRequired()
        }else {

            val intent = Intent(
                applicationContext,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }


    private fun loginRequired() {

        val intent = Intent(applicationContext,
            LoginScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.CHECK_APK_UPDATE_API =false
        mContext = this@SplashScreenActivity;

        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_splash

        )



        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAYED)
    }

//    private fun getRandomDrawable(): Int {
//       // val list = (1..num.size-1).filter { it % 2 == 0 }
//        var ran = Random.nextInt(0, num.size)
//        return num[ran]
//    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {


    }


    inner class MyAsyncTask : AsyncTask<Unit, Unit, String>() {

        override fun doInBackground(vararg params: Unit): String {


            return FirebaseInstanceId.getInstance().getToken(getString(R.string.gcm_default_sender_id), "FCM")!!

        }
        override fun onPostExecute(result: String) {
            updateFireBase(result)
            updateCheckApk()
        }

        private fun updateFireBase(result:String) {

            FirebaseApp.initializeApp(mContext);

            var userId = MyPreferences.getUserID(this@SplashScreenActivity)!!
            var notid = result
            if (!TextUtils.isEmpty(notid) && !TextUtils.isEmpty(userId)) {
                var request = RequestModel()
                request.user_id = userId
                request.device_id = notid!!
                request.deviceDetails = HardwareInfoManager(this@SplashScreenActivity).collectData()
                WebServiceClient(this@SplashScreenActivity).client.create(IApiMethod::class.java).deviceNotification(request)
                    .enqueue(object : Callback<UsersPostDBResponse?> {
                        override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                        }

                        override fun onResponse(
                            call: Call<UsersPostDBResponse?>?,
                            response: Response<UsersPostDBResponse?>?
                        ) {


                        }

                    })
            }
        }
    }

    fun updateCheckApk() {
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.version_code = BuildConfig.VERSION_CODE

        WebServiceClient(this).client.create(IApiMethod::class.java).apkUpdate(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    MainActivity.CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {

                    var res = response!!.body()
                    println("ress   ${res!!.updatedApkUrl}")
                    if(!isFinishing){
                        if(res!=null) {
                            if(res!!.status){
                                MainActivity.CHECK_APK_UPDATE_API = true
                                MainActivity.updatedApkUrl = res!!.updatedApkUrl
                                MainActivity.releaseNote = res!!.releaseNote
                            }

                        }
                    }


                }

            })

    }

}
