package ninja11.fantasy.ui.myaccounts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.R
import ninja11.fantasy.SportsFightApplication
import ninja11.fantasy.models.TransactionModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.dashboard.MyAccountFragment
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.databinding.FragmentTransactionHistoryBinding


class TransactionFragment(myAccountFragment: MyAccountFragment) : Fragment() {

    private lateinit var adapter: TransactionAdaptor
    private var mBinding: FragmentTransactionHistoryBinding? = null
    var transactionList = ArrayList<TransactionModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_transaction_history, container, false)

        return mBinding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var transactionHistory =
            (activity!!.applicationContext as SportsFightApplication).getTransactionHistory
        if(transactionHistory!=null && transactionHistory.size>0){
            transactionList.clear()
            transactionList.addAll(transactionHistory)
        }
        adapter = TransactionAdaptor(activity!!, transactionList)

        mBinding!!.recyclerView.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.recyclerView.getContext(),
            RecyclerView.VERTICAL
        )
        mBinding!!.recyclerView.addItemDecoration(dividerItemDecoration)

        mBinding!!.recyclerView.adapter = adapter
        getMyTransaction()
    }

    fun getMyTransaction() {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        //var userInfo = (activity as PlugSportsApplication).userInformations
        mBinding!!.progressBarTransaction.visibility = View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getTransactionHistory(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if(isAdded()) {
                        mBinding!!.progressBarTransaction.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(!isVisible){
                        return
                    }
                    mBinding!!.progressBarTransaction.visibility = View.GONE
                    var res = response!!.body()
                    if(res!=null) {
                        mBinding!!.progressBarTransaction.visibility = View.GONE
                        var responseModel = res.transactionHistory
                        if(responseModel!=null) {
                            if (responseModel!!.transactionList != null && responseModel!!.transactionList!!.size > 0) {
                                (activity!!.applicationContext as SportsFightApplication).saveTransactionHistory(responseModel!!.transactionList!!)
                                transactionList.addAll(responseModel!!.transactionList!!)
                                adapter.notifyDataSetChanged()

                            }
                        }

                    }

                }

            })

    }


    inner class TransactionAdaptor(
        val context: Context,
        val tradeinfoModels: ArrayList<TransactionModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((TransactionModel) -> Unit)? = null
        private var optionListObject = tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_recent_transactions, parent, false)
            return DataViewHolder(view)

        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = optionListObject!![viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder

            viewHolder?.transactionNote?.text = objectVal.paymentType
            viewHolder?.transactionId?.text = objectVal.transactionId
            viewHolder?.transactionDate?.text = objectVal.createdDate

             if(objectVal.debitCreditStatus.equals("+")){
                viewHolder?.transactionAmount.setTextColor(activity!!.resources.getColor(R.color.white))
                 viewHolder?.transactionAmount?.text = objectVal.debitCreditStatus+"₹"+objectVal.depositAmount
             }else {
                 viewHolder?.transactionAmount.setTextColor(activity!!.resources.getColor(R.color.white))
                 viewHolder?.transactionAmount?.text = objectVal.debitCreditStatus+"₹"+objectVal.depositAmount
             }

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

            val transactionNote = itemView.findViewById<TextView>(R.id.transaction_note)
            val transactionId = itemView.findViewById<TextView>(R.id.transaction_id)
            val transactionDate = itemView.findViewById<TextView>(R.id.transaction_date)
            val transactionAmount = itemView.findViewById<TextView>(R.id.transaction_amount)
        }


    }

}
