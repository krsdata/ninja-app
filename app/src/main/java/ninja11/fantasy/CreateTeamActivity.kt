package ninja11.fantasy

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import ninja11.fantasy.listener.OnMatchTimerStarted
import ninja11.fantasy.listener.OnTeamCreateListener
import com.google.gson.Gson
import ninja11.fantasy.R
import ninja11.fantasy.databinding.ActivityCreateTeamBinding
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.PlayerModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.contest.MyTeamFragment
import ninja11.fantasy.ui.contest.MyTeamFragment.Companion.SERIALIZABLE_EDIT_TEAM
import ninja11.fantasy.ui.createteam.AllRounder
import ninja11.fantasy.ui.createteam.Batsman
import ninja11.fantasy.ui.createteam.Bowlers
import ninja11.fantasy.ui.createteam.WicketKeepers
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreateTeamActivity : BaseActivity(), OnTeamCreateListener {

    private var playersList: PlayerModels?= null

    // private var isMatchLive: Boolean = false
    var myTeamModel: MyTeamModels? = null
   // private var playersList: PlayerModels? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    var matchObject: UpcomingMatchesModel? = null
    var crateTeamHashMap: HashMap<String, ArrayList<PlayersInfoModel>> =
        HashMap<String, ArrayList<PlayersInfoModel>>()
    private var mBinding: ActivityCreateTeamBinding? = null

    companion object {
        var CREATETEAM_REQUESTCODE: Int = 2001

        val SERIALIZABLE_MATCH_KEY: String = "matchObject"
        val SERIALIZABLE_JOINED_MATCH_KEY: String = "joinedmatchObject"
        val SERIALIZABLE_CONTEST_KEY: String = "contest"
        val SERIALIZABLE_SELECTED_TEAMS: String = "selected_teams"
        val CREATE_TEAM_WICKET_KEEPER: String = "wk"
        val CREATE_TEAM_BATSMAN: String = "bat"
        val CREATE_TEAM_ALLROUNDER: String = "all"
        val CREATE_TEAM_BOWLER: String = "bow"

        var isEditMode: Boolean = false
        val MAX_WICKET_KEEPER: IntArray = intArrayOf(1, 4)
        val MAX_BATSMAN: IntArray = intArrayOf(3, 6)
        val MAX_ALL_ROUNDER: IntArray = intArrayOf(1, 4)
        val MAX_BOWLER: IntArray = intArrayOf(3, 6)
        val MAX_PLAYERS_CRICKET: Int = 11
        val MAX_PLAYERS_FROM_TEAM: Int = 7

        var TEAMA: Int = 0
        var TEAMB: Int = 0
        var teamAId = 0
        var teamBId = 0

        var COUNT_WICKET_KEEPER: Int = 0
        var COUNT_BATS_MAN: Int = 0
        var COUNT_ALL_ROUNDER: Int = 0
        var COUNT_BOWLER: Int = 0
        var isAllPlayersSelected: Boolean? = false
        var totalPlayers: Int = 0


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_create_team
        )

        /*val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        isEditMode = false
        ressetPlayers()
        updatePlayersCountBar(0)
        matchObject = intent.getSerializableExtra(SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel
        if (intent.hasExtra(SERIALIZABLE_EDIT_TEAM)) {
            isEditMode = true
            myTeamModel = intent.getSerializableExtra(SERIALIZABLE_EDIT_TEAM) as MyTeamModels
        }
        teamAId = matchObject!!.teamAInfo!!.teamId
        teamBId = matchObject!!.teamBInfo!!.teamId

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.teamPreview.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CreateTeamActivity, TeamPreviewActivity::class.java)
            intent.putExtra(SERIALIZABLE_MATCH_KEY, matchObject)
            intent.putExtra(TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY, crateTeamHashMap)
            startActivity(intent)
        })

        mBinding!!.teamContinue.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CreateTeamActivity, SaveTeamActivity::class.java)
            intent.putExtra(SERIALIZABLE_MATCH_KEY, matchObject)
            intent.putExtra(MyTeamFragment.SERIALIZABLE_COPY, getIntent().getBooleanExtra(MyTeamFragment.SERIALIZABLE_COPY,false))
            intent.putExtra(TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY, crateTeamHashMap)

            if (myTeamModel != null) {
                intent.putExtra(SERIALIZABLE_EDIT_TEAM, myTeamModel)
            }
            startActivityForResult(intent, CREATETEAM_REQUESTCODE)
        })

        mBinding!!.imgWallet.setOnClickListener {
            val intent = Intent(this@CreateTeamActivity, MyBalanceActivity::class.java)
            startActivity(intent)
        }

        mBinding!!.fantasyPoints.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@CreateTeamActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
            if (Build.VERSION.SDK_INT > 20) {
                val options =
                    ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
        })

        mBinding!!.clearAllPlayer.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this@CreateTeamActivity)
            //set title for alert dialog
            // builder.setTitle("Warning")
            //set message for alert dialog
            builder.setMessage("Do You want to clear all selected players ?")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder.setPositiveButton("OK"){
                    dialogInterface, which ->
                ressetPlayers()
                initViewPager()


            }
            builder.setNegativeButton("Cancel"){
                    dialogInterface, which ->

            }
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        })

        mBinding!!.teamaName.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teambName.text = matchObject!!.teamBInfo!!.teamShortName
        Glide.with(this)
            .load(matchObject!!.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(mBinding!!.teamaLogo)


        Glide.with(this)
            .load(matchObject!!.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(mBinding!!.teambLogo)

       if(MyUtils.isConnectedWithInternet(this)) {
           showLoading()
           getAllPlayers()
       }else {
           MyUtils.showToast(this,"No Internet connection found")
       }

    }


    fun updatePlayersCountBar(count: Int) {
        when (count) {
            0 -> {
                mBinding!!.clearAllPlayer.isEnabled = false
              //  mBinding!!.playerSelected1.setText("")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.bg_text)

             //   mBinding!!.playerSelected2.setText("")
               mBinding!!.playerSelected2.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected3.setText("")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.bg_text)

              //  mBinding!!.playerSelected4.setText("")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected5.setText("")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected6.setText("")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.bg_text)

              //  mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)

             //   mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)

              //  mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)

                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            1 -> {
                mBinding!!.clearAllPlayer.isEnabled = true
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)

              //  mBinding!!.playerSelected2.setText("")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.bg_text)

             //   mBinding!!.playerSelected3.setText("")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.bg_text)

            //    mBinding!!.playerSelected4.setText("")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.bg_text)

             //   mBinding!!.playerSelected5.setText("")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.bg_text)

              //  mBinding!!.playerSelected6.setText("")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.bg_text)

                //mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)

               // mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)

                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            2 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)

              //  mBinding!!.playerSelected3.setText("")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected4.setText("")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected5.setText("")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected6.setText("")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
                
             //   mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
                
            }
            3 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)

             //   mBinding!!.playerSelected4.setText("")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected5.setText("")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected6.setText("")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)
                
             //   mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            4 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)

              //  mBinding!!.playerSelected5.setText("")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected6.setText("")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.bg_text)
                
             //   mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)
                
             //   mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
                
            }
            5 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)

               // mBinding!!.playerSelected6.setText("")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)
               
              //  mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
               
               // mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
               
                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            6 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected6.setText("6")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.rounded_btn_pink)
                
              //  mBinding!!.playerSelected7.setText("")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            7 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected6.setText("6")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected7.setText("7")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.rounded_btn_pink)

               // mBinding!!.playerSelected8.setText("")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
                
            }
            8 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected6.setText("6")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected7.setText("7")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected8.setText("8")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.rounded_btn_pink)

               // mBinding!!.playerSelected9.setText("")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
              //  mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            9 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected6.setText("6")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected7.setText("7")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected8.setText("8")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected9.setText("9")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.rounded_btn_pink)

              //  mBinding!!.playerSelected10.setText("")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.bg_text)
                
               // mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
            }
            10 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected6.setText("6")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected7.setText("7")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected8.setText("8")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected9.setText("9")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.rounded_btn_pink)
                
                mBinding!!.playerSelected10.setText("10")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.bg_text)
                
            }
            11 -> {
                mBinding!!.playerSelected1.setText("1")
                mBinding!!.playerSelected1.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected2.setText("2")
                mBinding!!.playerSelected2.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected3.setText("3")
                mBinding!!.playerSelected3.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected4.setText("4")
                mBinding!!.playerSelected4.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected5.setText("5")
                mBinding!!.playerSelected5.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected6.setText("6")
                mBinding!!.playerSelected6.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected7.setText("7")
                mBinding!!.playerSelected7.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected8.setText("8")
                mBinding!!.playerSelected8.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected9.setText("9")
                mBinding!!.playerSelected9.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected10.setText("10")
                mBinding!!.playerSelected10.setBackgroundResource(R.drawable.rounded_btn_pink)

                mBinding!!.playerSelected11.setText("11")
                mBinding!!.playerSelected11.setBackgroundResource(R.drawable.rounded_btn_pink)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            startCountDown()
        } else {
            updateTimerHeader()
        }
    }

    private fun updateTimerHeader() {
        mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase()
        mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.green))
    }

    private fun startCountDown() {
        BindingUtils.logD("TimerLogs", "initViewUpcomingMatches() called in ContestActivity")
        //matchObject!!.timestampStart = 1591158573 + 300
        BindingUtils.countDownStart(matchObject!!.timestampStart, object : OnMatchTimerStarted {

            override fun onTimeFinished() {
                updateTimerHeader()
//                if(matchObject!!.status.equals(BindingUtils.MATCH_STATUS_UPCOMING)){
//                    showMatchTimeUpDialog()
//                }
            }

            override fun onTicks(time: String) {
                mBinding!!.matchTimer.text = time
                mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.white))
                //         mBinding!!.watchTimerImg.visibility =View.VISIBLE
                BindingUtils.logD("TimerLogs", "ContestScreen: " + time)
            }
        })
    }

    fun pauseCountDown() {
        BindingUtils.stopTimer()
    }

    override fun onPause() {
        super.onPause()
        pauseCountDown()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (CREATETEAM_REQUESTCODE == requestCode && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }

    private fun ressetPlayers() {
        totalPlayers = 0
        TEAMA = 0
        TEAMB = 0
        COUNT_WICKET_KEEPER = 0
        COUNT_BATS_MAN = 0
        COUNT_ALL_ROUNDER = 0
        COUNT_BOWLER = 0
        isAllPlayersSelected = false
        crateTeamHashMap.clear()
//        viewPagerAdapter.notifyDataSetChanged()
    }

    private fun showLoading() {
        mBinding!!.relativeViewpager.visibility = View.GONE
        mBinding!!.relativeProgress.visibility = View.VISIBLE

    }

    private fun hideLoading() {
        mBinding!!.relativeViewpager.visibility = View.VISIBLE
        mBinding!!.relativeProgress.visibility = View.GONE

    }


    fun getAllPlayers() {

        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        // models.token = MyPreferences.getToken(this)!!
        models.match_id = "" + matchObject!!.matchId!!
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        println(Gson().toJson(models))
        WebServiceClient(this).client.create(IApiMethod::class.java).getPlayer(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    MyUtils.showToast(this@CreateTeamActivity, t!!.localizedMessage)

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    hideLoading()
                    var res = response!!.body()
                    if (res != null) {
                        val responseModel = res.responseObject
                        if (responseModel!!.playersList != null) {
                            this@CreateTeamActivity.playersList  = responseModel!!.playersList!!
                            initViewPager()
                        }
                    }
                }

            })

    }

    private fun initViewPager() {
        setupViewPager(mBinding!!.viewpager)
        mBinding!!.tabs.setupWithViewPager(mBinding!!.viewpager)
    }


    private fun setupViewPager(
        viewPager: ViewPager
    ) {

        var wkList = parseEditTeamModel(playersList!!.wicketKeepers!!, 1)

        var titleTabs = ""
        if (isEditMode) {
            titleTabs = String.format("WK(%d)", myTeamModel!!.wicketKeepers!!.size)

        } else {
            titleTabs = getString(R.string.createteam_type_wk)
        }

        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(WicketKeepers(wkList, matchObject!!,0), titleTabs)

        var batsManList = parseEditTeamModel(playersList!!.batsmen!!, 2)

        if (isEditMode) {
            titleTabs = String.format("BAT(%d)", myTeamModel!!.batsmen!!.size)
        } else {
            titleTabs = getString(R.string.createteam_type_bat)
        }

        viewPagerAdapter.addFragment(Batsman(batsManList, matchObject!!,1), titleTabs)

        var allList = parseEditTeamModel(playersList!!.allRounders!!, 3)
        if (isEditMode) {
            titleTabs = String.format("AR(%d)", myTeamModel!!.allRounders!!.size)
        } else {
            titleTabs = getString(
                R.string.createteam_type_ar
            )
        }

        viewPagerAdapter.addFragment(AllRounder(allList, matchObject!!,2), titleTabs)

        var bowlerList = parseEditTeamModel(playersList!!.bowlers!!, 4)

        if (isEditMode) {
            titleTabs = String.format("BOWL(%d)", myTeamModel!!.bowlers!!.size)
        } else {
            titleTabs = getString(
                R.string.createteam_type_bowl
            )
        }

        viewPagerAdapter.addFragment(Bowlers(bowlerList, matchObject!!,3), titleTabs)

        viewPager.adapter = viewPagerAdapter

        updateEditTEam()
    }

    private fun updateEditTEam() {
        if (myTeamModel != null) {
            var wkSelected = myTeamModel!!.wicketKeepers
            var playersListWK = playersList!!.wicketKeepers!!
            for (y in 0 until wkSelected!!.size) {
                var robj = wkSelected[y]

                for (x in 0 until playersListWK!!.size) {
                    var dkkd = playersListWK[x]
                    if (robj == dkkd.playerId) {
                        var obj = playersListWK[x]
                        obj.isSelected = true
                        onWicketKeeperSelected(obj)
                    }
                }
            }
            //BatsManSelected
            var batsSelected = myTeamModel!!.batsmen
            var playersListBats = playersList!!.batsmen!!
            for (y in 0 until batsSelected!!.size) {
                var robj = batsSelected.get(y)
                for (x in 0 until playersListBats!!.size) {
                    if (robj == playersListBats.get(x).playerId) {
                        var obj = playersListBats[x]
                        obj.isSelected = true
                        onBatsManSelected(obj)
                    }
                }
            }

            //allRounderSelected
            var allSelected = myTeamModel!!.allRounders
            var playersListAll = playersList!!.allRounders!!
            for (y in 0 until allSelected!!.size) {
                var robj = allSelected.get(y)
                for (x in 0 until playersListAll!!.size) {
                    if (robj == playersListAll.get(x).playerId) {
                        var obj = playersListAll[x]
                        obj.isSelected = true
                        onAllRounderSelected(obj)
                    }
                }
            }

            //allBowlerSelected
            var bowlerSelected = myTeamModel!!.bowlers

            var playersListbowl = playersList!!.bowlers!!
            for (y in 0 until bowlerSelected!!.size) {
                var robj = bowlerSelected.get(y)
                for (x in 0 until playersListbowl!!.size) {
                    if (robj == playersListbowl.get(x).playerId) {
                        var obj = playersListbowl.get(x)
                        obj.isSelected = true
                        onBowlerSelected(obj)
                    }
                }
            }
        }
    }

    private fun parseEditTeamModel(
        realList: java.util.ArrayList<PlayersInfoModel>,
        position: Int
    ): java.util.ArrayList<PlayersInfoModel> {

//        if(myTeamModel!=null){
//           if(position==1){
//               var myteamWk = myTeamModel!!.wicketKeepers
//               for(y in 0..realList!!.size-1) {
//                   var robj = realList.get(y)
//                   totalPlayers++
//                   if(teamAId==robj.teamId) {
//                       TEAMA++
//                   }else if(teamBId==robj.teamId) {
//                       TEAMB++
//                   }
//                   COUNT_WICKET_KEEPER++
//                   for(x in 0..myteamWk!!.size-1){
//                       /**
//                        * Here logic will be for selections
//                        */
//                       var obj = myteamWk.get(x)
//                       if(obj==robj.teamId){
//                           robj.isSelected =true
//                           realList.set(y,robj)
//                       }
//
//                   }
//
//               }
//           }else
//            if(position==2){
//                var myteamWk = myTeamModel!!.batsmen
//                for(y in 0..realList!!.size-1) {
//                    var robj = realList.get(y)
//                    totalPlayers++
//                    if(teamAId==robj.teamId) {
//                        TEAMA++
//                    }else if(teamBId==robj.teamId) {
//                        TEAMB++
//                    }
//                    COUNT_BATS_MAN++
//                    for(x in 0..myteamWk!!.size-1){
//                        /**
//                         * Here logic will be for selections
//                         */
//                        var obj = myteamWk.get(x)
//                        if(obj==robj.teamId){
//                            robj.isSelected =true
//                            realList.set(y,robj)
//                        }
//
//                    }
//
//                }
//            }else
//                if(position==3){
//                    var myteamWk = myTeamModel!!.allRounders
//                    for(y in 0..realList!!.size-1) {
//                        var robj = realList.get(y)
//                        totalPlayers++
//                        if(teamAId==robj.teamId) {
//                            TEAMA++
//                        }else if(teamBId==robj.teamId) {
//                            TEAMB++
//                        }
//                        COUNT_ALL_ROUNDER++
//                        for(x in 0..myteamWk!!.size-1){
//                            /**
//                             * Here logic will be for selections
//                             */
//                            var obj = myteamWk.get(x)
//                            if(obj==robj.teamId){
//                                robj.isSelected =true
//                                realList.set(y,robj)
//                            }
//
//                        }
//
//                    }
//                }else
//                    if(position==4){
//                        var myteamWk = myTeamModel!!.bowlers
//                        for(y in 0..realList!!.size-1) {
//                            var robj = realList.get(y)
//                            totalPlayers++
//                            if(teamAId==robj.teamId) {
//                                TEAMA++
//                            }else if(teamBId==robj.teamId) {
//                                TEAMB++
//                            }
//                            COUNT_BOWLER++
//                            for(x in 0..myteamWk!!.size-1){
//                                /**
//                                 * Here logic will be for selections
//                                 */
//                                var obj = myteamWk.get(x)
//                                if(obj==robj.teamId){
//                                    robj.isSelected =true
//                                    realList.set(y,robj)
//                                }
//
//                            }
//
//                        }
//                    }
//
//
//        }

        return realList
    }


    override fun onWicketKeeperSelected(objects: PlayersInfoModel) {
        COUNT_WICKET_KEEPER++
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_WICKET_KEEPER)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_WICKET_KEEPER) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.add(objects)
        crateTeamHashMap.put(CREATE_TEAM_WICKET_KEEPER, playerListObject)
        countPlayers(1)
        addTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(0)!!.setText(String.format("WK(%d)", playerListObject.size))
        }
    }

    override fun onWicketKeeperDeSelected(objects: PlayersInfoModel) {
        COUNT_WICKET_KEEPER--
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_WICKET_KEEPER)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_WICKET_KEEPER) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.remove(objects)
        crateTeamHashMap.put(CREATE_TEAM_WICKET_KEEPER, playerListObject)
        countPlayers(-1)
        removeTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(0)!!.setText(String.format("WK(%d)", playerListObject.size))
        }
    }

    override fun onBatsManSelected(objects: PlayersInfoModel) {
        COUNT_BATS_MAN++
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_BATSMAN)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_BATSMAN) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.add(objects)
        crateTeamHashMap.put(CREATE_TEAM_BATSMAN, playerListObject)
        countPlayers(1)
        addTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(1)!!.setText(String.format("BAT(%d)", playerListObject.size))
        }
    }

    override fun onBatsManDeSelected(objects: PlayersInfoModel) {
        COUNT_BATS_MAN--
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_BATSMAN)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_BATSMAN) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.remove(objects)
        crateTeamHashMap.put(CREATE_TEAM_BATSMAN, playerListObject)
        countPlayers(-1)
        removeTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(1)!!.text = String.format("BAT(%d)", playerListObject.size)
        }
    }

    override fun onAllRounderSelected(objects: PlayersInfoModel) {
        COUNT_ALL_ROUNDER++
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_ALLROUNDER)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_ALLROUNDER) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.add(objects)
        crateTeamHashMap.put(CREATE_TEAM_ALLROUNDER, playerListObject)
        countPlayers(1)
        addTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(2)!!.setText(String.format("AR(%d)", playerListObject.size))
        }
    }

    override fun onAllRounderDeSelected(objects: PlayersInfoModel) {
        COUNT_ALL_ROUNDER--
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_ALLROUNDER)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_ALLROUNDER) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.remove(objects)
        crateTeamHashMap.put(CREATE_TEAM_ALLROUNDER, playerListObject)
        countPlayers(-1)
        removeTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(2)!!.setText(String.format("AR(%d)", playerListObject.size))
        }
    }

    override fun onBowlerSelected(objects: PlayersInfoModel) {
        COUNT_BOWLER++
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_BOWLER)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_BOWLER) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.add(objects)
        crateTeamHashMap.put(CREATE_TEAM_BOWLER, playerListObject)
        countPlayers(1)
        addTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(3)!!.setText(String.format("BOWL(%d)", playerListObject.size))
        }
    }

    override fun onBowlerDeSelected(objects: PlayersInfoModel) {
        COUNT_BOWLER--
        var playerListObject: ArrayList<PlayersInfoModel>? = null
        if (crateTeamHashMap.containsKey(CREATE_TEAM_BOWLER)) {
            playerListObject =
                crateTeamHashMap.get(CREATE_TEAM_BOWLER) as ArrayList<PlayersInfoModel>
        } else {
            playerListObject = ArrayList<PlayersInfoModel>()
        }
        playerListObject!!.remove(objects)
        crateTeamHashMap.put(CREATE_TEAM_BOWLER, playerListObject)
        countPlayers(-1)
        removeTeamPlayers(objects)
        if (!isEditMode) {
            mBinding!!.tabs.getTabAt(3)!!.setText(String.format("BOWL(%d)", playerListObject.size))
        }
    }

    override fun countPlayers(obj: Int) {
        totalPlayers += obj
        mBinding!!.totalplayerSelected.text = String.format("%d", totalPlayers)
        updatePlayersCountBar(totalPlayers)
        if (totalPlayers == MAX_PLAYERS_CRICKET) {
            isAllPlayersSelected = true
            mBinding!!.teamContinue.isEnabled = true
            mBinding!!.teamContinue.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
        } else {
            isAllPlayersSelected = false
            mBinding!!.teamContinue.isEnabled = false
            mBinding!!.teamContinue.setBackgroundResource(R.drawable.button_selector_grey)
        }
    }

    override fun addTeamPlayers(objects: PlayersInfoModel) {

        if (objects.teamId == teamAId) {
            TEAMA++
        } else if (objects.teamId == teamBId) {
            TEAMB++
        }
        mBinding!!.teamaCounts.text = String.format("%d", TEAMA)
        mBinding!!.teambCounts.text = String.format("%d", TEAMB)
    }


    override fun removeTeamPlayers(objects: PlayersInfoModel) {
        //if(objects.teamId==matchObject!!.teamAInfo!!.teamId){
        if (objects.teamId == teamAId) {
            TEAMA--
        } else if (objects.teamId == teamBId) {
            TEAMB--
        }

        mBinding!!.teamaCounts.text = String.format("%d", TEAMA)
        mBinding!!.teambCounts.text = String.format("%d", TEAMB)
    }


    internal inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }

}
