package ninja11.fantasy

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.databinding.ActivityNotificationListBinding
import ninja11.fantasy.models.NotifyModels
import ninja11.fantasy.models.WalletInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class NotificationListActivity : AppCompatActivity() {
    var allNotificationList = ArrayList<NotifyModels>()
    private lateinit var walletInfo: WalletInfo
    private var customeProgressDialog: CustomeProgressDialog?=null
    private var mBinding: ActivityNotificationListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_notification_list
        )

    /*    val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        walletInfo = (application as SportsFightApplication).walletInfo
        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        customeProgressDialog = CustomeProgressDialog(this)
        allNotificationList.add(NotifyModels())
        allNotificationList.add(NotifyModels())
        allNotificationList.add(NotifyModels())
        allNotificationList.add(NotifyModels())
        var adapter = NotificationListAdaptor(this, allNotificationList)
        mBinding!!.recyclerNotificationList.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        var itemDecoration = DividerItemDecoration(this, LinearLayout.VERTICAL);
        mBinding!!.recyclerNotificationList.addItemDecoration(itemDecoration)
        mBinding!!.recyclerNotificationList.adapter = adapter
        adapter.onItemClick = { objects ->

        }
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        getNotificationList()
    }

    fun getNotificationList() {
        customeProgressDialog!!.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        //models.token = MyPreferences.getToken(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).getNotification(models)
            .enqueue(object : Callback<NotifyModels?> {
                override fun onFailure(call: Call<NotifyModels?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<NotifyModels?>?,
                    response: Response<NotifyModels?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    var res = response!!.body()
                    if(res!=null) {
                       // var responseModel = res.walletObjects
                       // if(responseModel!=null) {
                            //(application as PlugSportsApplication).saveWalletInformation(responseModel)
                       // }
                    }

                }

            })

    }


    inner class NotificationListAdaptor(
        val context: Context,
        val tradeinfoModels: ArrayList<NotifyModels>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((NotifyModels) -> Unit)? = null
        private var optionListObject = tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_notification, parent, false)
            return DataViewHolder(view)

        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = optionListObject!![viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder?.notificationMessage?.text = objectVal.notificationMessages
            viewHolder?.notificationDates?.text = objectVal.activationDate

        }


        override fun getItemCount(): Int {
            return optionListObject!!.size
        }


        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(optionListObject!![adapterPosition])
                }
            }

            val notificationMessage = itemView.findViewById<TextView>(R.id.notification_message)
            val notificationDates = itemView.findViewById<TextView>(R.id.notification_dates)
        }


    }

}
