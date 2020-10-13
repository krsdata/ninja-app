package ninja11.fantasy.ui.contest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ninja11.fantasy.*
import ninja11.fantasy.listener.OnContestEvents
import ninja11.fantasy.listener.OnContestLoadedListener
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.databinding.FragmentMyContestBinding
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyContestFragment(
    val objectMatches: UpcomingMatchesModel?
) : Fragment() {
    //private var isMatchStarted: Boolean= false
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private lateinit var matchObject: UpcomingMatchesModel
    private lateinit var mListener: OnContestLoadedListener
    var mContestListeners: OnContestEvents? =null
    private var mBinding: FragmentMyContestBinding? = null
    lateinit var adapter: MyContestAdapter
    var checkinArrayList = ArrayList<ContestModelLists>()
    private var teamName: String?=""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_contest, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
        if(objectMatches!=null) {
            matchObject = objectMatches
        }


        mBinding!!.recyclerMyContest.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        mBinding!!.linearEmptyContest.visibility=View.GONE
        adapter = MyContestAdapter(activity!!, checkinArrayList)
        mBinding!!.recyclerMyContest.adapter = adapter

        adapter.onItemClick= { objects ->
            val intent = Intent(context, LeadersBoardActivity::class.java)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objects)
            activity!!.startActivityForResult(intent, LeadersBoardActivity.CREATETEAM_REQUESTCODE)
        }
        mBinding!!.linearEmptyContest.visibility=View.GONE

        mBinding!!.btnJoinContest.setOnClickListener(View.OnClickListener {
            (activity as ContestActivity).changeTabsPositions(0)
        })

        mBinding!!.mycontestRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getMyJoinedContest()
        })

//        BindingUtils.countDownStart(matchObject!!.timestampStart,object:OnMatchTimerStarted{
//            override fun onTimeFinished() {
//                isMatchStarted =true
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onTicks(time: String) {
//                isMatchStarted =false
//            }
//
//        })
    }

    override fun onResume() {
        super.onResume()
        getMyJoinedContest()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context as OnContestLoadedListener
        }else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }

        if (context is OnContestEvents) {
            mContestListeners = context as OnContestEvents
        }else{
            throw RuntimeException(
                "$context must implement OnContestEvents"
            )
        }

    }

    fun getMyJoinedContest() {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        //var userInfo = (activity as PlugSportsApplication).userInformations
        mBinding!!.linearEmptyContest.visibility=View.GONE
        mBinding!!.loader.visibility = View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token = MyPreferences.getToken(activity!!)!!
        models.match_id =""+matchObject.matchId


        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getMyContest(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.mycontestRefresh.isRefreshing=false
                   // MyUtils.showToast(activity!!.getWindow().getDecorView().getRootView(),t!!.localizedMessage)
                    //updateEmptyViews()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.mycontestRefresh.isRefreshing=false
                    mBinding!!.loader.visibility = View.GONE
                    var res = response!!.body()
                    if(res!=null) {
                        var responseModel = res.responseObject
                        if(responseModel!=null) {
                            if (responseModel!!.myJoinedContest != null && responseModel!!.myJoinedContest!!.size > 0) {
                                checkinArrayList.clear()
                                checkinArrayList.addAll(responseModel!!.myJoinedContest!!)
                                mListener.onMyContest(checkinArrayList)
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                    updateEmptyViews()
                }

            })

    }

    fun updateEmptyViews(){
        if(checkinArrayList.size==0){
            mBinding!!.linearEmptyContest.visibility=View.VISIBLE
        }else {
            mBinding!!.linearEmptyContest.visibility=View.GONE
        }
    }



    inner class MyContestAdapter(
        val context: Activity,
        tradeinfoModels: ArrayList<ContestModelLists>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((ContestModelLists) -> Unit)? = null
        private var matchesListObject =  tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.mycontest_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.contestPrizePool.setText(String.format("₹%d",objectVal.totalWinningPrize))
            viewHolder.contestEntryPrize.setText(String.format("%d",objectVal.entryFees))
            if(objectVal.isCancelled) {
                viewHolder.contestInfo.setText("Cancelled")
                viewHolder.contestInfo.textSize = 18.0f
                viewHolder.contestInfo.setTextColor(Color.RED)
            }else {
                viewHolder.contestInfo.setText("Entry")
                viewHolder.contestInfo.setTextColor(Color.BLACK)
            }

            if(objectVal.totalSpots==0){
                viewHolder.contestProgressBar.max =objectVal.totalSpots +15
                viewHolder.contestProgressBar.progress =objectVal.filledSpots
                viewHolder?.totalSpots?.text = String.format("unlimited spots")
                viewHolder?.totalSpotLeft?.text = String.format("%d spot filled",objectVal.filledSpots)
            }else {
                viewHolder.contestProgressBar.max =objectVal.totalSpots
                viewHolder.contestProgressBar.progress =objectVal.filledSpots
                viewHolder?.totalSpots?.text = String.format("%d spots", objectVal.totalSpots)

                if(objectVal.totalSpots == objectVal.filledSpots){
                    viewHolder?.totalSpotLeft?.text = "Contest Full"
                    viewHolder?.totalSpotLeft?.setTextColor(Color.RED)
                }else {
                    viewHolder?.totalSpotLeft?.text =
                        String.format("%d spot left", objectVal.totalSpots - objectVal.filledSpots)
                }
            }


            viewHolder?.contestCancellation?.setOnClickListener(View.OnClickListener {
                //MyUtils.showToast(activity!!,""+objectVal.cancellation)
            })

            if(matchObject.status == BindingUtils.MATCH_STATUS_UPCOMING) {
                viewHolder?.contestEntryPrize?.setOnClickListener(View.OnClickListener {
                    mContestListeners!!.onContestJoinning(objectVal, viewType)

                })
            }else {
                viewHolder?.contestEntryPrize?.setBackgroundResource(R.drawable.button_selector_grey)
                viewHolder?.progressLinear?.visibility = View.GONE
            }
         //   viewHolder?.maxAllowedTeam?.text = String.format("%s %d %s","Upto",objectVal.winnerPercentage,"teams")
            viewHolder?.firstPrize?.text = String.format("%s%d","₹",objectVal.firstPrice)


            if(objectVal.joinedTeams!=null && objectVal.joinedTeams!!.size>0) {
                viewHolder?.recyclerTeamList.layoutManager =
                    LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                val dividerItemDecoration = DividerItemDecoration(
                    viewHolder?.recyclerTeamList.getContext(),
                    RecyclerView.VERTICAL
                )
                viewHolder?.recyclerTeamList.addItemDecoration(dividerItemDecoration)

                var adapterJoinTeamAapter =
                    MyContestJoinedTeamAdapter(activity!!, objectVal.joinedTeams!!)
                viewHolder?.recyclerTeamList.adapter = adapterJoinTeamAapter

                adapterJoinTeamAapter!!.onItemClick = { objects ->
                    teamName = objects.teamName
                    getPoints(objects.createdteamId)

                }
            }
        }



        override fun getItemCount(): Int {
            return matchesListObject!!.size
        }

        inner  class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject!![adapterPosition])
                }
            }
             val contestInfo = itemView.findViewById<TextView>(R.id.contest_info)
             val contestProgressBar = itemView.findViewById<ProgressBar>(R.id.contest_progress)
             val contestPrizePool = itemView.findViewById<TextView>(R.id.contest_prize_pool)

             val teamaName = itemView.findViewById<TextView>(R.id.teama_name)
             val progressLinear = itemView.findViewById<LinearLayout>(R.id.upcoming_linear_contest_view)
             val contestEntryPrize = itemView.findViewById<TextView>(R.id.contest_entry_prize)
             val totalSpotLeft = itemView.findViewById<TextView>(R.id.total_spot_left)
             val totalSpots = itemView.findViewById<TextView>(R.id.total_spot)

           // val maxAllowedTeam = itemView.findViewById<TextView>(R.id.max_allowed_team)
            val contestCancellation = itemView.findViewById<TextView>(R.id.contest_cancellation)
            val firstPrize = itemView.findViewById<TextView>(R.id.first_prize)
            val recyclerTeamList = itemView.findViewById<RecyclerView>(R.id.recycler_team_list)


        }


    }


    inner class MyContestJoinedTeamAdapter(
        val context: Activity,
        tradeinfoModels: ArrayList<MyTeamModels>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((MyTeamModels) -> Unit)? = null
        private var matchesListObject =  tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.mycontest_rows_teams, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.txtTeamName.setText(objectVal.teamName)
            if(!TextUtils.isEmpty(objectVal.teamWonStatus)) {
                if(Integer.parseInt(objectVal.teamWonStatus!!)>0){
                    if(matchObject.status==3){
                        viewHolder.teamWonStatus.setText(
                            String.format(
                                "Winning ₹%s",
                                objectVal.teamWonStatus
                            )
                        )
                    }else {
                        viewHolder.teamWonStatus.setText(
                            String.format(
                                "You Won ₹%s",
                                objectVal.teamWonStatus
                            )
                        )
                    }
                }else {
                    viewHolder.teamWonStatus.setText("")
                }

            }else {
                viewHolder.teamWonStatus.setText("")
            }
            viewHolder.teamPoints.setText(objectVal.teamPoints)
            viewHolder.teamRanks.setText("#"+objectVal.teamRanks)


        }
        override fun getItemCount(): Int {
            return matchesListObject!!.size
        }

        inner  class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject!![adapterPosition])
                }
            }
            val txtTeamName = itemView.findViewById<TextView>(R.id.team_name)
            val teamWonStatus = itemView.findViewById<TextView>(R.id.team_won_status)
            val teamPoints = itemView.findViewById<TextView>(R.id.team_points)
            val teamRanks = itemView.findViewById<TextView>(R.id.team_ranks)

        }


    }


    fun getPoints(teamId: Int) {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        customeProgressDialog.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
       // models.token =MyPreferences.getToken(activity!!)!!
        models.team_id =teamId

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getPoints(models)
            .enqueue(object : Callback<UsersPostDBResponse?>{
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    var res = response!!.body()
                    if(res!=null) {
                        var totalPoints = res.totalPoints
                        var responseModel = res.responseObject
                        if(responseModel!=null){
                            var playerPointsList = responseModel!!.playerPointsList
                            var hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>> = HashMap<String, ArrayList<PlayersInfoModel>>()

                            var wktKeeperList:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>();
                            var batsManList:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>();
                            var allRounderList:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>();
                            var allbowlerList:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>();

                            for(x in 0..playerPointsList!!.size-1){
                                var plyObj = playerPointsList.get(x)
                                if(plyObj.playerRole.equals("wk")){
                                    wktKeeperList.add(plyObj)
                                }else if(plyObj.playerRole.equals("bat")){
                                    batsManList.add(plyObj)
                                }else if(plyObj.playerRole.equals("all")){
                                    allRounderList.add(plyObj)
                                }else if(plyObj.playerRole.equals("bowl")){
                                    allbowlerList.add(plyObj)
                                }
                            }
                            hasmapPlayers.put(CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER,wktKeeperList)
                            hasmapPlayers.put(CreateTeamActivity.CREATE_TEAM_BATSMAN,batsManList)
                            hasmapPlayers.put(CreateTeamActivity.CREATE_TEAM_ALLROUNDER,allRounderList)
                            hasmapPlayers.put(CreateTeamActivity.CREATE_TEAM_BOWLER,allbowlerList)

                            val intent = Intent(activity, TeamPreviewActivity::class.java)
                            intent.putExtra(TeamPreviewActivity.KEY_TEAM_ID,teamId)
                            intent.putExtra(TeamPreviewActivity.KEY_TEAM_NAME,teamName)
                            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
                            intent.putExtra(TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY,hasmapPlayers)
                            startActivity(intent)
                        }

                    }

                }

            })

    }


}
