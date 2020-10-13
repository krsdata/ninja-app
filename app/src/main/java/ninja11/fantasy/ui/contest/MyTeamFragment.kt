package ninja11.fantasy.ui.contest

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import ninja11.fantasy.CreateTeamActivity
import ninja11.fantasy.R
import ninja11.fantasy.TeamPreviewActivity
import ninja11.fantasy.listener.OnContestLoadedListener
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.databinding.FragmentMyTeamBinding

class MyTeamFragment(
    val matchObject: UpcomingMatchesModel?
) : Fragment() {

    companion object {
        val SERIALIZABLE_EDIT_TEAM: String = "editteam"
        val SERIALIZABLE_COPY: String = "copyTeam"
    }

    private var teamName: String?=""
    private lateinit var mListener: OnContestLoadedListener
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var mBinding: FragmentMyTeamBinding? = null
    lateinit var adapter: MyTeamAdapter
    var myTeamArrayList = ArrayList<MyTeamModels>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_my_team, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.recyclerMyTeam.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        adapter = MyTeamAdapter(activity!!,myTeamArrayList)
        mBinding!!.recyclerMyTeam.adapter = adapter
        mBinding!!.linearEmptyContest.visibility=View.GONE

        adapter!!.onItemClick = { objects ->
            teamName = objects.teamName
            getPoints(objects.teamId!!.teamId)

        }
        mBinding!!.btnCreateTeam.setOnClickListener {
            val intent = Intent(activity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
            startActivity(intent)
        }
        mBinding!!.myteamRefresh.setOnRefreshListener {
             getMyTeam()
         }
    }

    override fun onResume() {
        super.onResume()
        getMyTeam()
    }

    private fun getPoints(teamId: Int) {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
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
                    val res = response!!.body()
                    if(res!=null) {
                        var totalPoints = res.totalPoints
                        val responseModel = res.responseObject
                        if(responseModel!=null){
                            val playerPointsList = responseModel!!.playerPointsList
                            val hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>> = HashMap()

                            val wktKeeperList:ArrayList<PlayersInfoModel> = ArrayList()
                            val batsManList:ArrayList<PlayersInfoModel> = ArrayList()
                            val allRounderList:ArrayList<PlayersInfoModel> = ArrayList()
                            val allbowlerList:ArrayList<PlayersInfoModel> = ArrayList()
                            for(x in 0 until playerPointsList!!.size){
                                var plyObj = playerPointsList[x]
                                if(plyObj.playerRole == "wk"){
                                    wktKeeperList.add(plyObj)
                                }else if(plyObj.playerRole == "bat"){
                                    batsManList.add(plyObj)
                                }else if(plyObj.playerRole == "all"){
                                    allRounderList.add(plyObj)
                                }else if(plyObj.playerRole == "bowl"){
                                    allbowlerList.add(plyObj)
                                }
                            }
                            hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER] =
                                wktKeeperList
                            hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN] = batsManList
                            hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER] =
                                allRounderList
                            hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER] = allbowlerList

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
        models.match_id =""+matchObject!!.matchId

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getMyTeam(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.myteamRefresh.isRefreshing=false
                    MyUtils.showToast(activity!! as AppCompatActivity,t!!.localizedMessage)
                    mBinding!!.progressMyteam.visibility = View.GONE
                    updateEmptyViews()
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
                        if(responseModel!=null) {
                            if (responseModel!!.myTeamList != null && responseModel!!.myTeamList!!.size > 0) {
                                myTeamArrayList.clear()
                                myTeamArrayList.addAll(responseModel!!.myTeamList!!)
                                adapter!!.notifyDataSetChanged()
                                mListener.onMyTeam(myTeamArrayList)
                            }
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
           // matchObject!!.status =1
            if(matchObject!!.status== BindingUtils.MATCH_STATUS_UPCOMING) {
                //viewHolder.linearTeamCountViews.visibility = View.VISIBLE
                viewHolder.linearPointViews.visibility = View.GONE
                viewHolder.teamEdit.visibility = View.VISIBLE
                viewHolder.teamCopy.visibility = View.VISIBLE
                viewHolder.teamEdit.setOnClickListener(View.OnClickListener {
                    val intent = Intent(activity, CreateTeamActivity::class.java)
                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
                    intent.putExtra(SERIALIZABLE_EDIT_TEAM,objectVal)
                    activity!!.startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
                })

                viewHolder.teamCopy.setOnClickListener { //copyTeam(objectVal.teamId)
                    val intent = Intent(activity, CreateTeamActivity::class.java)
                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY,matchObject)
                    intent.putExtra(SERIALIZABLE_EDIT_TEAM,objectVal)
                    intent.putExtra(SERIALIZABLE_COPY,true)
                    activity!!.startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
                }
            }else {
             //   viewHolder.linearTeamCountViews.visibility = View.GONE
                viewHolder.linearPointViews.visibility = View.VISIBLE
                viewHolder.teamCopy.visibility = View.GONE
                viewHolder.teamEdit.visibility = View.GONE
                viewHolder.myteamPoints.setText(objectVal.teamPoints)

            }



        }



        fun copyTeam(teamid: MyTeamModels.MyTeamId?) {
            //var userInfo = (activity as PlugSportsApplication).userInformations
            customeProgressDialog.show()
            var models = RequestModel()
            models.user_id = MyPreferences.getUserID(activity!!)!!
            models.match_id =""+matchObject!!.matchId
            models.team_id = teamid!!.teamId

            WebServiceClient(activity!!).client.create(IApiMethod::class.java).copyTeam(models)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                        if(isVisible){
                            mBinding!!.myteamRefresh.isRefreshing=false
                            MyUtils.showToast(activity!! as AppCompatActivity,t!!.localizedMessage)
                            mBinding!!.progressMyteam.visibility = View.GONE
                            updateEmptyViews()
                        }

                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {

                        if(isVisible){
                            customeProgressDialog.dismiss()
                            getMyTeam()
                        }
                    }

                })

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

            val myteamPoints = itemView.findViewById<TextView>(R.id.myteam_points)
            val linearPointViews = itemView.findViewById<LinearLayout>(R.id.linear_point_views)
           // val linearTeamCountViews = itemView.findViewById<LinearLayout>(R.id.linear_team_count_view)


        }


    }



}
