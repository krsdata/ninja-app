package skoreon.fantasy.network

import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import retrofit2.Call
import retrofit2.http.*

interface PlugDataService {

    @Headers("Content-Type: application/json")
    @POST("api/v2/getMatch")
    fun getAllMatches(@Body request: RequestModel): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("api/v2/getContestByMatch")
    fun getContestByMatch(@Body request: RequestModel): Call<UsersPostDBResponse>


}