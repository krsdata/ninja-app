package ninja11.fantasy.listener

import ninja11.fantasy.models.MyTeamModels
import ninja11.fantasy.ui.contest.models.ContestModelLists


interface OnContestLoadedListener {
    fun onMyContest(contestModel: ArrayList<ContestModelLists>)
    fun onMyTeam(count: ArrayList<MyTeamModels>)
}