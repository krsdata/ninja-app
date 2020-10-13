package ninja11.fantasy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.CreateTeamActivity.Companion.CREATE_TEAM_ALLROUNDER
import ninja11.fantasy.CreateTeamActivity.Companion.CREATE_TEAM_BATSMAN
import ninja11.fantasy.CreateTeamActivity.Companion.CREATE_TEAM_BOWLER
import ninja11.fantasy.CreateTeamActivity.Companion.CREATE_TEAM_WICKET_KEEPER
import ninja11.fantasy.databinding.ActivityTeamPreviewBinding
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.previewteam.adaptors.GridViewAdapter
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TeamPreviewActivity : AppCompatActivity() {
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var teamId: Int = 0
    private var teamName: String = ""
    private lateinit var matchObject: UpcomingMatchesModel
    private lateinit var hasmapPlayers: HashMap<String, java.util.ArrayList<PlayersInfoModel>>
    private var mBinding: ActivityTeamPreviewBinding? = null
    val listWicketKeeper = ArrayList<PlayersInfoModel>()
    val listBatsMan = ArrayList<PlayersInfoModel>()
    val listAllRounder = ArrayList<PlayersInfoModel>()
    val listBowler = ArrayList<PlayersInfoModel>()

    companion object {
        val SERIALIZABLE_TEAM_PREVIEW_KEY: String = "teampreview"
        val KEY_TEAM_NAME: String = "team_name"
        val KEY_TEAM_ID: String = "team_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_team_preview
        )
        /*    val w: Window = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )*/

        customeProgressDialog = CustomeProgressDialog(this)
        if (intent.hasExtra(KEY_TEAM_NAME)) {
            teamName = intent.getStringExtra(KEY_TEAM_NAME)
        }
        if (intent.hasExtra(KEY_TEAM_ID)) {
            teamId = intent.getIntExtra(KEY_TEAM_ID, 0)
        }
        matchObject =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel
        hasmapPlayers =
            intent.getSerializableExtra(SERIALIZABLE_TEAM_PREVIEW_KEY) as HashMap<String, ArrayList<PlayersInfoModel>>
        mBinding!!.imgRefresh.setOnClickListener {
            getPoints(teamId)
        }

        mBinding!!.imgClose.setOnClickListener {
            finish()
        }

        mBinding!!.fantasyPointsWebsview.setOnClickListener {
            val intent = Intent(this@TeamPreviewActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
            startActivity(intent)
        }

        mBinding!!.teamName.text = teamName
        if (matchObject.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.pointsBar.visibility = View.GONE
            mBinding!!.imgRefresh.visibility = View.GONE
        } else {
            mBinding!!.pointsBar.visibility = View.VISIBLE
            mBinding!!.imgRefresh.visibility = View.VISIBLE
        }
        setupPlayersOnGrounds()
    }

    private fun setupPlayersOnGrounds() {
        mBinding!!.totalPointsValue.text = calculatePoints()
        addWicketKeeper()
        addBatsman()
        addAllRounder()
        addBowler()

        val gridViewAdapterWicket =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listWicketKeeper,
                matchObject
            )
        mBinding!!.gridWicketKeeper.numColumns = listWicketKeeper.size
        mBinding!!.gridWicketKeeper.adapter = gridViewAdapterWicket

        var sizeofColumn = 0
        val gridViewAdapterBatsMan =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listBatsMan,
                matchObject
            )

        if (listBatsMan.size > 4) {
            sizeofColumn = 4
        } else {
            sizeofColumn = listBatsMan.size
        }
        mBinding!!.gridBatsman.numColumns = sizeofColumn
        mBinding!!.gridBatsman.adapter = gridViewAdapterBatsMan


        val gridViewAdapterAllRounder =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listAllRounder,
                matchObject
            )
        if (listAllRounder.size > 2) {
            sizeofColumn = 2
        } else {
            sizeofColumn = listAllRounder.size
        }
        mBinding!!.gridAllRounders.numColumns = sizeofColumn
        mBinding!!.gridAllRounders.adapter = gridViewAdapterAllRounder

        val gridViewAdapterBowler =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listBowler,
                matchObject
            )
        if (listBowler.size > 5) {
            sizeofColumn = 5
        } else {
            sizeofColumn = listBowler.size
        }
        mBinding!!.gridBowlers.numColumns = sizeofColumn
        mBinding!!.gridBowlers.adapter = gridViewAdapterBowler
        setGridViewOnItemClickListener()
    }

    private fun getPoints(teamId: Int) {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.team_id = teamId

        WebServiceClient(this).client.create(IApiMethod::class.java).getPoints(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    var res = response!!.body()
                    if (res != null) {
                        var totalPoints = res.totalPoints
                        val responseModel = res.responseObject
                        if (responseModel != null) {
                            val playerPointsList = responseModel!!.playerPointsList
                            val hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>> =
                                HashMap()

                            val wktKeeperList: ArrayList<PlayersInfoModel> =
                                ArrayList()
                            val batsManList: ArrayList<PlayersInfoModel> =
                                ArrayList()
                            val allRounderList: ArrayList<PlayersInfoModel> =
                                ArrayList()
                            val allbowlerList: ArrayList<PlayersInfoModel> =
                                ArrayList()

                            for (x in 0 until playerPointsList!!.size) {
                                val plyObj = playerPointsList[x]
                                if (plyObj.playerRole == "wk") {
                                    wktKeeperList.add(plyObj)
                                } else if (plyObj.playerRole == "bat") {
                                    batsManList.add(plyObj)
                                } else if (plyObj.playerRole == "all") {
                                    allRounderList.add(plyObj)
                                } else if (plyObj.playerRole == "bowl") {
                                    allbowlerList.add(plyObj)
                                }
                            }
                            hasmapPlayers[CREATE_TEAM_WICKET_KEEPER] = wktKeeperList
                            hasmapPlayers[CREATE_TEAM_BATSMAN] = batsManList
                            hasmapPlayers[CREATE_TEAM_ALLROUNDER] = allRounderList
                            hasmapPlayers[CREATE_TEAM_BOWLER] = allbowlerList
                            updatePlayersPoints(hasmapPlayers)
                        }

                    }

                }

            })

    }

    private fun updatePlayersPoints(hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>>) {
        this.hasmapPlayers.clear()
        this.hasmapPlayers = hasmapPlayers
        setupPlayersOnGrounds()
    }


    private fun calculatePoints(): String {
        var totalPoints: Double = 0.0
        if (hasmapPlayers.containsKey(CREATE_TEAM_WICKET_KEEPER)) {
            var lkeeper = hasmapPlayers[CREATE_TEAM_WICKET_KEEPER]
            for (x in 0 until lkeeper!!.size) {
                var obj = lkeeper[x]
                try {
                    totalPoints += obj.playerPoints.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (hasmapPlayers.containsKey(CREATE_TEAM_BATSMAN)) {
            var btslist = hasmapPlayers[CREATE_TEAM_BATSMAN]
            for (x in 0 until btslist!!.size) {
                var obj = btslist[x]
                try {
                    totalPoints += obj.playerPoints.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (hasmapPlayers.containsKey(CREATE_TEAM_ALLROUNDER)) {
            var alllist = hasmapPlayers[CREATE_TEAM_ALLROUNDER]
            for (x in 0 until alllist!!.size) {
                var obj = alllist[x]
                try {
                    totalPoints += obj.playerPoints.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (hasmapPlayers.containsKey(CREATE_TEAM_BOWLER)) {
            var bwllist = hasmapPlayers[CREATE_TEAM_BOWLER]
            for (x in 0 until bwllist!!.size) {
                var obj = bwllist[x]
                try {
                    totalPoints += obj.playerPoints.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return totalPoints.toString()
    }

    private fun setGridViewOnItemClickListener() {
        mBinding!!.gridWicketKeeper.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridBatsman.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridAllRounders.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridBowlers.setOnItemClickListener { parent, view, position, id ->

        }
    }

    private fun addWicketKeeper() {
        listWicketKeeper.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_WICKET_KEEPER)) {
            listWicketKeeper.addAll(hasmapPlayers.get(CREATE_TEAM_WICKET_KEEPER)!!)
        }
        //listWicketKeeper.add("R Pant")

        //listImageURLs.addAll(listImageURLs)
    }

    private fun addBatsman() {
        listBatsMan.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_BATSMAN)) {
            listBatsMan.addAll(hasmapPlayers.get(CREATE_TEAM_BATSMAN)!!)
        }

        //listImageURLs.addAll(listImageURLs)
    }


    private fun addAllRounder() {
        listAllRounder.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_ALLROUNDER)) {
            listAllRounder.addAll(hasmapPlayers.get(CREATE_TEAM_ALLROUNDER)!!)
        }
    }

    private fun addBowler() {
        listBowler.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_BOWLER)) {
            listBowler.addAll(hasmapPlayers.get(CREATE_TEAM_BOWLER)!!)
        }
        //listBowler.add("AY Patel")
        //listBowler.add("I Sharma")

        //listImageURLs.addAll(listImageURLs)
    }

}
