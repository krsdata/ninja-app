package ninja11.fantasy.ui.myaccounts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ninja11.fantasy.*
import ninja11.fantasy.models.WalletInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.dashboard.MyAccountFragment
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.*
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.databinding.FragmentMyAccountBalanceBinding


class MyAccountBalanceFragment(val myAccountFragment: MyAccountFragment) : Fragment() {

    private var mBinding: FragmentMyAccountBalanceBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_account_balance, container, false)

        return mBinding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var walletInfo = (activity!!.applicationContext as SportsFightApplication).walletInfo
        mBinding!!.btnAddCash.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, AddMoneyActivity::class.java)
            startActivity(intent)
        })

        mBinding!!.btnWithdraw.setOnClickListener(View.OnClickListener {
            if(walletInfo.bankAccountVerified==2){
                var amount =walletInfo.walletAmount
                if(amount >=200){
                    val intent = Intent(activity!!, WithdrawAmountsActivity::class.java)
                    startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                }else {
                    MyUtils.showToast(activity!! as AppCompatActivity,"Amount is less than 200 INR")
                }

            }else {
                var message = "Please Verify your account"
                if(walletInfo.bankAccountVerified==1){
                    message = "Documents Approvals Pending"
                }else if(walletInfo.bankAccountVerified==3){
                    message = "Your Document Rejected Please contact admin"
                }
                MyUtils.showToast(activity!! as AppCompatActivity,message)
            }

        })

        mBinding!!.refferalList.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, RefferalFriendsListActivity::class.java)
            startActivity(intent)
        })


        if(MyPreferences.getLoginStatus(activity!!)!!){
             getWalletBalances()
        }


        if(walletInfo!=null){
            mBinding!!.progressBarPlayingHistory.visibility  =View.GONE
            initWalletViews(walletInfo)
        }
    }

    fun getWalletBalances() {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        mBinding!!.progressBarPlayingHistory.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token = MyPreferences.getToken(this)!!

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if(isAdded) {
                        mBinding!!.progressBarPlayingHistory.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(isAdded) {
                        mBinding!!.progressBarPlayingHistory.visibility = View.GONE
                        var res = response!!.body()
                        if(res!=null) {
                            var responseModel = res.walletObjects
                            if(responseModel!=null) {
                                (activity!!.applicationContext as SportsFightApplication).saveWalletInformation(responseModel)
                                initWalletViews(responseModel)
                                if(myAccountFragment!=null && myAccountFragment.isAdded) {
                                    myAccountFragment.initProfile()
                                }
                            }
                        }
                    }


                }

            })

    }

    private fun initWalletViews(responseModel: WalletInfo) {
        mBinding!!.addedAmount.setText(String.format("₹ %s",responseModel.depositAmount))
        mBinding!!.winningAmount.setText(String.format("₹ %s",responseModel.prizeAmount))

        mBinding!!.cashBonus.setText(String.format("₹ %s",responseModel.bonusAmount))

        var totalBalance =
            responseModel.depositAmount.toDouble() + responseModel.prizeAmount.toDouble() + responseModel.bonusAmount.toDouble()
        mBinding!!.totalBalance.setText(String.format("₹ %s",totalBalance.toString()))

        mBinding!!.friendsCounts.setText(String.format("%d",responseModel.refferalCounts))


    }


}
