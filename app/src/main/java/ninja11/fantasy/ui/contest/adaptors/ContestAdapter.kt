package ninja11.fantasy.ui.contest.adaptors

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.R
import ninja11.fantasy.listener.OnContestEvents
import ninja11.fantasy.models.ContestsParentModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.ui.contest.adaptors.ContestListAdapter


class ContestAdapter(
    val context: Activity,
    val contestInfoModel: ArrayList<ContestsParentModels>,
    matchObject: UpcomingMatchesModel?,
    val listener: OnContestEvents
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mContext:Context ? =context
    var matchObject = matchObject
    private var matchesListObject =  contestInfoModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.contest_row_header, parent, false)
            return ViewHolderJoinedContest(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        val viewJoinedMatches: ViewHolderJoinedContest = parent as ViewHolderJoinedContest

        viewJoinedMatches.contestTitle.setText(objectVal.contestTitle)
        viewJoinedMatches.contestSubTitle.setText(objectVal.contestSubTitle)

        viewJoinedMatches.recyclerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        var  colorCode =  context.resources.getColor(R.color.white)
        if(objectVal.contestTitle.contains("Practise")){
            colorCode = context.resources.getColor(R.color.highlighted_text_material_dark)
        }
        var adapter = ContestListAdapter(
            context!!,
            objectVal.allContestsRunning!!,
            matchObject!!,
            listener,
            colorCode
        )
        viewJoinedMatches.recyclerView.adapter = adapter
        adapter.onItemClick = { objects ->
            //MyUtils.logd("JoinedContestAdapter","Joined Contest"+objects.country1Name+" Vs "+objects.country1Name)

        }
    }

    fun setMatchesList(matchesList: ArrayList<ContestsParentModels>?) {
        this.matchesListObject = matchesList!!
        // this.mContext = mContext
        notifyDataSetChanged()
    }

    fun addLoadingView() {
        Handler().post {
            matchesListObject!!.add(ContestsParentModels())
            notifyItemInserted(matchesListObject!!.size - 1)
        }
    }

    fun removeLoadingView() {
        matchesListObject!!.removeAt(matchesListObject!!.size - 1)
        notifyItemRemoved(matchesListObject!!.size)
    }


    override fun getItemCount(): Int {
        return matchesListObject!!.size
    }

    inner  class ViewHolderJoinedContest(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contestTitle = itemView.findViewById<TextView>(R.id.contest_title)
        val contestSubTitle = itemView.findViewById<TextView>(R.id.contest_sub_title)
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_all_contest)
    }



}

