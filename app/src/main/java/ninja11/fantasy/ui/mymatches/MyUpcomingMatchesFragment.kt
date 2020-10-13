package ninja11.fantasy.ui.mymatches

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import ninja11.fantasy.listener.OnMatchTimerStarted
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.databinding.FragmentMyUpcomingBinding
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MyUpcomingMatchesFragment : Fragment() {

    private var mBinding: FragmentMyUpcomingBinding? = null
    lateinit var adapter: MyMatchesAdapter
    var checkinArrayList = ArrayList<UpcomingMatchesModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_upcoming, container, false
        )

        return mBinding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.recyclerMyUpcoming.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        //initDummyContent()

        adapter = MyMatchesAdapter(activity!!, checkinArrayList)
        mBinding!!.recyclerMyUpcoming.adapter = adapter
        adapter.onItemClick = { objects ->

            //MyUtils.logd("MatchesAdapter",objects.country1Name+" Vs "+objects.country1Name)
            val intent = Intent(activity!!, ContestActivity::class.java)
            intent.putExtra(ContestActivity.SERIALIZABLE_KEY_UPCOMING_MATCHES,objects)
            startActivity(intent)
        }
        if (checkinArrayList.size > 0) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        } else {
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
        models.action_type ="upcoming"


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
                                    checkinArrayList.addAll(responseModel!!.matchdatalist!!.get(0).upcomingMatchHistory!!)
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
        val tradeinfoModels: ArrayList<UpcomingMatchesModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((UpcomingMatchesModel) -> Unit)? = null
        private var matchesListObject = tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.matches_row_upcoming_inner, parent, false)
            return DataViewHolder(view)

        }

        fun getRandomColor():Int {
            val rnd = Random()
            val color: Int = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

            return color
        }
        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            if(objectVal.isLineup) {
                viewHolder?.matchTitle?.visibility = View.VISIBLE
            }else {
                viewHolder?.matchTitle?.visibility = View.GONE
            }
            viewHolder?.tournamentTitle?.text = objectVal.leagueTitle
            // viewHolder?.matchProgress?.text = ""+objectVal.timestampEnd
            viewHolder?.opponent1?.text = objectVal.teamAInfo!!.teamShortName
            viewHolder?.opponent2?.text = objectVal.teamBInfo!!.teamShortName

            if(!TextUtils.isEmpty(objectVal.dateStart)) {
                viewHolder?.matchtime.visibility = View.VISIBLE
                viewHolder?.matchtime.setText(objectVal.dateStart)
            }else {
                viewHolder?.matchtime.visibility = View.GONE
            }

            if(objectVal.freeContest) {
                viewHolder?.freeView?.visibility = View.VISIBLE
            }
            else {
                viewHolder?.freeView?.visibility = View.GONE
            }
            viewHolder?.teamAColorView?.setColorFilter(getRandomColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
            viewHolder?.teamBColorView?.setColorFilter(getRandomColor(), android.graphics.PorterDuff.Mode.MULTIPLY);

            BindingUtils.countDownStartForAdaptors(objectVal.timestampStart,object :
                OnMatchTimerStarted {
                override fun onTimeFinished() {
                    viewHolder?.matchProgress.setText(objectVal.statusString)
                }

                override fun onTicks(time:String) {
                    viewHolder?.matchProgress.setText(time)
                }

            })
            if (!TextUtils.isEmpty(objectVal.contestName)) {
                viewHolder?.upcomingLinearContestView.visibility = View.VISIBLE;
                viewHolder?.contestName?.text = "" + objectVal.contestName
                viewHolder?.contestPrice?.text = "" + objectVal.contestPrize
            } else {
                viewHolder?.upcomingLinearContestView.visibility = View.INVISIBLE;
            }

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
            val matchTitle = itemView.findViewById<TextView>(R.id.upcoming_match_title)
            val tournamentTitle = itemView.findViewById<TextView>(R.id.tournament_title)
            val teamAColorView = itemView.findViewById<ImageView>(R.id.countrycolorview)
            val teamBColorView = itemView.findViewById<ImageView>(R.id.countrybcolorview)
            val opponent1 = itemView.findViewById<TextView>(R.id.upcoming_opponent1)
            val opponent2 = itemView.findViewById<TextView>(R.id.upcoming_opponent2)
            val matchtime = itemView.findViewById<TextView>(R.id.match_time)
            val freeView = itemView.findViewById<TextView>(R.id.free_view)
            val matchProgress = itemView.findViewById<TextView>(R.id.upcoming_match_progress)
            val upcomingLinearContestView = itemView.findViewById<LinearLayout>(R.id.upcoming_linear_contest_view)
            val contestName = itemView.findViewById<TextView>(R.id.upcoming_contest_name)
            val contestPrice = itemView.findViewById<TextView>(R.id.upcoming_contest_price)



        }
    }
}
