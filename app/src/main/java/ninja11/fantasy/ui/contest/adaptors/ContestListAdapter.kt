package ninja11.fantasy.ui.contest.adaptors

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.LeadersBoardActivity
import ninja11.fantasy.R
import ninja11.fantasy.listener.OnContestEvents
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.ui.contest.models.ContestModelLists


class ContestListAdapter(
    val context: Activity,
    private val contestModelList: ArrayList<ContestModelLists>,
    matchObject: UpcomingMatchesModel,
    val listener: OnContestEvents,
    val colorCode: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((ContestModelLists) -> Unit)? = null
    private var matchesListObject =  contestModelList

    var matchObject = matchObject

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.contest_rows_inner, parent, false)

        return DataViewHolder(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        val viewHolder: DataViewHolder = parent as DataViewHolder
        viewHolder?.contestPrizePool?.text =String.format("%s%d","₹",objectVal.totalWinningPrize)
        viewHolder?.contestEntryPrize?.text = String.format("%s%d","₹",objectVal.entryFees)
        viewHolder?.firstPrize?.text = String.format("%s%d","₹",objectVal.firstPrice)

       viewHolder?.winnerCount?.text = String.format("%s%d","",objectVal.winnerCount)
      //  viewHolder?.maxAllowedTeam?.text = String.format("%s %d %s","Upto",objectVal.maxAllowedTeam,"teams")
        viewHolder?.multiplayer?.text = String.format("%s",objectVal.maxAllowedTeam,"teams")
        viewHolder?.bonusAmount?.text = String.format("%s%d","",objectVal.usable_bonus)

        if (objectVal.maxAllowedTeam.equals(1)){
           viewHolder.multiply.visibility = View.GONE

        }

        if (objectVal.usable_bonus.equals(0)){
            viewHolder.bonus.visibility = View.GONE

        }

        if (objectVal.cancellation.equals("true")){
            viewHolder.contestCancellation.visibility = View.GONE


        }else{

            viewHolder.contestCancellation.visibility = View.VISIBLE
        }

        if(objectVal.totalSpots==0){
            viewHolder.contestProgress.max =objectVal.totalSpots +15
            viewHolder.contestProgress.progress =objectVal.filledSpots
            viewHolder?.totalSpot?.text = String.format("unlimited spots")
            viewHolder?.totalSpotLeft?.text = String.format("%d spot filled",objectVal.filledSpots)
        }else {
            viewHolder.contestProgress.max =objectVal.totalSpots
            viewHolder.contestProgress.progress =objectVal.filledSpots
            if(objectVal.totalSpots==objectVal.filledSpots){
                viewHolder?.totalSpot?.text = "Contest full"
                viewHolder?.totalSpot?.setTextSize(18.0f)
                viewHolder?.totalSpotLeft?.text = ""

            }else {
                viewHolder?.totalSpot?.text = String.format("%d spots", objectVal.totalSpots)
                viewHolder?.totalSpotLeft?.text =
                    String.format("%d spot left", objectVal.totalSpots - objectVal.filledSpots)
            }
        }
       // viewHolder?.cardBackround?.setBackgroundColor(colorCode)
        viewHolder?.linearContestViews?.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, LeadersBoardActivity::class.java)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objectVal)
            context!!.startActivityForResult(intent, LeadersBoardActivity.CREATETEAM_REQUESTCODE)
        })

        viewHolder?.contestCancellation?.setOnClickListener(View.OnClickListener {
            //MyUtils.showToast(viewHolder?.contestCancellation,objectVal.cancellation)
        })

        //if(matchObject.status==1) {
            viewHolder?.contestEntryPrize?.setOnClickListener(View.OnClickListener {
                listener.onContestJoinning(objectVal, viewType)

            })
//        }else {
//            viewHolder?.contestEntryPrize?.setBackgroundResource(R.drawable.button_selector_grey)
//        }

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
       // val cardBackround = itemView.findViewById<CardView>(R.id.card_backround)
        val linearContestViews = itemView.findViewById<LinearLayout>(R.id.linear_trades_status)
        val contestPrizePool = itemView.findViewById<TextView>(R.id.contest_prize_pool)
        val contestEntryPrize = itemView.findViewById<TextView>(R.id.contest_entry_prize)
        val firstPrize = itemView.findViewById<TextView>(R.id.first_prize)
        //val winningPercentage = itemView.findViewById<TextView>(R.id.winning_percentage)
      //  val maxAllowedTeam = itemView.findViewById<TextView>(R.id.max_allowed_team)
        val contestCancellation = itemView.findViewById<TextView>(R.id.contest_cancellation)
        val  winnerCount = itemView.findViewById<TextView>(R.id.winner_count)
        val  multiplayer = itemView.findViewById<TextView>(R.id.mutiplayer)
        val totalSpotLeft = itemView.findViewById<TextView>(R.id.total_spot_left)
        val totalSpot = itemView.findViewById<TextView>(R.id.total_spot)
        val contestProgress = itemView.findViewById<ProgressBar>(R.id.contest_progress)
        val multiply = itemView.findViewById<LinearLayout>(R.id.multiplayTeam)
        val bonus = itemView.findViewById<LinearLayout>(R.id.bonus)
        val bonusAmount = itemView.findViewById<TextView>(R.id.tv_bonus)



    }


}

