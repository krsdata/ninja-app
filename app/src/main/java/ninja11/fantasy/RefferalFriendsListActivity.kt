package ninja11.fantasy

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.databinding.ActivityRefferalFriendsBinding
import ninja11.fantasy.models.RefferalUsersModel
import ninja11.fantasy.models.TransactionModel
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.home.models.UsersPostDBResponse

import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class RefferalFriendsListActivity : BaseActivity() {

    private lateinit var userInfo: UserInfo
    private lateinit var adapter: RefferalsListAdaptors
    private var mBinding: ActivityRefferalFriendsBinding? = null

    var checkinArrayList = ArrayList<RefferalUsersModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = (application as SportsFightApplication).userInformations
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_refferal_friends
        )
        mBinding!!.toolbar.setTitle("My Refferals Friends")
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.refferNowButton.setOnClickListener(View.OnClickListener {
            val msgText: String = ("" +
                    "Register on SportsFight application with referral code " +
                    "*"+userInfo!!.referalCode+"*"+
                    " and get Rs 100 Bonus on Joining.\n" +
                    " Click on " +
                    BindingUtils.BILTY_APK_LINK)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, msgText)
            shareIntent.type = "text/plain"


            startActivity(Intent.createChooser(shareIntent,"Refer and Earn Rs 100"))
        })
        customeProgressDialog = CustomeProgressDialog(this)

        mBinding!!.transactionHistoryRecycler.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        var itemDecoration = DividerItemDecoration(this, LinearLayout.VERTICAL);
        mBinding!!.transactionHistoryRecycler.addItemDecoration(itemDecoration)
        adapter = RefferalsListAdaptors(this, checkinArrayList)
        mBinding!!.transactionHistoryRecycler.adapter = adapter
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        getMyRefferralsFriends()
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {

    }

    fun getMyRefferralsFriends() {
        mBinding!!.emptyViewRefferal.visibility=View.GONE
        mBinding!!.progressBar.visibility = View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).myRefferalsList(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.emptyViewRefferal.visibility=View.VISIBLE
                    mBinding!!.progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.progressBar.visibility = View.GONE
                    var res = response!!.body()
                    if(res!=null) {
                        var responseModel = res.referalUserList
                        if(responseModel!=null) {
                            if (responseModel!!.size > 0) {

                                checkinArrayList.addAll(responseModel!!)
                                adapter!!.notifyDataSetChanged()
                                mBinding!!.emptyViewRefferal.visibility=View.GONE

                            }else {
                                mBinding!!.emptyViewRefferal.visibility=View.VISIBLE
                            }
                        }else {
                            mBinding!!.emptyViewRefferal.visibility=View.VISIBLE
                        }

                    }

                }

            })

    }


    inner class RefferalsListAdaptors(
        val context: Context,
        val tradeinfoModels: ArrayList<RefferalUsersModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((TransactionModel) -> Unit)? = null
        private var optionListObject = tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_refferal_friends, parent, false)
            return DataViewHolder(view)

        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = optionListObject!![viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder?.transactionDate?.text = objectVal.created_at
            viewHolder?.friendName?.text = objectVal.name
            viewHolder?.earnedAmount?.text = "₹"+objectVal.referral_amount
        }


        override fun getItemCount(): Int {
            return optionListObject!!.size
        }


        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val transactionDate = itemView.findViewById<TextView>(R.id.transaction_date)
            val friendName = itemView.findViewById<TextView>(R.id.friend_name)
            val earnedAmount = itemView.findViewById<TextView>(R.id.earned_amount)
        }


    }
}
