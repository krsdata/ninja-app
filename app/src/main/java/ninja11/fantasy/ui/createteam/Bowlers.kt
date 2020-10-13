package ninja11.fantasy.ui.createteam

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja.fantasy.utils.CricketPlayersFilters
import ninja11.fantasy.CreateTeamActivity
import ninja11.fantasy.R
import ninja11.fantasy.listener.OnTeamCreateListener
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.ui.createteam.adaptors.PlayersContestAdapter
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel
import ninja11.fantasy.databinding.FragmentCreateTeamListBinding
import ninja11.fantasy.utils.MyUtils

class Bowlers(
    bowlers: ArrayList<PlayersInfoModel>,
    val matchObject: UpcomingMatchesModel,
    val playerType: Int
) : Fragment() {
    var count = 0
    private lateinit var mListener: OnTeamCreateListener
    var bowlers = bowlers
    private var mBinding: FragmentCreateTeamListBinding? = null
    lateinit var adapter: PlayersContestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_create_team_list, container, false
        );
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bowlers = CricketPlayersFilters.getPlayersbyOddEvenPositions(bowlers, matchObject)
        mBinding!!.labelPlayersCounts.setText("Select 3 - 6 Bowlers")
        mBinding!!.recyclerCreatePlayersList.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.recyclerCreatePlayersList.getContext(),
            RecyclerView.VERTICAL
        )
        mBinding!!.recyclerCreatePlayersList.addItemDecoration(dividerItemDecoration)

        adapter = PlayersContestAdapter(
            activity!!,
            bowlers,
            matchObject,
            playerType
        )
        mBinding!!.recyclerCreatePlayersList.adapter = adapter


        adapter.onItemClick = { objects ->
            CreateTeamActivity.isEditMode = false
            if (objects.isSelected) {
                count--
                objects.isSelected = false
                mListener.onBowlerDeSelected(objects)
            } else {
                if (!CreateTeamActivity.isAllPlayersSelected!!) {
                    if (count < CreateTeamActivity.MAX_BOWLER[1]) {
                        if (isMaxPlayersValid(objects)) {
                            if (isMinimumPlayerSelected()) {
                                count++
                                objects.isSelected = true
                                mListener.onBowlerSelected(objects)
                            }
                        } else {
                            MyUtils.showToast(
                                activity!! as AppCompatActivity,
                                "MAX Player Reached limit  " + objects.teamShortName
                            )
                        }
                    } else {
                        MyUtils.showToast(
                            activity!! as AppCompatActivity,
                            "MAX ALLOWED is " + CreateTeamActivity.MAX_BOWLER[1]
                        )
                    }
                } else {
                    MyUtils.showToast(activity!! as AppCompatActivity, "ALL 11 Players Selected")
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun isMinimumPlayerSelected(): Boolean {
        if (CreateTeamActivity.totalPlayers >= CreateTeamActivity.MAX_PLAYERS_FROM_TEAM) {
            if (CreateTeamActivity.COUNT_WICKET_KEEPER < CreateTeamActivity.MAX_WICKET_KEEPER[0]) {
                MyUtils.showToast(
                    activity!! as AppCompatActivity,
                    "Minimum " + CreateTeamActivity.MAX_WICKET_KEEPER[0] + " " + "Wicket Keeper Required"
                )
                return false
            } else if (CreateTeamActivity.COUNT_BATS_MAN < CreateTeamActivity.MAX_BATSMAN[0]) {
                MyUtils.showToast(
                    activity!! as AppCompatActivity,
                    "Minimum " + CreateTeamActivity.MAX_BATSMAN[0] + " " + "BatsMan Required"
                )
                return false
            } else if (CreateTeamActivity.COUNT_ALL_ROUNDER < CreateTeamActivity.MAX_ALL_ROUNDER[0]) {
                MyUtils.showToast(
                    activity!! as AppCompatActivity,
                    "Minimum " + CreateTeamActivity.MAX_ALL_ROUNDER[0] + " " + "All Rounder Required"
                )
                return false
            } else if (CreateTeamActivity.COUNT_BOWLER < CreateTeamActivity.MAX_BOWLER[0]) {
                // MyUtils.showToast(activity!!.getWindow().getDecorView().getRootView(),"Minimum "+ CreateTeamActivity.MAX_BOWLER[0]+" "+"BOWLER Required")
                return true

            }
            return true
        }
        return true
    }

    private fun isMaxPlayersValid(objects: PlayersInfoModel): Boolean {
        if (objects.teamId == CreateTeamActivity.teamAId && CreateTeamActivity.TEAMA < CreateTeamActivity.MAX_PLAYERS_FROM_TEAM) {
            return true
        } else if (objects.teamId == CreateTeamActivity.teamBId && CreateTeamActivity.TEAMB < CreateTeamActivity.MAX_PLAYERS_FROM_TEAM) {
            return true

        }
        return false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTeamCreateListener) {
            mListener = context as OnTeamCreateListener
        } else {
            throw RuntimeException(
                "$context must implement OnTeamCreateListener"
            )
        }

    }

}
