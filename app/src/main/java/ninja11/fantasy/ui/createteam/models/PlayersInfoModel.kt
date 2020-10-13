package ninja11.fantasy.ui.createteam.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PlayersInfoModel :Serializable,Cloneable{

    var isSelected: Boolean = false

    @SerializedName("captain")
    @Expose
    var isCaptain: Boolean = false

    @SerializedName("vice_captain")
    @Expose
    var isViceCaptain: Boolean = false

    @SerializedName("trump")
    @Expose
    var isTrump: Boolean = false

    @SerializedName("role")
    @Expose
    var playerRole: String = ""

    @SerializedName("playing11")
    @Expose
    var isPlaying11: Boolean = false

    @SerializedName("pid")
    @Expose
    var playerId: Int = 0

    @SerializedName("match_id")
    @Expose
    var matchId: Int = 0

    @SerializedName("team_id")
    @Expose
    var teamId: Int = 0

    @SerializedName("short_name")
    @Expose
    var shortName: String = ""

    @SerializedName("player_image")
    @Expose
    var playerImage: String = "https"

    @SerializedName("team_name")
    @Expose
    var teamShortName: String = ""

    @SerializedName("fantasy_player_rating")
    @Expose
    var fantasyPlayerRating: String=""

    @SerializedName("playerPoints")
    @Expose
    var playerPoints: String=""

    @SerializedName("analytics")
    @Expose
    var analyticsModel: AnalyticsModel?=null



    public override fun clone(): PlayersInfoModel {
        return super.clone() as PlayersInfoModel
    }

}
