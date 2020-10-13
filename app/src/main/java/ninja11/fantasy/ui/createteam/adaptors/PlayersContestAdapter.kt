package ninja11.fantasy.ui.createteam.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ninja11.fantasy.R
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.ui.createteam.models.PlayersInfoModel


class PlayersContestAdapter(
    val context: Context,
    val playerList: ArrayList<PlayersInfoModel>,
    matchObject: UpcomingMatchesModel,
    val playerType:Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //private var isMatchStarted: Boolean?=false
    var onItemClick: ((PlayersInfoModel) -> Unit)? = null
    private var playerListObject = playerList
    private var matchObject = matchObject


    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.createteam_row_players, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = playerListObject!![viewType]
        val viewHolder: DataViewHolder = parent as DataViewHolder
        println("role   " + objectVal.playerRole)
        if (objectVal.analyticsModel != null) {
            viewHolder?.playerSelectionPercentage?.text =
                "Sel by " + objectVal.analyticsModel!!.selectionPc + "%"
        } else {
            viewHolder?.playerSelectionPercentage?.text = ""
        }
        viewHolder?.playerName?.text = objectVal.shortName
        viewHolder?.teamName?.text = objectVal.teamShortName
        if (matchObject.teamAInfo!!.teamId == objectVal.teamId) {
            viewHolder?.teamName?.setBackgroundColor(context.resources.getColor(R.color.black))
            viewHolder?.teamName?.setTextColor(context.resources.getColor(R.color.white))
            when(playerType){
                1->
                Glide.with(context)
                    .load(objectVal.playerImage)
                    .placeholder(R.drawable.ic_batsman_1)
                    .into(viewHolder?.playerImage)
                0->
                Glide.with(context)
                    .load(objectVal.playerImage)
                    .placeholder(R.drawable.ic_wicket_keeper_1)
                    .into(viewHolder?.playerImage)
                2->
                Glide.with(context)
                    .load(objectVal.playerImage)
                    .placeholder(R.drawable.ic_all_rounder_1)
                    .into(viewHolder?.playerImage)
                3->
                Glide.with(context)
                    .load(objectVal.playerImage)
                    .placeholder(R.drawable.ic_bowler_1)
                    .into(viewHolder?.playerImage)
            }
        } else {
            viewHolder?.teamName?.setBackgroundColor(context.resources.getColor(R.color.white))
            viewHolder?.teamName?.setTextColor(context.resources.getColor(R.color.black))
            when(playerType){
                1->
                    Glide.with(context)
                        .load(objectVal.playerImage)
                        .placeholder(R.drawable.ic_batsman)
                        .into(viewHolder?.playerImage)
                0->
                    Glide.with(context)
                        .load(objectVal.playerImage)
                        .placeholder(R.drawable.ic_wicket_keeper)
                        .into(viewHolder?.playerImage)
                2->
                    Glide.with(context)
                        .load(objectVal.playerImage)
                        .placeholder(R.drawable.ic_all_rounder)
                        .into(viewHolder?.playerImage)
                3->
                    Glide.with(context)
                        .load(objectVal.playerImage)
                        .placeholder(R.drawable.ic_bowler)
                        .into(viewHolder?.playerImage)
            }

        }
        viewHolder?.fantasyPoints?.text = objectVal.fantasyPlayerRating
        viewHolder?.playerPoints?.text = objectVal.playerPoints
//        BindingUtils.countDownStart(matchObject.timestampStart,object:OnMatchTimerStarted{
//            override fun onTimeFinished() {
//                  isMatchStarted=true
//            }

//
//            override fun onTicks(time: String) {
//                isMatchStarted=false
//            }
//
//        })
        // if(!isMatchStarted!!){

        if (objectVal.isPlaying11 && matchObject.isLineup) {
            viewHolder?.anouncedIndicatorCircle?.setBackgroundResource(R.drawable.circle_green)
            viewHolder?.anouncedIndicatorText?.setText("Announced")
            viewHolder?.anouncedIndicatorText?.setTextColor(context.resources.getColor(R.color.green))
        } else {
            viewHolder?.anouncedIndicatorText?.setText("")
            viewHolder?.anouncedIndicatorCircle?.setBackgroundResource(R.drawable.circle_red)
        }


//        }else {
//            viewHolder?.linearAnnounced?.visibility = View.GONE
//        }




        if (objectVal.isSelected) {
            viewHolder?.addImage.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp)
            viewHolder?.linearTradesStatus.setBackgroundColor(context.resources.getColor(R.color.background_default))
        } else {
            viewHolder?.addImage.setImageResource(R.drawable.ic_add_circle_outline_black_24dp)
            viewHolder?.linearTradesStatus.setBackgroundColor(context.resources.getColor(R.color.white))
        }

    }


    override fun getItemCount(): Int {
        return playerListObject!!.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(playerListObject!![adapterPosition])
            }
        }

        val linearTradesStatus = itemView.findViewById<RelativeLayout>(R.id.linear_trades_status)
        val playerSelectionPercentage =
            itemView.findViewById<TextView>(R.id.player_selection_percentage)
        val playerName = itemView.findViewById<TextView>(R.id.player_name)
        val playerImage = itemView.findViewById<ImageView>(R.id.player_image)
        val teamName = itemView.findViewById<TextView>(R.id.team_name)
        val fantasyPoints = itemView.findViewById<TextView>(R.id.fantasy_points)
        val linearAnnounced = itemView.findViewById<LinearLayout>(R.id.linear_announced)
        val anouncedIndicatorCircle =
            itemView.findViewById<TextView>(R.id.anounced_indicator_circle)
        val anouncedIndicatorText = itemView.findViewById<TextView>(R.id.anounced_indicator_text)
        val addImage = itemView.findViewById<ImageView>(R.id.add_image)
        val playerPoints = itemView.findViewById<TextView>(R.id.points)


    }


}

