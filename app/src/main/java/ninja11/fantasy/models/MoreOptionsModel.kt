package ninja11.fantasy.models

import ninja11.fantasy.R
import java.io.Serializable


class MoreOptionsModel :Serializable,Cloneable {

    var id: Int = 0
    var drawable: Int = R.drawable.logo_google
    var title: String = ""

}
