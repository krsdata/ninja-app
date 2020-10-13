package ninja11.fantasy.ui

import android.app.Activity.RESULT_OK
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ninja11.fantasy.*
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
//import com.paytm.pgsdk.PaytmOrder
//import com.paytm.pgsdk.PaytmPGService
//import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.databinding.FragmentJoinContestConfirmationBinding
import kotlin.math.abs

class JoinContestDialogFragment(
    private val myTeamArrayList: ArrayList<MyTeamModels>,
    val matchObject: UpcomingMatchesModel,
    private val contestModel: ContestModelLists
) : DialogFragment() {

    private lateinit var customeProgressDialog: CustomeProgressDialog
    var walletAmount:Double = 0.0
    var bonusAmount:Double = 0.0
    var createdTeamIdList :ArrayList<Int> ? =null
    private var mBinding: FragmentJoinContestConfirmationBinding? = null

    companion object{
        const val DISCOUNT_ON_BONUS = 10.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.dialog_theme)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_join_contest_confirmation, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.imgClose.setOnClickListener {
            activity!!.finish()
        }
        initWalletInfo()

        getWalletBalances()

    }

    private fun initWalletInfo() {
        val walletInfo = (activity!!.applicationContext as SportsFightApplication).walletInfo
        walletAmount = walletInfo.walletAmount
        bonusAmount = walletInfo.bonusAmount
        createdTeamIdList = ArrayList()
        var totalEntryFees = 0.0
        var discountFromBonusAmount=0.0
        val totalPayable: Double
        for(x in 0 until myTeamArrayList.size){
            val objects = myTeamArrayList[x]
            if(objects.isSelected!!){
                createdTeamIdList!!.add(objects.teamId!!.teamId)
                discountFromBonusAmount = ((contestModel.entryFees*DISCOUNT_ON_BONUS))/100
                totalEntryFees+= contestModel.entryFees
            }
        }
        if(bonusAmount>=discountFromBonusAmount) {
            totalPayable = totalEntryFees - discountFromBonusAmount
        }else {
            discountFromBonusAmount = bonusAmount
            totalPayable =totalEntryFees - discountFromBonusAmount
        }

        var actualPayable: Double
        if(totalPayable<=walletAmount){
            actualPayable = 0.0
        }else {
            actualPayable = walletAmount - totalPayable
        }
        mBinding!!.walletTotalAmount.text = String.format("Amount Added + Bonus =₹%.2f",walletAmount+bonusAmount)
        mBinding!!.entryFees.text = String.format("₹%.2f",totalEntryFees)
        mBinding!!.usableCashbonus.text = String.format("₹%.2f",discountFromBonusAmount)

        //var finalAmount = walletAmount - totalPayable
        mBinding!!.usableTopay.text = String.format("₹%.2f",actualPayable)
        if(actualPayable<0){
            mBinding!!.joinContest.text = "Pay Now"
            mBinding!!.joinContest.setBackgroundResource(R.drawable.default_flat_button_sportsfight)
        }
        mBinding!!.joinContest.setOnClickListener {

            if(actualPayable<0){
                val intent = Intent(activity, AddMoneyActivity::class.java)
                intent.putExtra(AddMoneyActivity.ADD_EXTRA_AMOUNT, abs(actualPayable) +10)
                startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
                dismiss()
            }else {
                placeOrders(totalEntryFees, actualPayable,discountFromBonusAmount)
            }
        }

        mBinding!!.termsCondition.setOnClickListener {
            val intent = Intent(activity!!, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
            val options =
                ActivityOptions.makeSceneTransitionAnimation(activity)
            startActivity(intent, options.toBundle())
        }

    }


    private fun placeOrders(totalEntryFees: Double,totalPayable: Double,discountFromBonusAmount: Double) {
        if(!isVisible){
          return
        }
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token = MyPreferences.getToken(activity!!)!!
        models.match_id =""+matchObject.matchId
        models.contest_id =""+contestModel.id
        models.created_team_id =createdTeamIdList

        models.entryFees =totalEntryFees.toString()
        models.totalPaidAmount =totalPayable.toString()
        models.discountOnBonusAmount =discountFromBonusAmount.toString()

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).joinContest(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if(isVisible) {
                        customeProgressDialog.dismiss()
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(isVisible){
                        customeProgressDialog.dismiss()
                        val res = response!!.body()
                        if(res!!.status) {
                            activity!!.setResult(RESULT_OK)
                            activity!!.finish()
                        }else {
                            MyUtils.showMessage(activity!!,res!!.message)
                        }
                    }
                }

            })
    }


    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()!!.setLayout(width, height)
        }
    }

    private fun getWalletBalances() {
        //var userInfo = (activity as PlugSportsApplication).userInformations
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token = MyPreferences.getToken(activity!!)!!

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if(res!=null) {
                        val responseModel = res.walletObjects
                        if(responseModel!=null) {
                            (activity!!.applicationContext as SportsFightApplication).saveWalletInformation(responseModel)
                            initWalletInfo()
                        }
                    }

                }

            })

    }
}