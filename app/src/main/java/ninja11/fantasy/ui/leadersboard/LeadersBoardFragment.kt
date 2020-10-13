package ninja11.fantasy.ui.leadersboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import ninja11.fantasy.CreateTeamActivity
import ninja11.fantasy.R
import ninja11.fantasy.TeamPreviewActivity
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.leadersboard.models.LeadersBoardModels
import ninja11.fantasy.databinding.FragmentLeadersBoardBinding
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LeadersBoardFragment(
    objectMatches: UpcomingMatchesModel,
    val contestObject: ContestModelLists
) : Fragment() {
    //private var isMatchStarted: Boolean?=false
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var teamName: String?=""
    var matchObject = objectMatches
    private var mBinding: FragmentLeadersBoardBinding? = null
    var adapter: LeadersBoardAdapter?=null
    var leadersBoardList = ArrayList<LeadersBoardModels>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_leaders_board, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.prizeLeadersboardRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.prizeLeadersboardRecycler.getContext(),
            RecyclerView.VERTICAL
        )
        mBinding!!.prizeLeadersboardRecycler.addItemDecoration(dividerItemDecoration)

        adapter = LeadersBoardAdapter(activity!!, leadersBoardList)
        mBinding!!.prizeLeadersboardRecycler.adapter = adapter
        adapter!!.onItemClick = { objects ->
            teamName = String.format("%s(%s)",objects.userInfo!!.fullName,objects.teamName)
            if(objects.userId == MyPreferences.getUserID(activity!!)){
                getPoints(objects.teamId)
            }else
            if(matchObject.status!=BindingUtils.MATCH_STATUS_UPCOMING) {
                getPoints(objects.teamId)
            }else {
                MyUtils.showToast(activity!! as AppCompatActivity,"You cannot see other players teams, until match started")
            }

        }
        mBinding!!.swipeRefreshLeaderboard.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getLeadersBoards()
        })
        setTotalTeamCounts(0)
        getLeadersBoards()

    }


    private fun setTotalTeamCounts(value:Int) {
        mBinding!!.totalTeamCounts.setText(String.format("ALL TEAMS (%d)",value))
    }

    fun getPoints(teamId: Int) {
        customeProgressDialog.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token =MyPreferences.getToken(activity!!)!!
        models.contest_id =""+contestObject!!.id
        models.match_id =""+matchObject.matchId
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
                        //var totalPoints = res.totalPoints
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
                            intent.putExtra(TeamPreviewActivity.KEY_TEAM_NAME,teamName)
                            intent.putExtra(TeamPreviewActivity.KEY_TEAM_ID,teamId)
                            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
                            intent.putExtra(TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY,hasmapPlayers)
                            startActivity(intent)
                        }

                    }

                }

            })

    }


    fun getLeadersBoards() {
       // (activity as LeadersBoardActivity).updateScores()
       // customeProgressDialog.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        //models.token =MyPreferences.getToken(activity!!)!!
        models.contest_id =""+contestObject!!.id
        models.match_id =""+matchObject.matchId
        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getLeaderBoard(models)
            .enqueue(object : Callback<UsersPostDBResponse?>{
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                   // MyUtils.showToast(activity!!.getWindow().getDecorView().getRootView(),t!!.localizedMessage)
                    //customeProgressDialog.dismiss()
                    mBinding!!.swipeRefreshLeaderboard.isRefreshing = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(!isVisible){
                        return
                    }
                    mBinding!!.swipeRefreshLeaderboard.isRefreshing = false
                    //customeProgressDialog.dismiss()
                    var res = response!!.body()
                    if(res!=null) {
                        var responseModel = res.leaderBoardList
                        if(responseModel!=null){
                            if(responseModel!=null){
                                if(responseModel!!.size>0) {
                                    leadersBoardList.clear()
                                    leadersBoardList.addAll(responseModel!!)
                                    adapter!!.notifyDataSetChanged()
                                    setTotalTeamCounts(responseModel!!.size)
                                }
                            }
                        }


                    }

                }

            })

    }


    inner class LeadersBoardAdapter(val context: Context, rangeModels: ArrayList<LeadersBoardModels>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((LeadersBoardModels) -> Unit)? = null
        private var matchesListObject =  rangeModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.leaders_board_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = matchesListObject!![viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.teamName.setText(String.format("%s(%s)",objectVal.userInfo!!.teamName,objectVal.teamName))
            viewHolder.userPoints.setText(objectVal.teamPoints)
            viewHolder.playeRanks.setText(objectVal.teamRanks)
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
                               "Won ₹%s",
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

            if(!TextUtils.isEmpty(objectVal.userInfo!!.profileImage)) {
                Glide.with(context)
                    .load(objectVal.userInfo!!.profileImage)
                    .placeholder(R.drawable.placeholder_player_teama)
                    .into(viewHolder.profileImage)
            }else {
                viewHolder.profileImage.setImageResource(R.drawable.placeholder_player_teama)
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
            val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
            val teamName = itemView.findViewById<TextView>(R.id.team_name)
            val userPoints = itemView.findViewById<TextView>(R.id.points)
            val playeRanks = itemView.findViewById<TextView>(R.id.player_rank)
            val teamWonStatus = itemView.findViewById<TextView>(R.id.team_won_status)
            val imgMatchStatus = itemView.findViewById<ImageView>(R.id.match_status)


        }


    }


}
