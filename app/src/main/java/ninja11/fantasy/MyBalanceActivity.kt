package ninja11.fantasy

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.VerifyDocumentsActivity.Companion.REQUESTCODE_VERIFY_DOC
import ninja11.fantasy.databinding.ActivityMyBallanceBinding
import ninja11.fantasy.models.WalletInfo
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


class MyBalanceActivity : AppCompatActivity() {

    private lateinit var walletInfo: WalletInfo
    private var customeProgressDialog: CustomeProgressDialog?=null
    private var mBinding: ActivityMyBallanceBinding? = null

    companion object{

        val REQUEST_CODE_ADD_MONEY : Int = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_my_ballance
        )

   /*     val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        walletInfo = (application as SportsFightApplication).walletInfo
      //  mBinding!!.toolbar.setTitle("My Balance")

        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })

//        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(mBinding!!.toolbar)
//        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })

        customeProgressDialog = CustomeProgressDialog(this)


        getWalletBalances()
        initWalletInfo()

        mBinding!!.addCash.setOnClickListener(View.OnClickListener {
            if(MyPreferences.getLoginStatus(this@MyBalanceActivity)!!){
                val intent = Intent(this@MyBalanceActivity, AddMoneyActivity::class.java)
                startActivityForResult(intent,REQUEST_CODE_ADD_MONEY)
            }else {
                val intent = Intent(this@MyBalanceActivity, RegisterScreenActivity::class.java)
                intent.putExtra(RegisterScreenActivity.ISACTIVITYRESULT,true)
                startActivityForResult(intent, RegisterScreenActivity.REQUESTCODE_LOGIN)
            }



        })

        if(walletInfo!=null){
            var accountStatus = walletInfo.accountStatus
            if(accountStatus!=null){
                if(walletInfo.bankAccountVerified==3){
                    mBinding!!.verifyAccountMessage.visibility = View.GONE
                    mBinding!!.verifyAccount.setText("REJECTED")
                    mBinding!!.verifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.verifyAccount.setBackgroundResource(R.drawable.button_selector_red)
                    mBinding!!.verifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.verifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })

                }else
                if(walletInfo.bankAccountVerified==2){
                    mBinding!!.verifyAccountMessage.visibility = View.GONE
                    mBinding!!.verifyAccount.setText("Account Verified")
                    mBinding!!.verifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.verifyAccount.setBackgroundResource(R.drawable.button_selector_green)
                    mBinding!!.verifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.verifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })

                }else if(walletInfo.bankAccountVerified==1){
                    mBinding!!.verifyAccountMessage.visibility = View.GONE
                    mBinding!!.verifyAccount.setText("Approval Pending")
                    mBinding!!.verifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.verifyAccount.setBackgroundResource(R.drawable.button_selector_white)
                    mBinding!!.verifyAccount.setTextColor(Color.BLACK)
                    mBinding!!.verifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })
                }else {
                    mBinding!!.verifyAccountMessage.visibility = View.VISIBLE
                    mBinding!!.verifyAccount.setOnClickListener(View.OnClickListener {
                        val intent = Intent(this@MyBalanceActivity, VerifyDocumentsActivity::class.java)
                        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                    })
                }
            }
        }else {
            mBinding!!.verifyAccountMessage.visibility = View.VISIBLE
            mBinding!!.verifyAccount.setOnClickListener(View.OnClickListener {
                val intent = Intent(this@MyBalanceActivity, VerifyDocumentsActivity::class.java)
                startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
            })
        }

        mBinding!!.btnWithdraw.setOnClickListener(View.OnClickListener {
            if(walletInfo.bankAccountVerified==2){
                var value =  walletInfo.walletAmount
                var amount =  value.toDouble()
                if(amount >=200){
                    val intent = Intent(this@MyBalanceActivity, WithdrawAmountsActivity::class.java)
                    startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                }else {
                    MyUtils.showToast(this@MyBalanceActivity,"Amount is less than 200 INR")
                }

            }else {
                var message = "Please Verify your account"
                if(walletInfo.bankAccountVerified==1){
                    message = "Documents Approvals Pending"
                }else if(walletInfo.bankAccountVerified==3){
                    message = "Your Document Rejected Please contact admin"
                }
                MyUtils.showToast(this@MyBalanceActivity,message)
            }

        })

        mBinding!!.txtRecentTransaction.setOnClickListener(View.OnClickListener {

            val intent = Intent(this@MyBalanceActivity, MyTransactionHistoryActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE_ADD_MONEY)
        })
    }

    private fun gotoDocumentsListActivity() {
        val intent = Intent(this, DocumentsListActivity::class.java)
        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_MONEY&& resultCode== Activity.RESULT_OK) {
            initWalletInfo()
        }else if (requestCode == REQUESTCODE_VERIFY_DOC&& resultCode== Activity.RESULT_OK) {
            getWalletBalances()
        }
    }
    private fun initWalletInfo() {
        var walletInfo = (application as SportsFightApplication).walletInfo
        var totalBalance = walletInfo.depositAmount.toDouble() + walletInfo.prizeAmount.toDouble() + walletInfo.bonusAmount.toDouble()
        mBinding!!.walletTotalAmount.setText(String.format("₹%s",totalBalance.toString()))
        mBinding!!.amountAdded.text = String.format("₹%.2f",walletInfo.depositAmount.toDouble() + walletInfo.prizeAmount.toDouble())
        mBinding!!.bonusAmount.text = String.format("₹%.2f",walletInfo.bonusAmount.toDouble())
    }

    fun getWalletBalances() {
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        //models.token = MyPreferences.getToken(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(models)
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
                    if(res!=null) {
                        var responseModel = res.walletObjects
                        if(responseModel!=null) {
                            (application as SportsFightApplication).saveWalletInformation(responseModel)
                            initWalletInfo()
                        }
                    }

                }

            })

    }




}
