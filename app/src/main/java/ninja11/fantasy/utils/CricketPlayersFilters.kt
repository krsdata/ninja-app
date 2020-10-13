package ninja.fantasy.utils

import android.content.Context
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel

class CricketPlayersFilters(context : Context){

    companion object{
        fun getPlayersbyOddEvenPositions(
            filteredWicketKeepers: ArrayList<PlayersInfoModel>,
            matchObject: UpcomingMatchesModel
        ):ArrayList<PlayersInfoModel>{

            var finalobjects:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>()
            var teamAPlayerList:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>()
            var teamBPlayerList:ArrayList<PlayersInfoModel> = ArrayList<PlayersInfoModel>()



            for(x in 0..filteredWicketKeepers.size-1){
                var playerInfoObject = filteredWicketKeepers.get(x)
                if(matchObject.teamAInfo!!.teamId==playerInfoObject.teamId){
                    teamAPlayerList.add(playerInfoObject)
                }
                if(matchObject.teamBInfo!!.teamId==playerInfoObject.teamId){
                    teamBPlayerList.add(playerInfoObject)
                }
            }
            val t1 = teamAPlayerList.size
            val t2 = teamBPlayerList.size
            if(t1>t2){
                for(x in 0..t1-1){
                    finalobjects.add(teamAPlayerList.get(x))
                    if(x<t2) {
                        finalobjects.add(teamBPlayerList.get(x))
                    }
                }
            }else {
                for(x in 0..t2-1){
                    finalobjects.add(teamBPlayerList.get(x))
                    if(x<t1) {
                        finalobjects.add(teamAPlayerList.get(x))
                    }
                }
            }

            return finalobjects
        }
    }
}