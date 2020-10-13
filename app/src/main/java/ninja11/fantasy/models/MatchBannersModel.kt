package ninja11.fantasy.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MatchBannersModel :Serializable,Cloneable{

    @SerializedName("url")
    @Expose
    val bannerUrl: String = ""

    @SerializedName("title")
    @Expose
    val title: String = ""

    @SerializedName("descriptions")
    @Expose
    val description: String = ""



    public override fun clone(): MatchBannersModel {
        return super.clone() as MatchBannersModel
    }

    override fun toString(): String {
        return "bannerUrl(Id='$bannerUrl')"
    }


}
