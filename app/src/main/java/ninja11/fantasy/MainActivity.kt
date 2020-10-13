package ninja11.fantasy
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import ninja11.fantasy.databinding.ActivityMainBinding
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.UpdateAppDialogFragment
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




//import javax.swing.text.StyleConstants.getBackground


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener/*,
    NavController.OnDestinationChangedListener*/ {
    private lateinit var walletBalance: TextView
    private lateinit var navController: NavController
    private var mBinding: ActivityMainBinding? = null

    companion object {

        var updatedApkUrl: String = ""
        var releaseNote: String = ""
        var CHECK_APK_UPDATE_API: Boolean = false

    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

//        getWindow().setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
/*
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setSupportActionBar(mBinding!!.toolbar)
        // setUpDrawerLayout()

        navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_myaccount,
                R.id.navigation_more
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
//        mBinding!!.navView1.bottomNavigationView.setupWithNavController(navController)
//        navController.addOnDestinationChangedListener(this)

        mBinding!!.imgWalletAmount.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MainActivity, MyBalanceActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        })
//        mBinding!!.notificationId.setOnClickListener(View.OnClickListener {
//            val intent = Intent(this@MainActivity, NotificationListActivity::class.java)
//            startActivityForResult(intent,MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
//        })

        getWalletBalances()

        mBinding!!.profileImage.setOnClickListener {
            val intent = Intent(this@MainActivity, EditProfileActivity::class.java)
            intent.putExtra(
                FullScreenImageViewActivity.KEY_IMAGE_URL,
                MyPreferences.getProfilePicture(this@MainActivity)!!
            )
            startActivity(intent)
        }
        viewAllMatches()
        mBinding!!.navView1.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    mBinding!!.navView1.updateFabPosition2(0)
                }
                R.id.navigation_dashboard -> {
                    mBinding!!.navView1.updateFabPosition2(1)
                }
                R.id.navigation_myaccount -> {
                    mBinding!!.navView1.updateFabPosition2(2)
                }
                R.id.navigation_more -> {
                    mBinding!!.navView1.updateFabPosition2(3)
                }
            }
            if(navController.currentDestination!!.id!= item.itemId)
            navController.navigate(item.itemId)
            false
        }
    }


//    override fun onDestinationChanged(
//        controller: NavController,
//        destination: NavDestination,
//        arguments: Bundle?
//    ) {
//        when(destination.id){
//            R.id.navigation_home->{
//                mBinding!!.navView1.updateFabPosition(0)
//            }
//            R.id.navigation_dashboard->{
//                mBinding!!.navView1.updateFabPosition(1)
//            }
//            R.id.navigation_myaccount->{
//                mBinding!!.navView1.updateFabPosition(2)
//            }
//            R.id.navigation_more->{
//                mBinding!!.navView1.updateFabPosition(3)
//            }
//        }
//
//    }

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(MyPreferences.getProfilePicture(this))) {
            Glide.with(this)
                .load(MyPreferences.getProfilePicture(this))
                .placeholder(R.drawable.dummy)
                .into(mBinding!!.profileImage)
        }
    }

    fun viewUpcomingMatches() {
        mBinding!!.navView1.bottomNavigationView.selectedItemId = R.id.navigation_home
    }

    fun viewAllMatches() {
        mBinding!!.navView1.bottomNavigationView.selectedItemId = R.id.navigation_dashboard
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyBalanceActivity.REQUEST_CODE_ADD_MONEY) {
            getWalletBalances()
            setWalletBalanceValue()
        }
    }


    private fun setWalletBalanceValue() {

//        var walletInfo = (application as SportsFightApplication).walletInfo
//        if(walletInfo!=null){
//            var totalBalance =
//                walletInfo.depositAmount + walletInfo.prizeAmount + walletInfo.bonusAmount
//            mBinding!!.walletAmount.setText(String.format("₹%s",totalBalance.toString()))
//        }

    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {

    }

    override fun onStart() {
        super.onStart()
        setWalletBalanceValue()
        if (CHECK_APK_UPDATE_API) {
            CHECK_APK_UPDATE_API = false
            val fm = supportFragmentManager
            val pioneersFragment =
                UpdateAppDialogFragment(updatedApkUrl, releaseNote)
            pioneersFragment.show(fm, "updateapp_tag")
        }
    }

    fun getWalletBalances() {
        //var userInfo = (activity as PlugSportsApplication).userInformations
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        // models.token = MyPreferences.getToken(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    CHECK_APK_UPDATE_API = false
                    var res = response!!.body()
                    if (res != null) {
                        var responseModel = res.walletObjects
                        if (responseModel != null) {
                            (application as SportsFightApplication).saveWalletInformation(
                                responseModel
                            )
                            setWalletBalanceValue()
                        }
                    }

                }

            })

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }


            R.id.nav_wallet -> {
                val intent = Intent(this@MainActivity, MyBalanceActivity::class.java)
                startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
                if (Build.VERSION.SDK_INT > 21) {
                    val options =
                        ActivityOptions.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }

            R.id.nav_referenearn -> {
                val intent = Intent(this@MainActivity, MyTransactionHistoryActivity::class.java)
                startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
                if (Build.VERSION.SDK_INT > 21) {
                    val options =
                        ActivityOptions.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }

            R.id.nav_tnc -> {
                val intent = Intent(this@MainActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
                if (Build.VERSION.SDK_INT > 21) {
                    val options =
                        ActivityOptions.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }
            R.id.nav_privacy -> {
                val intent = Intent(this@MainActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
                if (Build.VERSION.SDK_INT > 21) {
                    val options =
                        ActivityOptions.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }

            R.id.nav_aboutus -> {
                val intent = Intent(this@MainActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_ABOUT_US)
                if (Build.VERSION.SDK_INT > 21) {
                    val options =
                        ActivityOptions.makeSceneTransitionAnimation(this)
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }


//            R.id.nav_logout -> {
//                var userId = MyPreferences.getUserID(this@MainActivity)!!
//                if (!TextUtils.isEmpty(userId)) {
//                    customeProgressDialog.show()
//                    var request = RequestModel()
//                    request.user_id = userId
//                    WebServiceClient(this@MainActivity).client.create(BackEndApi::class.java).logout(request)
//                        .enqueue(object : Callback<UsersPostDBResponse?> {
//                            override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
//
//                            }
//
//                            override fun onResponse(
//                                call: Call<UsersPostDBResponse?>?,
//                                response: Response<UsersPostDBResponse?>?
//                            ) {
//
//                                customeProgressDialog.dismiss()
//                                MyPreferences.clear(this@MainActivity)
//                                val intent = Intent(
//                                    this@MainActivity,
//                                    SplashScreenActivity::class.java
//                                )
//                                startActivity(intent)
//                                finish()
//                            }
//
//                        })
//                 }
//
//
//            }


        }

        // mBinding!!.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            return true
        }
        return super.onOptionsItemSelected(item)
    }


    fun showToolbar() {
        mBinding!!.toolbar.visibility = View.VISIBLE
    }

    fun hideToolbar() {
        mBinding!!.toolbar.visibility = View.GONE
    }


}
