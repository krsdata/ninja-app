package ninja11.fantasy.ui.leadersboard

import android.content.Context
import android.content.Intent
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import ninja11.fantasy.CreateTeamActivity
import ninja11.fantasy.R
import ninja11.fantasy.listener.OnContestLoadedListener
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.databinding.FragmentMyTeamBinding
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContestStatsFragment(objectMatches: UpcomingMatchesModel) : Fragment() {

    companion object {
        val SERIALIZABLE_EDIT_TEAM: String = "editteam"
    }

    private lateinit var mListener: OnContestLoadedListener
    var matchObject = objectMatches
    private var mBinding: FragmentMyTeamBinding? = null
    lateinit var adapter: MyTeamAdapter
    var myTeamArrayList = ArrayList<MyTeamModels>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_team, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.recyclerMyTeam.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        adapter = MyTeamAdapter(activity!!,myTeamArrayList)
        mBinding!!.recyclerMyTeam.adapter = adapter
        mBinding!!.linearEmptyContest.visibility=View.GONE

        mBinding!!.btnCreateTeam.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
            startActivity(intent)
        })
         mBinding!!.myteamRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
             getMyTeam()
         })
        getMyTeam()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context as OnContestLoadedListener
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }

    }

    fun getMyTeam() {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        //var userInfo = (activity as PlugSportsApplication).userInformations
        mBinding!!.linearEmptyContest.visibility=View.GONE
        mBinding!!.progressMyteam.visibility = View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token = MyPreferences.getToken(activity!!)!!
        models.match_id =""+matchObject.matchId

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getMyTeam(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                  if(isVisible) {
                      mBinding!!.myteamRefresh.isRefreshing = false
                      mBinding!!.progressMyteam.visibility = View.GONE
                      updateEmptyViews()
                  }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.myteamRefresh.isRefreshing=false
                    mBinding!!.progressMyteam.visibility = View.GONE
                    var res = response!!.body()
                    if(res!=null) {
                        var responseModel = res.responseObject

                        if(responseModel!!.myTeamList!!.size>0) {
                            myTeamArrayList.clear()
                            myTeamArrayList.addAll(responseModel!!.myTeamList!!)
                            adapter!!.notifyDataSetChanged()
                            mListener.onMyTeam(myTeamArrayList)
                        }
                    }
                    updateEmptyViews()
                }

            })

    }

    fun updateEmptyViews(){
        if(myTeamArrayList.size==0){
            mBinding!!.linearEmptyContest.visibility=View.VISIBLE
        }else {
            mBinding!!.linearEmptyContest.visibility=View.GONE
        }
    }
    inner class MyTeamAdapter(val context: Context,tradeinfoModels: ArrayList<MyTeamModels>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((MyTeamModels) -> Unit)? = null
        private var matchesListObject =  tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.myteam_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.userTeamName.setText(objectVal.teamName)
            viewHolder.teamaName.setText(objectVal.teamsInfo!!.get(0).teamName)
            viewHolder.teambName.setText(objectVal.teamsInfo!!.get(1).teamName)

            viewHolder.teamaCount.setText(""+objectVal.teamsInfo!!.get(0).count)
            viewHolder.teambCount.setText(""+objectVal.teamsInfo!!.get(1).count)

            viewHolder.trumpPlayerName.setText(objectVal.trump!!.playerName)
            viewHolder.captainPlayerName.setText(objectVal.captain!!.playerName)
            viewHolder.vcPlayerName.setText(objectVal.viceCaptain!!.playerName)

            viewHolder.countWicketkeeper.setText(String.format("%d",objectVal.wicketKeepers!!.size))
            viewHolder.countBatsman.setText(String.format("%d",objectVal.batsmen!!.size))
            viewHolder.countAllRounder.setText(String.format("%d",objectVal.allRounders!!.size))
            viewHolder.countBowler.setText(String.format("%d",objectVal.bowlers!!.size))


            Glide.with(context)
                .load("https://")
                .placeholder(R.drawable.player_blue)
                .into(viewHolder?.trumpImageView)

            Glide.with(context)
                .load("https://")
                .placeholder(R.drawable.player_blue)
                .into(viewHolder?.captainImageView)

            Glide.with(context)
                .load("https://")
                .placeholder(R.drawable.player_blue)
                .into(viewHolder?.vcImageView)

            viewHolder.teamEdit.setOnClickListener(View.OnClickListener {
                val intent = Intent(activity, CreateTeamActivity::class.java)
                intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
                intent.putExtra(SERIALIZABLE_EDIT_TEAM,objectVal)
                activity!!.startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
            })


        }



        override fun getItemCount(): Int {
            return matchesListObject!!.size
        }

        inner  class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val userTeamName = itemView.findViewById<TextView>(R.id.user_team_name)
            val teamEdit = itemView.findViewById<ImageView>(R.id.team_edit)
            val teamCopy = itemView.findViewById<ImageView>(R.id.team_copy)
            val teamShare = itemView.findViewById<ImageView>(R.id.team_share)

            val teamaName = itemView.findViewById<TextView>(R.id.teama_name)
            val teamaCount = itemView.findViewById<TextView>(R.id.teama_count)
            val teambName = itemView.findViewById<TextView>(R.id.teamb_name)
            val teambCount = itemView.findViewById<TextView>(R.id.teamb_count)

            val trumpImageView = itemView.findViewById<ImageView>(R.id.trump_imageView)
            val trumpPlayerName = itemView.findViewById<TextView>(R.id.trump_player_name)

            val captainImageView = itemView.findViewById<ImageView>(R.id.captain_imageView)
            val captainPlayerName = itemView.findViewById<TextView>(R.id.captain_player_name)

            val vcImageView = itemView.findViewById<ImageView>(R.id.vc_imageView)
            val vcPlayerName = itemView.findViewById<TextView>(R.id.vc_player_name)

            val countWicketkeeper = itemView.findViewById<TextView>(R.id.count_wicketkeeper)
            val countBatsman = itemView.findViewById<TextView>(R.id.count_batsman)
            val countAllRounder = itemView.findViewById<TextView>(R.id.count_allrounder)
            val countBowler = itemView.findViewById<TextView>(R.id.count_bowler)


        }


    }



}
