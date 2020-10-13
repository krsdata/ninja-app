package ninja11.fantasy.ui.leadersboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.R
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.leadersboard.models.PrizeBreakUpModels
import ninja11.fantasy.databinding.FragmentPrizeBreakupBinding
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PrizeBreakupFragment(
    objectMatches: UpcomingMatchesModel,
    val contestObject: ContestModelLists
) : Fragment(){

    var matchObject = objectMatches
    private var mBinding: FragmentPrizeBreakupBinding? = null
    lateinit var adapter: PrizeBreakUpAdapter
    var prizeBreakupList = ArrayList<PrizeBreakUpModels>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_prize_breakup, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.prizeViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        adapter = PrizeBreakUpAdapter(
            activity!!,
            prizeBreakupList
        )
        mBinding!!.prizeViewRecycler.adapter = adapter
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        getPrizeBreakup()
    }



    fun getPrizeBreakup() {
        mBinding!!.progressBar.visibility = View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
       // models.token =MyPreferences.getToken(activity!!)!!
        models.match_id =""+matchObject.matchId
        models.contest_id =""+contestObject.id
        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getPrizeBreakUp(models)
            .enqueue(object : Callback<UsersPostDBResponse?>{
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    var res = response!!.body()
                    if(res!=null) {
                        if(isVisible) {
                            mBinding!!.progressBar.visibility = View.GONE
                            var responseModel = res.responseObject
                            if (responseModel!!.prizeBreakUpModelsList!!.size > 0) {
                                prizeBreakupList.clear()

                                prizeBreakupList.addAll(responseModel!!.prizeBreakUpModelsList!!)
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }

                }

            })

    }


    inner class PrizeBreakUpAdapter(val context: Context,rangeModels: ArrayList<PrizeBreakUpModels>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((PrizeBreakUpModels) -> Unit)? = null
        private var matchesListObject =  rangeModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.prize_breakup_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.rankRange.setText(objectVal.rangeName)
            viewHolder.winnerPrize.setText("₹"+objectVal.winnersPrice)
        }



        override fun getItemCount(): Int {
            return matchesListObject!!.size
        }

        inner  class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val rankRange = itemView.findViewById<TextView>(R.id.rank_range)
            val winnerPrize = itemView.findViewById<TextView>(R.id.winner_rpize)


        }


    }


}
