package ninja11.fantasy.adaptors

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ninja11.fantasy.ContestActivity
import ninja11.fantasy.MainActivity
import ninja11.fantasy.R
import ninja11.fantasy.models.MatchesModels
import ninja11.fantasy.adaptors.UpcomingMatchesAdapter
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyUtils


class MatchesAdapter(val context: Context, val tradeinfoModels: ArrayList<MatchesModels>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((MatchesModels) -> Unit)? = null
    var mContext:Context ? =context
    private var matchesListObject =  tradeinfoModels

    companion object {
        const val TYPE_JOINED = 1
        const val TYPE_BANNERS = 2
        const val TYPE_UPCOMING_MATCHES = 3
    }

    override fun getItemViewType(position: Int): Int {
        val comparable = matchesListObject[position]
            return comparable.viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType== TYPE_JOINED){
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.matches_row_joined_matches, parent, false)
            return ViewHolderJoinedMatches(view)
        }else if(viewType== TYPE_BANNERS){
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.matches_row_banners_matches, parent, false)
            return BannersViewHolder(view)
        }else if(viewType== TYPE_UPCOMING_MATCHES){
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.matches_row_upcoming_matches, parent, false)
            return UpcomingMatchesViewHolder(view)
        }
        return null!!
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        if(objectVal.viewType== TYPE_JOINED){
            val viewJoinedMatches: ViewHolderJoinedMatches = parent as ViewHolderJoinedMatches
            viewJoinedMatches.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)



            val adapter = JoinedMatchesAdapter(
                mContext!!,
                objectVal.joinedMatchModel!!
            )

            Glide.with(context)
                .load(BindingUtils.BASE_URL+"banners/joined_contest_bg.jpg")
                .into(viewJoinedMatches?.backgroundImage)

            viewJoinedMatches.txtViewAll.setOnClickListener {
                (mContext as MainActivity).viewAllMatches()
            }
            viewJoinedMatches.recyclerView.adapter = adapter
            adapter.onItemClick = { objects ->
                //MyUtils.logd("MatchesAdapter","Joined Contest"+objects.country1Name+" Vs "+objects.country1Name)
                val intent = Intent(mContext, ContestActivity::class.java)
                intent.putExtra(ContestActivity.SERIALIZABLE_KEY_JOINED_CONTEST,objects)
                mContext!!.startActivity(intent)
           //     MyUtils.showToast(parent.recyclerView,"Open New Joined Contest Activity")
            }


        }else  if(objectVal.viewType== TYPE_BANNERS){
            val objectVal = matchesListObject!![viewType]
            val viewBanners: BannersViewHolder = parent as BannersViewHolder
            viewBanners.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            val adapter = BannersMatchesAdapter(
                mContext!!,
                objectVal.matchBanners!!
            )
            viewBanners.recyclerView.adapter = adapter


        }else  if(objectVal.viewType== TYPE_UPCOMING_MATCHES) {
            val objectVal = matchesListObject!![viewType]
            val viewUpcomingMatches: UpcomingMatchesViewHolder = parent as UpcomingMatchesViewHolder
            viewUpcomingMatches.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)

            if (objectVal.upcomingMatches != null && objectVal.upcomingMatches!!.size > 0) {
                viewUpcomingMatches.linearEmptyView.visibility = GONE


                val adapter = UpcomingMatchesAdapter(
                    mContext!!,
                    objectVal.upcomingMatches!!
                )
                viewUpcomingMatches.recyclerView.adapter = adapter
                adapter.onItemClick = { objects ->

                    //MyUtils.logd("MatchesAdapter",objects.country1Name+" Vs "+objects.country1Name)
                    val intent = Intent(mContext, ContestActivity::class.java)
                    intent.putExtra(ContestActivity.SERIALIZABLE_KEY_UPCOMING_MATCHES,objects)
                    mContext!!.startActivity(intent)
                }

            } else {
                MyUtils.logd("ADaptor", "Draw Empty View Here")
                viewUpcomingMatches.linearEmptyView.visibility = VISIBLE
            }
        }
    }

    fun setMatchesList(matchesList: java.util.ArrayList<MatchesModels>?) {
        this.matchesListObject = matchesList!!
       // this.mContext = mContext
        notifyDataSetChanged()
    }

    fun addLoadingView() {
        Handler().post {
            matchesListObject!!.add(MatchesModels())
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

    inner  class ViewHolderJoinedMatches(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_joined_matches)
        val txtViewAll = itemView.findViewById<TextView>(R.id.txtViewAll)
        val backgroundImage = itemView.findViewById<ImageView>(R.id.imageView4)
    }


    inner  class BannersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_banners)
    }

    inner  class UpcomingMatchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_upcoming_matches)
        val linearEmptyView = itemView.findViewById<LinearLayout>(R.id.linear_empty_view)

    }




}

