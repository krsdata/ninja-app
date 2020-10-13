package ninja11.fantasy.adaptors

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ninja11.fantasy.InviteFriendsActivity
import ninja11.fantasy.R
import ninja11.fantasy.SupportActivity
import ninja11.fantasy.models.MatchBannersModel

import ninja11.fantasy.utils.BindingUtils


class BannersMatchesAdapter(val context: Context, val tradeinfoModels: ArrayList<MatchBannersModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((MatchBannersModel) -> Unit)? = null
    private var matchesListObject =  tradeinfoModels


    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.matches_row_banners_inner, parent, false)
        return BannerMatchesViewHolder(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        var objectVal = matchesListObject!![viewType]
        val viewHolder: BannerMatchesViewHolder = parent as BannerMatchesViewHolder

        Glide.with(context)
            .load(objectVal!!.bannerUrl)
            .placeholder(R.drawable.ground_bg)
            .skipMemoryCache(true)
            .into(viewHolder?.imageBanners)

        viewHolder?.imageBanners.setOnClickListener(View.OnClickListener {
            if(objectVal.title.equals(BindingUtils.BANNERS_KEY_REFFER)){
                val intent = Intent(context, InviteFriendsActivity::class.java)
                    context.startActivity(intent)
            }
            if(objectVal.title.equals(BindingUtils.BANNERS_KEY_SUPPORT)){
                val intent = Intent(context, SupportActivity::class.java)
                context.startActivity(intent)
            }

            if(objectVal.title.equals(BindingUtils.BANNERS_KEY_BROWSERS)){

                val urlString = objectVal.description
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setPackage("com.android.chrome")
                try {
                    context.startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    intent.setPackage(null)
                    context.startActivity(intent)
                }
            }
        })

    }



    override fun getItemCount(): Int {
        return matchesListObject!!.size
    }

    inner  class BannerMatchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageBanners = itemView.findViewById<ImageView>(R.id.image_banner)


    }


}

