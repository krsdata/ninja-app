package ninja11.fantasy.ui.home.models

import android.content.Context
import androidx.lifecycle.MutableLiveData
import ninja11.fantasy.models.ContestsParentModels
import ninja11.fantasy.models.MatchesModels
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.RetrofitClient
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import java.util.*

class MatchesRepository(val mContext: Context, request: RequestModel) {
   // private var listofData: ArrayList<MatchesModels>? = ArrayList()
    private val mutableallMatchData =
        MutableLiveData<List<MatchesModels>?>()

    private val mutableallContestData =
        MutableLiveData<List<ContestsParentModels>?>()

    fun getMutableLiveData(request: RequestModel): MutableLiveData<List<MatchesModels>?> {
        val userDataService = RetrofitClient(mContext).service
        val call = userDataService.getAllMatches(request)

        call!!.enqueue(object : Callback<UsersPostDBResponse?> {
            override fun onResponse(
                call: Call<UsersPostDBResponse?>,
                response: Response<UsersPostDBResponse?>
            ) {
                val response = response.body()
                if (response != null) {
                   BindingUtils.currentTimeStamp =  response.systemTime
                    var responseObject = response.responseObject
                    var listofData = responseObject!!.matchdatalist as ArrayList<MatchesModels>?
                    mutableallMatchData.value = listofData
                }
            }

            override fun onFailure(
                call: Call<UsersPostDBResponse?>,
                t: Throwable
            ) {
            }
        })
        return mutableallMatchData
    }

    fun getLiveContestData(request: RequestModel): MutableLiveData<List<ContestsParentModels>?> {
        val userDataService = RetrofitClient(mContext).service
        val call = userDataService.getContestByMatch(request)

        call!!.enqueue(object : Callback<UsersPostDBResponse?> {
            override fun onResponse(
                call: Call<UsersPostDBResponse?>,
                response: Response<UsersPostDBResponse?>
            ) {

                val response = response.body()
                if (response != null) {
                    BindingUtils.currentTimeStamp =  response.systemTime
                    var responseObject = response.responseObject
                    var listofData = responseObject!!.matchContestlist as ArrayList<ContestsParentModels>?
                    mutableallContestData.value = listofData
                }
            }

            override fun onFailure(
                call: Call<UsersPostDBResponse?>,
                t: Throwable
            ) {
                MyUtils.logd("failure","failed"+t.message)
            }
        })
        return mutableallContestData
    }

    companion object {
        private const val TAG = "MatchRepository"
    }
}