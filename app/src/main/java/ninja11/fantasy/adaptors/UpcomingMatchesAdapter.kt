package ninja11.fantasy.adaptors

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.InternalCoroutinesApi
import ninja11.fantasy.R
import ninja11.fantasy.listener.OnMatchTimerStarted
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.utils.BindingUtils
import java.util.*


class UpcomingMatchesAdapter(val context: Context, val tradeinfoModels: ArrayList<UpcomingMatchesModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((UpcomingMatchesModel) -> Unit)? = null
    private var matchesListObject =  tradeinfoModels


    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.matches_row_upcoming_inner, parent, false)
        return DataViewHolder(view)

    }

    @InternalCoroutinesApi
    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        val viewHolder: DataViewHolder = parent as DataViewHolder
        if(objectVal.isLineup) {
            viewHolder?.matchTitle?.visibility = View.VISIBLE
        }else {
            viewHolder?.matchTitle?.visibility = View.GONE
        }
       viewHolder?.tournamentTitle?.text = objectVal.leagueTitle
       // viewHolder?.matchProgress?.text = ""+objectVal.timestampEnd
        viewHolder?.opponent1?.text = objectVal.teamAInfo!!.teamShortName
        viewHolder?.opponent2?.text = objectVal.teamBInfo!!.teamShortName


//        if(viewType %2 == 1)
//        {
//            viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
//
//        }
//        else
//        {
//            viewHolder.itemView.setBackgroundColor(Color.parseColor("#32324E"))
//
//        }

        if(objectVal.freeContest) {
            viewHolder?.freeView?.visibility = View.VISIBLE
        }
        else {
            viewHolder?.freeView?.visibility = View.GONE
        }
        if(!TextUtils.isEmpty(objectVal.dateStart)) {
            viewHolder?.matchtime.visibility = View.VISIBLE
            viewHolder?.matchtime.setText(objectVal.dateStart)
        }else {
            viewHolder?.matchtime.visibility = View.GONE
        }
        viewHolder?.teamAColorView?.setBackgroundColor(getRandomColor());
        viewHolder?.teamBColorView?.setBackgroundColor(getRandomColor());


        BindingUtils.countDownStartForAdaptors(objectVal.timestampStart,object :
            OnMatchTimerStarted {
            override fun onTimeFinished() {
                viewHolder?.matchProgress.setText(objectVal.statusString)
            }

            override fun onTicks(time:String) {
                viewHolder?.matchProgress.setText(time)
            }

        })
        if (!TextUtils.isEmpty(objectVal.contestName)) {
            viewHolder?.upcomingLinearContestView.visibility = View.VISIBLE;
            viewHolder?.contestName?.text = "" + objectVal.contestName
            viewHolder?.contestPrice?.text = "" + objectVal.contestPrize
        } else {
            viewHolder?.upcomingLinearContestView.visibility = View.INVISIBLE;
        }

        Glide.with(context)
            .load(objectVal.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(viewHolder?.teamALogo)


        Glide.with(context)
            .load(objectVal.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.flag_indian)
            .into(viewHolder?.teamBLogo)



    }

    fun getRandomColor():Int {
        val rnd = Random()
        val color: Int = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

        return color
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
        val teamALogo = itemView.findViewById<ImageView>(R.id.teama_logo)
        val teamBLogo = itemView.findViewById<ImageView>(R.id.teamb_logo)
        val matchTitle = itemView.findViewById<TextView>(R.id.upcoming_match_title)
        val tournamentTitle = itemView.findViewById<TextView>(R.id.tournament_title)
        val teamAColorView = itemView.findViewById<View>(R.id.countrycolorview)
        val teamBColorView = itemView.findViewById<View>(R.id.countrybcolorview)
        val opponent1 = itemView.findViewById<TextView>(R.id.upcoming_opponent1)
        val opponent2 = itemView.findViewById<TextView>(R.id.upcoming_opponent2)
        val freeView = itemView.findViewById<TextView>(R.id.free_view)
        val matchtime = itemView.findViewById<TextView>(R.id.match_time)
        val matchProgress = itemView.findViewById<TextView>(R.id.upcoming_match_progress)
        val upcomingLinearContestView = itemView.findViewById<LinearLayout>(R.id.upcoming_linear_contest_view)
        val contestName = itemView.findViewById<TextView>(R.id.upcoming_contest_name)
        val contestPrice = itemView.findViewById<TextView>(R.id.upcoming_contest_price)

     }


}


