package ninja11.fantasy.listener

import ninja11.fantasy.ui.createteam.models.PlayersInfoModel


interface OnRolesSelected {
    fun onTrumpSelected(objects: PlayersInfoModel, position: Int)
    fun onCaptainSelected(objects: PlayersInfoModel,position: Int)
    fun onViceCaptainSelected(objects: PlayersInfoModel,position: Int)
    fun onReady()

}