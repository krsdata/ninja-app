package ninja11.fantasy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.adaptors.SelectedTeamAdapter
import ninja11.fantasy.databinding.ActivitySelectTeamBinding
import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.models.SelectedTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.JoinContestDialogFragment
import ninja11.fantasy.ui.contest.models.ContestModelLists
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SelectTeamActivity : AppCompatActivity() {

    private var customeProgressDialog: CustomeProgressDialog?=null
    private lateinit var contestModel: ContestModelLists
    private lateinit var matchObject: UpcomingMatchesModel
    private var mBinding: ActivitySelectTeamBinding? = null
    lateinit var adapter: SelectedTeamAdapter
    var selectedTeamList : ArrayList<SelectedTeamModels> =ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_select_team
        )
       /* val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        customeProgressDialog = CustomeProgressDialog(this)
        matchObject = intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel
        contestModel = intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_CONTEST_KEY) as ContestModelLists
        selectedTeamList = intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS) as ArrayList<SelectedTeamModels>

        mBinding!!.imageBack.setOnClickListener {
            finish()
        }


        mBinding!!.createTeam.setOnClickListener {
            val intent = Intent(this@SelectTeamActivity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
        }

        mBinding!!.recyclerSelectTeam.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        adapter = SelectedTeamAdapter(this,matchObject,customeProgressDialog!!,selectedTeamList!!)
        mBinding!!.recyclerSelectTeam.adapter = adapter
        mBinding!!.teamContinue.setOnClickListener {

            joinMatch()
        }

        if(selectedTeamList!=null && selectedTeamList.size>0){
            val openMatchListPos0 = selectedTeamList[0].openTeamList
            if(openMatchListPos0!=null && openMatchListPos0.size==1){
                val obj = selectedTeamList[0].openTeamList!!
                val otl = obj[0]
                otl.isSelected = true
                obj[0] = otl
                joinMatch()
            }else {
                if(selectedTeamList.size ==2) {
                    val openMatchListPos1 = selectedTeamList[1].openTeamList
                    if (openMatchListPos1 != null && openMatchListPos1.size == 1) {
                        val otl = openMatchListPos1[0]
                        otl.isSelected = true
                        openMatchListPos1[0] = otl
                        joinMatch()
                    }
                }
            }
        }


    }

    private fun joinMatch() {
        var isTeamFound = false
        val seelctedTeamList = getSelectedOpenList()
        for(x in 0 until seelctedTeamList!!.size){
            val objects = seelctedTeamList[x]
            if(objects.isSelected!!){
                isTeamFound = true
            }
        }
        if(isTeamFound) {
            val fm = supportFragmentManager
            val pioneersFragment =
                JoinContestDialogFragment(seelctedTeamList, matchObject, contestModel)
            pioneersFragment.show(fm, "PioneersFragment_tag")
        }else {
            MyUtils.showToast(this@SelectTeamActivity,"Please select your team to join this contest")
        }
    }

    private fun getSelectedOpenList(): ArrayList<MyTeamModels> {
        return selectedTeamList.get(selectedTeamList.size-1).openTeamList!!
    }

    fun refreshContents() {
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        // models.token =MyPreferences.getToken(this)!!
        models.match_id =""+matchObject!!.matchId
        models.contest_id =""+contestModel!!.id

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
                    val res = response!!.body()
                    if(res!=null) {
                        selectedTeamList = res.selectedTeamModel!!
                        adapter = SelectedTeamAdapter(this@SelectTeamActivity,matchObject,customeProgressDialog!!,selectedTeamList!!)
                        mBinding!!.recyclerSelectTeam.adapter = adapter
                    }

                }

            })
    }


}
