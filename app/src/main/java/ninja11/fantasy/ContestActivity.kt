package ninja11.fantasy

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import ninja11.fantasy.R
import ninja11.fantasy.databinding.ActivityContestBinding
import ninja11.fantasy.listener.OnContestEvents
import ninja11.fantasy.listener.OnContestLoadedListener
import ninja11.fantasy.listener.OnMatchTimerStarted
import ninja11.fantasy.models.JoinedMatchModel
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.contest.ContestFragment
import ninja11.fantasy.ui.contest.MyContestFragment
import ninja11.fantasy.ui.contest.MyTeamFragment
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.ui.home.models.UsersPostDBResponse

import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class ContestActivity : BaseActivity(), OnContestLoadedListener, OnContestEvents {

    //private var isMatchLive: Boolean = false
    var matchObject : UpcomingMatchesModel?=null
    var isTimeUp :Boolean =false
    var joinedTeamList: java.util.ArrayList<MyTeamModels>?=null
    var contestObjects: ArrayList<ContestModelLists>?=null
    private var mBinding: ActivityContestBinding? = null
    companion object{
        val SERIALIZABLE_KEY_UPCOMING_MATCHES: String = "contest"
        val SERIALIZABLE_KEY_JOINED_CONTEST: String = "joinedcontest"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_contest)

       /* val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/



        if(intent.hasExtra(SERIALIZABLE_KEY_UPCOMING_MATCHES)) {
            matchObject = intent.getSerializableExtra(SERIALIZABLE_KEY_UPCOMING_MATCHES) as UpcomingMatchesModel
        }
        if(intent.hasExtra(SERIALIZABLE_KEY_JOINED_CONTEST)) {
            var joinedMatchObject = intent.getSerializableExtra(SERIALIZABLE_KEY_JOINED_CONTEST) as JoinedMatchModel
            matchObject = UpcomingMatchesModel()
            matchObject!!.teamAInfo = joinedMatchObject.teamAInfo
            matchObject!!.teamBInfo = joinedMatchObject.teamBInfo
            matchObject!!.matchId = joinedMatchObject.matchId
            matchObject!!.timestampStart = joinedMatchObject.timestampStart
            matchObject!!.timestampEnd = joinedMatchObject.timestampEnd
            matchObject!!.matchTitle = joinedMatchObject.matchTitle
            matchObject!!.status = joinedMatchObject.status
            matchObject!!.statusString = joinedMatchObject.statusString

        }
        if(matchObject!=null) {
            initViewUpcomingMatches()
        }

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.imgWallet.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ContestActivity, MyBalanceActivity::class.java)
            startActivity(intent)
        })
        setupViewPager(mBinding!!.viewpagerContest)
        mBinding!!.tabs.setupWithViewPager(mBinding!!.viewpagerContest)


    }
    private fun initViewUpcomingMatches() {

        mBinding!!.teamsa.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teamsb.text = matchObject!!.teamBInfo!!.teamShortName
      //  mBinding!!.teamaLogo.imageAlpha = matchObject!!.teamAInfo!!.logoUrl

        Glide.with(this)
            .load(matchObject!!.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(mBinding!!.teamaLogo)

        Glide.with(this)
            .load(matchObject!!.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(mBinding!!.teambLogo)


    }

    override fun onResume() {
        super.onResume()
        if(matchObject!!.status==BindingUtils.MATCH_STATUS_UPCOMING) {
            pauseCountDown()
            startCountDown()
        }else {
            updateTimerHeader()
        }
    }

    private fun startCountDown() {
        BindingUtils.logD("TimerLogs","initViewUpcomingMatches() called in ContestActivity")
        //matchObject!!.timestampStart = 1591371412 + 300
        BindingUtils.countDownStart(matchObject!!.timestampStart,object : OnMatchTimerStarted {

            override fun onTimeFinished() {
                if(!isTimeUp) {
                    isTimeUp =true
                    updateTimerHeader()
                    if (matchObject!!.status.equals(BindingUtils.MATCH_STATUS_UPCOMING)) {
                        showMatchTimeUpDialog()
                    }
                }
            }


            override fun onTicks(time:String) {
                mBinding!!.matchTimer.text = time
                mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.white))



                //  mBinding!!.watchTimerImg.visibility =View.VISIBLE
                BindingUtils.logD("TimerLogs","ContestScreen: "+time)
            }
        })
    }

    private fun updateTimerHeader() {
      mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase()
       mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.white))
       //mBinding!!.watchTimerImg.visibility =View.GONE
    }

    fun pauseCountDown(){
        BindingUtils.stopTimer()
    }

    override fun onPause() {
        super.onPause()
        pauseCountDown()
    }

    fun changeTabsPositions(postion:Int){
        mBinding!!.viewpagerContest.currentItem = postion
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if(CreateTeamActivity.CREATETEAM_REQUESTCODE==requestCode && resultCode== Activity.RESULT_OK){
//            mBinding!!.viewpagerContest.setCurrentItem(1)
//        }else {
//            getCon
//        }
        mBinding!!.viewpagerContest.currentItem = 0
       // mBinding!!.viewpagerContest.get
       // getAllContest()
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        this.collapseMenu = menu
        return true
    }
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        if(matchObject!!.status==BindingUtils.MATCH_STATUS_UPCOMING) {
            adapter.addFragment(
                ContestFragment(matchObject), getString(
                    R.string.contest_type_contests
                )
            )
        }
        adapter.addFragment(
            MyContestFragment(matchObject), getString(
            R.string.contest_type_mycontest
        ))
        adapter.addFragment(
            MyTeamFragment(matchObject), getString(
            R.string.contest_type_myteam
        ))
        viewPager.adapter = adapter
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

    override fun onMyContest(contestObjects: ArrayList<ContestModelLists>) {
        this.contestObjects = contestObjects
        if(matchObject!!.status==BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.tabs.getTabAt(1)!!.text = String.format("My Contest(%d)", contestObjects.size)
        }else {
            mBinding!!.tabs.getTabAt(0)!!.text = String.format("My Contest(%d)", contestObjects.size)
        }
    }

    override fun onMyTeam(objects: ArrayList<MyTeamModels>) {
        this.joinedTeamList =objects
        if(matchObject!!.status==BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.tabs.getTabAt(2)!!.text = String.format("MyTeam(%d)", this.joinedTeamList!!.size)
        }else {
            mBinding!!.tabs.getTabAt(1)!!.text = String.format("MyTeam(%d)", this.joinedTeamList!!.size)
        }
    }

    override fun onContestJoinning(objects: ContestModelLists,position: Int) {
        customeProgressDialog!!.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
      //  models.token =MyPreferences.getToken(this)!!
        models.match_id =""+matchObject!!.matchId
        models.contest_id =""+objects!!.id
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        WebServiceClient(this).client.create(IApiMethod::class.java).joinNewContestStatus(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    var res = response!!.body()
                    if(res!=null) {
                        if(res.actionForTeam==1){
                            val intent = Intent(this@ContestActivity, CreateTeamActivity::class.java)
                            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                            startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
                        }else if(res.actionForTeam==2){
                            val intent = Intent(this@ContestActivity, SelectTeamActivity::class.java)
                            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                            intent.putExtra(CreateTeamActivity.SERIALIZABLE_CONTEST_KEY, objects)
                            intent.putExtra(CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS, res.selectedTeamModel)
                            startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
                        }else {
                            Toast.makeText(this@ContestActivity,res.message,Toast.LENGTH_LONG).show()
                        }
                    }

                }

            })


    }

    override fun onShareContest(objects: ContestModelLists) {

    }


}
