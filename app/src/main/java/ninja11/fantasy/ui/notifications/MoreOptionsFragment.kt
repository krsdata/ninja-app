package ninja11.fantasy.ui.notifications

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.*
import ninja11.fantasy.models.MoreOptionsModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.ui.login.LoginScreenActivity
import ninja11.fantasy.*
import ninja11.fantasy.databinding.FragmentMoreoptionsBinding
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MoreOptionsFragment : Fragment() {
    companion object{
        val REQUEST_CODE_LOGIN: Int = 1003
    }

    private var mBinding: FragmentMoreoptionsBinding? = null
    lateinit var adapter: MoreOptionsAdaptor
    var allOptionsList = ArrayList<MoreOptionsModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_moreoptions, container, false
        );
        return mBinding!!.getRoot();
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showToolbar()
        mBinding!!.appVersion.setText(MyUtils.getAppVersionName(activity!!))
        mBinding!!.recyclerMoreoptions.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)


        initContent()

        mBinding!!.loader.visibility = View.VISIBLE
        BackgroundLoading().execute()
    }




    fun logoutApp(){
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        var userId = MyPreferences.getUserID(activity!!)!!
        if (!TextUtils.isEmpty(userId)) {
            mBinding!!.loader.visibility = View.VISIBLE
            var request = RequestModel()
            request.user_id = userId
            WebServiceClient(activity!!).client.create(IApiMethod::class.java).logout(request)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {

                        mBinding!!.loader.visibility = View.GONE
                        MyPreferences.clear(activity!!)
                        val intent = Intent(
                            activity!!,
                            SplashScreenActivity::class.java
                        )
                        startActivity(intent)
                        activity!!.finish()
                    }

                })
        }else {
            val intent = Intent(
                activity!!,
                LoginScreenActivity::class.java
            )
            startActivityForResult(intent,REQUEST_CODE_LOGIN)

        }
    }

    private fun initContent() {
        allOptionsList.clear()

        var upcomingMModle1 = MoreOptionsModel()
        upcomingMModle1.drawable = R.drawable.refer
        upcomingMModle1.id = 0
        upcomingMModle1.title = "Refer & Earn"
        allOptionsList.add(upcomingMModle1)

        var upcomingMModle7 = MoreOptionsModel()
        upcomingMModle7.drawable = R.drawable.support
        upcomingMModle7.id = 6
        upcomingMModle7.title = "SkoreOn Support Team"
        allOptionsList.add(upcomingMModle7)


        var upcomingMModle2 = MoreOptionsModel()
        upcomingMModle2.drawable = R.drawable.points
        upcomingMModle2.id = 1
        upcomingMModle2.title = "Fantasy Points System"
        allOptionsList.add(upcomingMModle2)


        var upcomingMModle3 = MoreOptionsModel()
        upcomingMModle3.drawable = R.drawable.play
        upcomingMModle3.id = 2
        upcomingMModle3.title = "How to Play"
        allOptionsList.add(upcomingMModle3)

        var upcomingMModle4 = MoreOptionsModel()
        upcomingMModle4.drawable = R.drawable.about
        upcomingMModle4.id = 3
        upcomingMModle4.title = "About Us"
        allOptionsList.add(upcomingMModle4)

        var upcomingMModle5 = MoreOptionsModel()
        upcomingMModle5.drawable = R.drawable.legality
        upcomingMModle5.id = 4
        upcomingMModle5.title = "Legality"
        allOptionsList.add(upcomingMModle5)

        var upcomingMModle6 = MoreOptionsModel()
        upcomingMModle6.drawable = R.drawable.terms
        upcomingMModle6.id = 5
        upcomingMModle6.title = "Terms and Conditions"
        allOptionsList.add(upcomingMModle6)

//        var upcomingMModle9 = MoreOptionsModel()
//        upcomingMModle9.drawable = R.drawable.ic_chat_black_24dp
//        upcomingMModle9.id = 9
//        upcomingMModle9.title = "Chat with Sports Fight"
//        allOptionsList.add(upcomingMModle9)

        var upcomingMModle8 = MoreOptionsModel()
        upcomingMModle8.drawable = R.drawable.logout
        upcomingMModle8.id = 8
        var userId = MyPreferences.getUserID(activity!!)!!
        if (!TextUtils.isEmpty(userId)) {
            upcomingMModle8.title = "Logout"
        }else {
            upcomingMModle8.title = "Login"
        }

        allOptionsList.add(upcomingMModle8)






    }

    inner class MoreOptionsAdaptor(
        val context: Context,
        val tradeinfoModels: ArrayList<MoreOptionsModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((MoreOptionsModel) -> Unit)? = null
        private var optionListObject = tradeinfoModels


        override fun getItemViewType(position: Int): Int {
            return super.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_more_options, parent, false)
            return DataViewHolder(view)

        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = optionListObject!![viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder?.optionsTitle?.text = objectVal.title
            viewHolder?.optionIcon.setImageResource(objectVal.drawable)
        }


        override fun getItemCount(): Int {
            return optionListObject!!.size
        }


        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(optionListObject!![adapterPosition])
                }
            }

            val optionsTitle = itemView.findViewById<TextView>(R.id.options_title)
            val optionIcon = itemView.findViewById<ImageView>(R.id.option_icon)
        }


    }



    inner class BackgroundLoading : AsyncTask<Unit, Unit, String>() {

        override fun doInBackground(vararg params: Unit): String {
             Thread.sleep(200)
            return ""
        }
        override fun onPostExecute(result: String) {
            mBinding!!.loader.visibility = View.INVISIBLE

            var itemDecoration = DividerItemDecoration(activity!!, VERTICAL);
            mBinding!!.recyclerMoreoptions.addItemDecoration(itemDecoration)
            adapter = MoreOptionsAdaptor(activity!!, allOptionsList)
            mBinding!!.recyclerMoreoptions.adapter = adapter
            adapter!!.onItemClick = { objects ->

                when(objects.id){
                    0->{
                        val intent = Intent(activity!!, InviteFriendsActivity::class.java)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }
                    1->{
                        val intent = Intent(activity!!, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }
                    2->{
                        val intent = Intent(activity!!, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_HOW_TO_PLAY)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }
                    3->{
                        val intent = Intent(activity!!, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_ABOUT_US)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }
                    4->{
                        val intent = Intent(activity!!, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_LEGALITY)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }
                    5->{
                        val intent = Intent(activity!!, WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }

                    6->{
                        val intent = Intent(activity!!, SupportActivity::class.java)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options =
                                ActivityOptions.makeSceneTransitionAnimation(activity)
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                    }

//                    9->{
//                        val intent = Intent(activity!!, WebActivity::class.java)
//                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_CHAT)
//                        if (Build.VERSION.SDK_INT > 20) {
//                            val options =
//                                ActivityOptions.makeSceneTransitionAnimation(activity)
//                            startActivity(intent, options.toBundle())
//                        } else {
//                            startActivity(intent)
//                        }
//                    }

                    8->{
                        logoutApp()
                    }


                }
            }


        }

    }
}