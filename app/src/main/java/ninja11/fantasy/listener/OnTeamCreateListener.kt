package ninja11.fantasy.listener

import ninja11.fantasy.ui.createteam.models.PlayersInfoModel


interface OnTeamCreateListener {
    fun onWicketKeeperSelected(objects: PlayersInfoModel)
    fun onWicketKeeperDeSelected(objects: PlayersInfoModel)
    fun onBatsManSelected(objects: PlayersInfoModel)
    fun onBatsManDeSelected(objects: PlayersInfoModel)
    fun onAllRounderSelected(objects: PlayersInfoModel)
    fun onAllRounderDeSelected(objects: PlayersInfoModel)
    fun onBowlerSelected(objects: PlayersInfoModel)
    fun onBowlerDeSelected(objects: PlayersInfoModel)
    fun countPlayers(obj: Int)

    fun addTeamPlayers(objects: PlayersInfoModel)
    fun removeTeamPlayers(objects: PlayersInfoModel)

}