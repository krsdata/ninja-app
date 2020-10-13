package ninja11.fantasy.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import java.io.Serializable


class PlayerModels:Serializable,Cloneable {

    @SerializedName("bat")
    @Expose
    var batsmen: ArrayList<PlayersInfoModel> ?=null

    @SerializedName("bowl")
    @Expose
    var bowlers: ArrayList<PlayersInfoModel> ?=null


    @SerializedName("all")
    @Expose
    var allRounders: ArrayList<PlayersInfoModel> ?=null


    @SerializedName("wkbat")
    @Expose
    var wicketKeeperBatsMan: ArrayList<PlayersInfoModel> ?=null

     @SerializedName("wk")
    @Expose
    var wicketKeepers: ArrayList<PlayersInfoModel> ?=null



}
