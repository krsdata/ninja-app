package ninja11.fantasy.ui.mymatches

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ninja11.fantasy.ContestActivity
import ninja11.fantasy.MainActivity
import ninja11.fantasy.R
import ninja11.fantasy.models.JoinedMatchModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.databinding.FragmentMyCompletedBinding
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MyCompletedMatchesFragment : Fragment() {

    private var mBinding: FragmentMyCompletedBinding? = null
    lateinit var adapter: MyMatchesAdapter
    var checkinArrayList = ArrayList<JoinedMatchModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_completed, container, false)

        return mBinding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.recyclerMyUpcoming.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        adapter = MyMatchesAdapter(activity!!,checkinArrayList)
        mBinding!!.recyclerMyUpcoming.adapter = adapter
        adapter.onItemClick = { objects ->
            val intent = Intent(activity!!, ContestActivity::class.java)
            intent.putExtra(ContestActivity.SERIALIZABLE_KEY_JOINED_CONTEST,objects)
            activity!!.startActivity(intent)
        }
        if(checkinArrayList.size>0){
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }else {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }

        mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
            (activity as MainActivity).viewUpcomingMatches()
        })

        getMatchHistory()
    }
    fun getMatchHistory() {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        if(checkinArrayList.size==0) {
            mBinding!!.progressBar.visibility = View.VISIBLE
        }
        mBinding!!.linearEmptyContest.visibility=View.GONE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        models.action_type ="completed"


        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getMatchHistory(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                    if(isAdded) {
                        updateEmptyViews()
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(isAdded) {
                        mBinding!!.progressBar.visibility = View.GONE
                        var res = response!!.body()
                        if(res!=null) {
                            var responseModel = res.responseObject
                            if(responseModel!=null) {
                                if (responseModel!!.matchdatalist != null && responseModel!!.matchdatalist!!.size > 0) {
                                    checkinArrayList.clear()
                                    checkinArrayList.addAll(responseModel!!.matchdatalist!!.get(0).completedMatchHistory!!)
                                    adapter!!.notifyDataSetChanged()
                                }
                            }
                        }
                        updateEmptyViews()
                    }



                }

            })

    }

    private fun updateEmptyViews() {
        if(checkinArrayList.size>0){
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }else {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }


    }
    inner class MyMatchesAdapter(
        val context: Context,
        val tradeinfoModels: ArrayList<JoinedMatchModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((JoinedMatchModel) -> Unit)? = null
        private var matchesListObject = tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.matches_row_completed, parent, false)
            return DataViewHolder(view)

        }

        fun getRandomColor():Int {
            val rnd = Random()
            val color: Int = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

            return color
        }
        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: MyMatchesAdapter.DataViewHolder = parent as MyMatchesAdapter.DataViewHolder
            viewHolder?.matchTitle?.text = objectVal.matchTitle
            viewHolder?.matchStatus?.text = ""+objectVal.statusString
           // viewHolder?.matchProgress.setText(objectVal.dateStart)
            // viewHolder?.matchProgress?.text = ""+objectVal.timestampEnd
            viewHolder?.opponent1?.text = objectVal.teamAInfo!!.teamShortName
            viewHolder?.opponent2?.text = objectVal.teamBInfo!!.teamShortName
            viewHolder?.winningPrice?.text = String.format(" ₹%s",objectVal.prizeAmount)

            viewHolder?.totalTeamCreated?.text = String.format("%d",objectVal.totalTeams)
            viewHolder?.totalContestJoined?.text = String.format("%d",objectVal.totalJoinContests)
            viewHolder?.date?.text = objectVal.dateStart


            viewHolder?.teamAColorView?.setBackgroundColor(getRandomColor())
            viewHolder?.teamBColorView?.setBackgroundColor(getRandomColor())

//            BindingUtils.countDownStart(objectVal.timestampStart,object : OnMatchTimerStarted {
//                override fun onTimeFinished() {
//                    viewHolder?.matchProgress.setText(objectVal.statusString)
//                }
//
//                override fun onTicks(time:String) {
//                    viewHolder?.matchProgress.setText(time)
//                }
//
//            })

            Glide.with(context)
                .load(objectVal.teamAInfo!!.logoUrl)
                .placeholder(R.drawable.flag_indian)
                .into(viewHolder?.teamALogo)


            Glide.with(context)
                .load(objectVal.teamBInfo!!.logoUrl)
                .placeholder(R.drawable.flag_indian)
                .into(viewHolder?.teamBLogo)



        }


        override fun getItemCount(): Int {
            return matchesListObject!!.size
        }


        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject!![adapterPosition])
                }
            }

            val teamALogo = itemView.findViewById<ImageView>(R.id.teama_logo)
            val teamBLogo = itemView.findViewById<ImageView>(R.id.teamb_logo)
            val matchTitle = itemView.findViewById<TextView>(R.id.completed_match_title)
            val matchStatus = itemView.findViewById<TextView>(R.id.completed_match_status)
            val teamAColorView = itemView.findViewById<View>(R.id.countrycolorview)
            val teamBColorView = itemView.findViewById<View>(R.id.countrybcolorview)
            val opponent1 = itemView.findViewById<TextView>(R.id.upcoming_opponent1)
            val opponent2 = itemView.findViewById<TextView>(R.id.upcoming_opponent2)
            val winningPrice = itemView.findViewById<TextView>(R.id.winning_price)
             val  date = itemView.findViewById<TextView>(R.id.date)

            val totalTeamCreated = itemView.findViewById<TextView>(R.id.total_team_created)
            val totalContestJoined = itemView.findViewById<TextView>(R.id.total_contest_joined)


        }
    }
}
