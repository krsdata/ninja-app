package ninja11.fantasy.listener

import ninja11.fantasy.ui.contest.models.ContestModelLists


interface OnContestEvents {
    fun onContestJoinning(objects: ContestModelLists, position: Int)
    fun onShareContest(objects:ContestModelLists)
}