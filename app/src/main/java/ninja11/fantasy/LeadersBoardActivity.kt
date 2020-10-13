package ninja11.fantasy

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import ninja11.fantasy.databinding.ActivityLeadersBoardBinding
import ninja11.fantasy.listener.OnMatchTimerStarted
import ninja11.fantasy.models.PlayerModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.ui.leadersboard.LeadersBoardFragment
import ninja11.fantasy.ui.leadersboard.PrizeBreakupFragment
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.ui.home.models.UsersPostDBResponse as UsersPostDBResponse1


class LeadersBoardActivity : BaseActivity() {

    var mainHandler: Handler? = Handler()
    private var playersList: PlayerModels?=null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    var contestObject: ContestModelLists?=null
    var matchObject: UpcomingMatchesModel?=null
    private var mBinding: ActivityLeadersBoardBinding? = null
    private val updateScoresHandler = object : Runnable {
        override fun run() {
            Log.d("leadersboard", "hitting to server")
            if(!isFinishing) {
                updateScores()
                mainHandler!!.postDelayed(this, 120000)
            }
        }
    }


    companion object{
        var CREATETEAM_REQUESTCODE: Int=2001

        val SERIALIZABLE_MATCH_KEY: String = "matchObject"
        val SERIALIZABLE_CONTEST_KEY: String = "contest"

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_leaders_board
        )

     /*   val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        contestObject = intent.getSerializableExtra(SERIALIZABLE_CONTEST_KEY) as ContestModelLists
        matchObject = intent.getSerializableExtra(SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel

        mBinding!!.imgFantasyPoints.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@LeadersBoardActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_URL,BindingUtils.WEBVIEW_FANTASY_POINTS)
            if (Build.VERSION.SDK_INT > 21) {
                val options =
                    ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
        })

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        if(matchObject!!.status==BindingUtils.MATCH_STATUS_UPCOMING){
            mBinding!!.includeContestRow.linearTradesStatus.visibility = View.VISIBLE
            mBinding!!.includeLiveMatchRow.liveMatchesRow.visibility = View.GONE
            initUpcomingMatchData()
        }else {
            mBinding!!.includeContestRow.linearTradesStatus.visibility = View.GONE
            mBinding!!.includeLiveMatchRow.liveMatchesRow.visibility = View.VISIBLE
            mBinding!!.loaderLogin.visibility = View.VISIBLE
            initScoreCard();
        }

        setupViewPager(mBinding!!.viewpager)
        mBinding!!.tabs.setupWithViewPager(mBinding!!.viewpager)


       initContestDetails()

    }


    override fun onResume() {
        super.onResume()
        if(matchObject!!.status==BindingUtils.MATCH_STATUS_UPCOMING) {
            startCountDown()
        }else {
            updateTimerHeader()
        }
        if(matchObject!!.status.equals(BindingUtils.MATCH_STATUS_LIVE)) {
            mainHandler!!.post(updateScoresHandler)
        }
    }

    private fun updateTimerHeader() {
        mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase()
        mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.white))
       // mBinding!!.watchTimerImg.visibility =View.GONE
    }

    private fun startCountDown() {
        BindingUtils.logD("TimerLogs","initViewUpcomingMatches() called in ContestActivity")
       // matchObject!!.timestampStart = 1591158573 + 300
        BindingUtils.countDownStart(matchObject!!.timestampStart,object : OnMatchTimerStarted {
            override fun onTimeFinished() {
                updateTimerHeader()
                if(matchObject!!.status.equals(BindingUtils.MATCH_STATUS_UPCOMING)){
                    showMatchTimeUpDialog()
                }
            }

            override fun onTicks(time:String) {
                mBinding!!.matchTimer.setText(time)
            }

        })

    }

    fun pauseCountDown(){
        BindingUtils.stopTimer()
    }


    private fun initContestDetails() {
        mBinding!!.includeLiveMatchRow.contestPrizePool.text= String.format("%s%d","Rs ",contestObject!!.totalWinningPrize)
        mBinding!!.includeLiveMatchRow.contestSpots.text= String.format("%d",contestObject!!.totalSpots)
        mBinding!!.includeLiveMatchRow.contestEntryPrize.text=  String.format("%s%d","₹",contestObject!!.entryFees)
    }

    private fun initScoreCard() {
        mBinding!!.teamsa.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teamsb.text = matchObject!!.teamBInfo!!.teamShortName
        Glide.with(this)
            .load(matchObject!!.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.placeholder_player_teama)
            .into(mBinding!!.includeLiveMatchRow.imgTeamaLogo)

        Glide.with(this)
            .load(matchObject!!.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.placeholder_player_teama)
            .into(mBinding!!.includeLiveMatchRow.imgTeambLogo)

        mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase()
        mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.green))
       // mBinding!!.watchTimerImg.visibility =View.GONE

        mBinding!!.includeLiveMatchRow.teamAName.setText(matchObject!!.teamAInfo!!.teamShortName)
        mBinding!!.includeLiveMatchRow.teamBName.setText(matchObject!!.teamBInfo!!.teamShortName)

        mBinding!!.includeLiveMatchRow.teamAScore.setText("0-0")
        mBinding!!.includeLiveMatchRow.teamAOver.setText("(0)")

        mBinding!!.includeLiveMatchRow.teamBScore.setText("0-0")
        mBinding!!.includeLiveMatchRow.teamBOver.setText("0-0")

        updateScores()
    }


    override fun onPause() {
        super.onPause()
        pauseCountDown()
        if(matchObject!!.status.equals(BindingUtils.MATCH_STATUS_LIVE)) {
            mainHandler!!.removeCallbacks(updateScoresHandler)
        }
    }


    fun updateScores() {
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
       // models.token = MyPreferences.getToken(this)!!
        models.contest_id =""+contestObject!!.id
        models.match_id =""+matchObject!!.matchId

        WebServiceClient(this).client.create(IApiMethod::class.java).getScore(models)
            .enqueue(object : Callback<UsersPostDBResponse1?> {
                override fun onFailure(call: Call<UsersPostDBResponse1?>?, t: Throwable?) {
                    if(mBinding!!.loaderLogin!=null) {
                        mBinding!!.loaderLogin.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse1?>?,
                    response: Response<UsersPostDBResponse1?>?
                ) {
                    if(mBinding!!.loaderLogin!=null) {
                        mBinding!!.loaderLogin.visibility = View.GONE
                    }
                    var res = response!!.body()
                    if(res!=null) {
                        if(res.scoresModel!=null){

                            mBinding!!.includeLiveMatchRow.statusNote.setText(res.scoresModel!!.statusNote)
                            if(res.scoresModel!!.teama!!.scores!=null){
                                mBinding!!.includeLiveMatchRow.teamAScore.setText(res.scoresModel!!.teama!!.scores)
                            }else {
                                mBinding!!.includeLiveMatchRow.teamAScore.setText("")
                            }

                            if(res.scoresModel!!.teama!!.overs!=null){
                                mBinding!!.includeLiveMatchRow.teamAOver.setText(String.format("(%s)",res.scoresModel!!.teama!!.overs))
                            }else {
                                mBinding!!.includeLiveMatchRow.teamAOver.setText(String.format("(%s)",""))
                            }


                            mBinding!!.includeLiveMatchRow.teamBScore.setText(res.scoresModel!!.teamb!!.scores)
                            mBinding!!.includeLiveMatchRow.teamBOver.setText(String.format("(%s)",res.scoresModel!!.teamb!!.overs))

                            val fragment: Fragment = viewPagerAdapter.getItem(mBinding!!.viewpager.getCurrentItem())
                            if(fragment!=null){
                                if(fragment is LeadersBoardFragment){
                                    fragment.getLeadersBoards()
                                }
                            }
                        }

                    }

                }

            })
    }


    private fun initUpcomingMatchData() {
        mBinding!!.teamsa.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teamsb.text = matchObject!!.teamBInfo!!.teamShortName


        Glide.with(this)
            .load(matchObject!!.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(mBinding!!.teamaLogo)

        Glide.with(this)
            .load(matchObject!!.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(mBinding!!.teambLogo)

        var totalSpots = contestObject!!.totalSpots
        var filledSpots = contestObject!!.filledSpots
        mBinding!!.includeContestRow.contestPrizePool.text= String.format("%s%d","₹ ",contestObject!!.totalWinningPrize)
        mBinding!!.includeContestRow.contestEntryPrize.text=  String.format("%s%d","₹",contestObject!!.entryFees)
        if(totalSpots==0){
            mBinding!!.includeContestRow.contestProgress.max =totalSpots +15
            mBinding!!.includeContestRow.contestProgress.progress =filledSpots
            mBinding!!.includeContestRow.totalSpot.text= String.format("Unlimited spots")
            mBinding!!.includeContestRow.totalSpotLeft.text= String.format("%d spots filled",filledSpots)

        }else {
            mBinding!!.includeContestRow.contestProgress.max =totalSpots
            mBinding!!.includeContestRow.contestProgress.progress =filledSpots
            mBinding!!.includeContestRow.totalSpot.text= String.format("%d spots",totalSpots)
            if(contestObject!!.totalSpots == contestObject!!.filledSpots){
                mBinding!!.includeContestRow.totalSpotLeft?.text = "Contest Full"
                mBinding!!.includeContestRow.totalSpotLeft?.setTextColor(Color.RED)
            }else {
                mBinding!!.includeContestRow.totalSpotLeft.text =
                    String.format("%d spots left", (totalSpots - filledSpots))
            }
        }


        mBinding!!.includeContestRow.contestEntryPrize.text=  String.format("%s%d","₹",contestObject!!.entryFees)
        mBinding!!.includeContestRow.firstPrize?.text = String.format("%s%d","₹",contestObject!!.firstPrice)
        mBinding!!.includeContestRow.winnerCount?.text = String.format("%s%d","",contestObject!!.winnerCount)
//        mBinding!!.includeContestRow.winningPercentage?.text = String.format("%d%s",contestObject!!.winnerPercentage,"%")
//        mBinding!!.includeContestRow.maxAllowedTeam?.text = String.format("%s %d %s","Upto",contestObject!!.winnerPercentage,"teams")

        mBinding!!.includeContestRow.mutiplayer?.text = String.format("%s",contestObject!!.maxAllowedTeam,"teams")
        mBinding!!.includeContestRow.tvBonus?.text = String.format("%s%d","",contestObject!!.usable_bonus)

        if (contestObject!!.maxAllowedTeam.equals(1)){
            mBinding!!.includeContestRow.multiplayTeam.visibility = View.GONE

        }

        if (contestObject!!.usable_bonus.equals(0)){
            mBinding!!.includeContestRow.bonus.visibility = View.GONE

        }

        if (contestObject!!.cancellation.equals("true")){
            mBinding!!.includeContestRow.contestCancellation.visibility = View.GONE


        }else{

            mBinding!!.includeContestRow.contestCancellation.visibility = View.VISIBLE
        }


        mBinding!!.includeContestRow?.contestEntryPrize?.setOnClickListener(View.OnClickListener {
            if(!MyUtils.isConnectedWithInternet(this)) {
                MyUtils.showToast(this,"No Internet connection found")
            }else {
                mBinding!!.loaderLogin.visibility = View.VISIBLE
                var models = RequestModel()
                models.user_id = MyPreferences.getUserID(this)!!
                // models.token =MyPreferences.getToken(this)!!
                models.match_id =""+matchObject!!.matchId
                models.contest_id =""+contestObject!!.id

                WebServiceClient(this).client.create(IApiMethod::class.java).joinNewContestStatus(models)
                    .enqueue(object : Callback<UsersPostDBResponse1?> {
                        override fun onFailure(call: Call<UsersPostDBResponse1?>?, t: Throwable?) {
                            mBinding!!.loaderLogin.visibility = View.GONE
                        }

                        override fun onResponse(
                            call: Call<UsersPostDBResponse1?>?,
                            response: Response<UsersPostDBResponse1?>?
                        ) {
                            mBinding!!.loaderLogin.visibility = View.GONE
                            var res = response!!.body()
                            if(res!=null) {
                                if(res.actionForTeam==1){
                                    val intent = Intent(this@LeadersBoardActivity, CreateTeamActivity::class.java)
                                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                                    startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
                                }else if(res.actionForTeam==2){
                                    val intent = Intent(this@LeadersBoardActivity, SelectTeamActivity::class.java)
                                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_CONTEST_KEY, contestObject)
                                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS, res.selectedTeamModel)
                                    startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)

                                }else {
                                    Toast.makeText(
                                        this@LeadersBoardActivity,
                                        res.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        }

                    })
            }

        })


//        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(CREATETEAM_REQUESTCODE==requestCode && resultCode== Activity.RESULT_OK){
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {


    }

    private fun setupViewPager(
        viewPager: ViewPager
    ) {
        this.playersList = playersList

        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        viewPagerAdapter.addFragment(PrizeBreakupFragment(matchObject!!,contestObject!!),"Prize Breakup")
        viewPagerAdapter.addFragment(LeadersBoardFragment(matchObject!!,contestObject!!), "Leaderboard")
       // viewPagerAdapter.addFragment(ContestStatsFragment(matchObject!!), "Leaderboard")

        viewPager.adapter = viewPagerAdapter


    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
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
