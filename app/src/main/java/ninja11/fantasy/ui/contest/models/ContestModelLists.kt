package ninja11.fantasy.ui.contest.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ninja11.fantasy.models.MyTeamModels
import java.io.Serializable


class ContestModelLists :Serializable,Cloneable {

    @SerializedName("contestId")
    @Expose
    var id: Int = 0

    @SerializedName("isCancelled")
    @Expose
    var isCancelled: Boolean = false

    @SerializedName("match_id")
    @Expose
    var matchId: String = ""

    @SerializedName("totalWinningPrize")
    @Expose
    var totalWinningPrize: Int = 0

    @SerializedName("winnerCount")
    @Expose
    var winnerCount: Int = 0


    @SerializedName("entryFees")
    @Expose
    var entryFees: Int = 0

    @SerializedName("match_status")
    @Expose
    var matchStatus: String = ""

    @SerializedName("totalSpots")
    @Expose
    var totalSpots: Int = 0

    @SerializedName("usable_bonus")
    @Expose
    var usable_bonus: Int = 0

    @SerializedName("filledSpots")
    @Expose
    var filledSpots: Int = 0

    @SerializedName("firstPrice")
    @Expose
    var firstPrice: Int = 0

    @SerializedName("winnerPercentage")
    @Expose
    var winnerPercentage: Int = 0

    @SerializedName("maxAllowedTeam")
    @Expose
    var maxAllowedTeam: Int = 0

    @SerializedName("cancellation")
    @Expose
    var cancellation: String = ""

    @SerializedName("joinedTeams")
    @Expose
    var joinedTeams: ArrayList<MyTeamModels>? = null



    public override fun clone(): ContestModelLists {
        return super.clone() as ContestModelLists
    }



}
