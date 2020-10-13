package  ninja11.fantasy.adaptors

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ninja11.fantasy.CreateTeamActivity
import ninja11.fantasy.R
import ninja11.fantasy.SelectTeamActivity
import ninja11.fantasy.models.MyJoinedTeamModels
import ninja11.fantasy.models.UpcomingMatchesModel
import ninja11.fantasy.ui.contest.MyTeamFragment

import ninja11.fantasy.utils.CustomeProgressDialog
import kotlin.collections.ArrayList


class ClosedTeamsAdapter(
    val context: SelectTeamActivity,
    val matchObject: UpcomingMatchesModel?,
    val customeProgressDialog: CustomeProgressDialog,
    val tradeinfoModels: ArrayList<MyJoinedTeamModels>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((MyJoinedTeamModels) -> Unit)? = null
    private var matchesListObject = tradeinfoModels


    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return matchesListObject!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.myteam_rows, parent, false)
        return MyMatchViewHolder(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        val viewHolder: MyMatchViewHolder =
            parent as MyMatchViewHolder
        viewHolder.userTeamName.setText(objectVal.teamName)
        viewHolder.teamaName.setText(objectVal.teamsInfo!!.get(0).teamName)
        viewHolder.teambName.setText(objectVal.teamsInfo!!.get(1).teamName)

        viewHolder.teamaCount.setText("" + objectVal.teamsInfo!!.get(0).count)
        viewHolder.teambCount.setText("" + objectVal.teamsInfo!!.get(1).count)

        viewHolder.trumpPlayerName.setText(objectVal.trump!!.playerName)
        viewHolder.captainPlayerName.setText(objectVal.captain!!.playerName)
        viewHolder.vcPlayerName.setText(objectVal.viceCaptain!!.playerName)

        viewHolder.countWicketkeeper.setText(String.format("%d", objectVal.wicketKeepers!!.size))
        viewHolder.countBatsman.setText(String.format("%d", objectVal.batsmen!!.size))
        viewHolder.countAllRounder.setText(String.format("%d", objectVal.allRounders!!.size))
        viewHolder.countBowler.setText(String.format("%d", objectVal.bowlers!!.size))


        Glide.with(context)
            .load("https://")
            .placeholder(R.drawable.player_blue)
            .into(viewHolder?.trumpImageView)

        Glide.with(context)
            .load("https://")
            .placeholder(R.drawable.player_blue)
            .into(viewHolder?.captainImageView)

        Glide.with(context)
            .load("https://")
            .placeholder(R.drawable.player_blue)
            .into(viewHolder?.vcImageView)

        viewHolder.teamEdit.visibility = View.VISIBLE
        viewHolder.teamCopy.visibility = View.VISIBLE
        viewHolder.teamEdit.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            intent.putExtra(MyTeamFragment.SERIALIZABLE_EDIT_TEAM, objectVal)
            context!!.startActivity(intent)
        })

        viewHolder.teamCopy.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(context, CreateTeamActivity::class.java)
                intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                intent.putExtra(MyTeamFragment.SERIALIZABLE_EDIT_TEAM, objectVal)
                context!!.startActivity(intent)

            }
        })
    }

//    fun copyTeam(teamid: MyJoinedTeamModels.MyTeamId?) {
//        //var userInfo = (activity as PlugSportsApplication).userInformations
//        customeProgressDialog.show()
//        var models = RequestModel()
//        models.user_id = MyPreferences.getUserID(context!!)!!
//        models.match_id =""+matchObject!!.matchId
//        models.team_id = teamid!!.teamId
//
//        WebServiceClient(context!!).client.create(BackEndApi::class.java).copyTeam(models)
//            .enqueue(object : Callback<UsersPostDBResponse?> {
//                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
//                    MyUtils.showToast(context!! as AppCompatActivity,t!!.localizedMessage)
//                }
//
//                override fun onResponse(
//                    call: Call<UsersPostDBResponse?>?,
//                    response: Response<UsersPostDBResponse?>?
//                ) {
//                    customeProgressDialog.dismiss()
//                    context.refreshContents()
//
//                }
//
//            })
//
//    }


    inner  class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(matchesListObject!![adapterPosition])
            }
        }
        val userTeamName = itemView.findViewById<TextView>(R.id.user_team_name)
        val teamEdit = itemView.findViewById<ImageView>(R.id.team_edit)
        val teamCopy = itemView.findViewById<ImageView>(R.id.team_copy)
        val teamShare = itemView.findViewById<ImageView>(R.id.team_share)

        val teamaName = itemView.findViewById<TextView>(R.id.teama_name)
        val teamaCount = itemView.findViewById<TextView>(R.id.teama_count)
        val teambName = itemView.findViewById<TextView>(R.id.teamb_name)
        val teambCount = itemView.findViewById<TextView>(R.id.teamb_count)

        val trumpImageView = itemView.findViewById<ImageView>(R.id.trump_imageView)
        val trumpPlayerName = itemView.findViewById<TextView>(R.id.trump_player_name)

        val captainImageView = itemView.findViewById<ImageView>(R.id.captain_imageView)
        val captainPlayerName = itemView.findViewById<TextView>(R.id.captain_player_name)

        val vcImageView = itemView.findViewById<ImageView>(R.id.vc_imageView)
        val vcPlayerName = itemView.findViewById<TextView>(R.id.vc_player_name)

        val countWicketkeeper = itemView.findViewById<TextView>(R.id.count_wicketkeeper)
        val countBatsman = itemView.findViewById<TextView>(R.id.count_batsman)
        val countAllRounder = itemView.findViewById<TextView>(R.id.count_allrounder)
        val countBowler = itemView.findViewById<TextView>(R.id.count_bowler)


    }


}

