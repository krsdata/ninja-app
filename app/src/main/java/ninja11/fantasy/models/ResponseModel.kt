package ninja11.fantasy.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ninja11.fantasy.models.UserInfo
import java.io.Serializable

class ResponseModel :Serializable {



    @SerializedName("status")
    val status: Boolean = false

    @SerializedName("message")
    val message: String = ""

    @SerializedName("is_account_verified")
    val isAccountVerified: Int = 0

    val image_url: String = ""

    @SerializedName("token")
    @Expose
    val token: String = "1"

    //{"CHECKSUMHASH":"oH28p6S0SC\/Tsh\/y2R2vZWvuUVlyWynVrvAkiHU9Oahe\/gt+Jg3h0s2iPR+i6iufIGFamPRpO4UPvX+7YG0bEqu36afqYNNCgNXOeVXSXrM=","order_id":"947014","status":"1"}

    @SerializedName("CHECKSUMHASH")
    val checksum: String = ""

    @SerializedName("data")
    var infomodel: UserInfo? = null

    @SerializedName("response")
    @Expose
    var responseModels: Response? = null

    inner class  Response {

        @SerializedName("matchcontests")
        @Expose
        var matchContestlist: List<ContestsParentModels>? = null

    }

}
