package ninja11.fantasy.adaptors

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ninja11.fantasy.R
import ninja11.fantasy.listener.OnMatchTimerStarted
import ninja11.fantasy.models.JoinedMatchModel

import ninja11.fantasy.utils.BindingUtils
import kotlin.collections.ArrayList


class JoinedMatchesAdapter(val context: Context, val tradeinfoModels: ArrayList<JoinedMatchModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((JoinedMatchModel) -> Unit)? = null
    private var matchesListObject =  tradeinfoModels


    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.matches_row_joined_inner, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        val viewHolder: DataViewHolder = parent as DataViewHolder
        viewHolder?.matchTitle?.text = objectVal.matchTitle
        viewHolder?.matchStatus?.text = ""+objectVal.statusString
        viewHolder?.matchProgress?.text = ""
        viewHolder?.opponent1?.text = objectVal.teamAInfo!!.teamShortName
        viewHolder?.opponent2?.text = objectVal.teamBInfo!!.teamShortName
        viewHolder?.totalTeamCreated?.text = String.format("%d",objectVal.totalTeams)
        viewHolder?.totalContestJoined?.text = String.format("%d",objectVal.totalJoinContests)

        if(!TextUtils.isEmpty(objectVal.prizeAmount) && Integer.parseInt(objectVal.prizeAmount)>0) {
            if(objectVal.status==3){
                viewHolder.winningPrice.setText(
                    String.format(
                        "Winning ₹%s",
                        objectVal.prizeAmount
                    )
                )
            }else {
                viewHolder?.winningPrice?.text = String.format("You Won ₹%s", objectVal.prizeAmount)
            }
        }else {
            viewHolder?.winningPrice?.visibility =View.INVISIBLE
        }

        BindingUtils.countDownStartForAdaptors(objectVal!!.timestampStart,object :
            OnMatchTimerStarted {

            override fun onTimeFinished() {
                viewHolder?.matchProgress.setText(objectVal.statusString)
            }

            override fun onTicks(time:String) {
                viewHolder?.matchProgress.setText(time)
            }


        })

        Glide.with(context)
            .load(objectVal.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(viewHolder?.teamALogo)


        Glide.with(context)
            .load(objectVal.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(viewHolder?.teamBLogo)

    }



    override fun getItemCount(): Int {
        return matchesListObject!!.size
    }

    inner  class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(matchesListObject!![adapterPosition])
            }
        }
        val matchTitle = itemView.findViewById<TextView>(R.id.completed_match_title)
        val matchStatus = itemView.findViewById<TextView>(R.id.completed_match_status)
        val opponent1 = itemView.findViewById<TextView>(R.id.upcoming_opponent1)
        val matchProgress = itemView.findViewById<TextView>(R.id.upcoming_match_progress)
        val opponent2 = itemView.findViewById<TextView>(R.id.upcoming_opponent2)
        val totalTeamCreated = itemView.findViewById<TextView>(R.id.total_team_created)
        val totalContestJoined = itemView.findViewById<TextView>(R.id.total_contest_joined)
        val teamALogo = itemView.findViewById<ImageView>(R.id.teama_logo)
        val teamBLogo = itemView.findViewById<ImageView>(R.id.teamb_logo)
        val winningPrice = itemView.findViewById<TextView>(R.id.winning_price)


    }


}

