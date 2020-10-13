package ninja11.fantasy.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import ninja11.fantasy.R
import ninja11.fantasy.SportsFightApplication
import ninja11.fantasy.adaptors.MatchesAdapter
import ninja11.fantasy.listener.RecyclerViewLoadMoreScroll
import ninja11.fantasy.models.MatchesModels
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.BindingUtils
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja11.fantasy.databinding.FragmentAllGamesBinding

//import skoreon.fantasy.databinding.FragmentAllGamesBinding


class FixtureCricketFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    //var listener : OnPageRefreshedListener? =null
    companion object {
        fun newInstance() = FixtureCricketFragment()
        var pageNo = 1
    }


    //   private lateinit var mainViewModel: MatchesViewModel
    private var mBinding: FragmentAllGamesBinding? = null
    lateinit var adapter: MatchesAdapter
    var allmatchesArrayList = ArrayList<MatchesModels>()
    var scrollListener: RecyclerViewLoadMoreScroll? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_games, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // mainViewModel = ViewModelProviders.of(this).get(MatchesViewModel::class.java)
        //mainViewModel = ViewModelProviders.of(this).get(MatchesViewModel::class.java)
        getAllMatches()
        mBinding!!.allGameViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        mBinding!!.linearEmptyContest.visibility = View.GONE
        //   mBinding!!.allGameViewRecycler.addItemDecoration(EqualSpacing(16, context))
        mBinding!!.swipeRefresh.setColorScheme(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        mBinding!!.swipeRefresh.setOnRefreshListener(this);
        // initDummyContent();
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager)

        mBinding!!.allGameViewRecycler.layoutManager = linearLayoutManager

        var upcomingmatchlist =
            (activity!!.applicationContext as SportsFightApplication).getUpcomingMatches
        if (upcomingmatchlist != null && upcomingmatchlist.size > 0) {
            allmatchesArrayList.clear()
            allmatchesArrayList.addAll(upcomingmatchlist)
        }
        adapter = MatchesAdapter(activity!!, allmatchesArrayList)
        mBinding!!.allGameViewRecycler.adapter = adapter

//        scrollListener!!.setOnLoadMoreListener(object : OnLoadMoreListener {
//            override fun onLoadMore() {
//                MyUtils.showToast(activity!! as AppCompatActivity,"Load more is called")
//                LoadMoreData()
//            }
//
//        })

        getAllMatches()
    }

//    private fun LoadMoreData() {
//        adapter!!.addLoadingView()
//        Handler().postDelayed({
//            adapter!!.removeLoadingView()
//            scrollListener!!.setLoaded()
//            if(MyUtils.isConnectedWithInternet(activity as AppCompatActivity)){
//                if(isValidRequest()) {
//                    ++pageNo
//                    //mainViewModel.pageNumber = pageNo
//                    getAllMatches()
//                }
//            }
//
//
//        }, 5000)
//    }

    private fun isValidRequest(): Boolean {

        var offset = 10;
        var cal = (pageNo * offset) + 1
        if (adapter!!.itemCount <= cal) {
            return true
        } else {
            return true
        }
    }

    fun updateEmptyViews() {
        if (allmatchesArrayList.size == 0) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
            mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(BindingUtils.WEBVIEW_TNC)
                startActivity(openURL)
            })

        } else {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
    }


    fun getAllMatches() {
//        if(!isVisible()){
//            return
//        }
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            mBinding!!.swipeRefresh.isRefreshing = false
//            val mSnackbar =
//                Snackbar.make(mBinding!!.linearEmptyContest, , Snackbar.LENGTH_LONG)
//                    .setAction("RETRY") {
//                        getAllMatches()
//                    }
//            mSnackbar.show()
            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                "NO Internet Connection found!!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Retry") {
                // Call action functions here
                getAllMatches()
            }.setActionTextColor(resources.getColor(R.color.red)).show()
            return
        }
        mBinding!!.swipeRefresh.isRefreshing = true
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        models.token =MyPreferences.getToken(activity!!)!!

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getAllMatches(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    // customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (isVisible) {
                        mBinding!!.swipeRefresh.isRefreshing = false
                        var resObje = response!!.body()
                        if (resObje != null) {
                            BindingUtils.currentTimeStamp = resObje.systemTime
                            var responseObject = resObje.responseObject
                            var listofData =
                                responseObject!!.matchdatalist as ArrayList<MatchesModels>?
                            (activity!!.applicationContext as SportsFightApplication).saveUpcomingMatches(
                                listofData
                            )
                            if (listofData!!.size > 0) {
                                addAllList(listofData)
                                adapter!!.setMatchesList(allmatchesArrayList)
                                //  MyUtils.showToast(activity!! as AppCompatActivity,allmatchesArrayList.size.toString())
                            }
                        }
                        updateEmptyViews()

                    }
                }

            })

    }
//     fun getAllMatches() {
//        //listener!!.onPageCreated("Cricket")
//
//        // var userInfo = ((activity as Application) as PlugSportsApplication).userInformations
//         var models = RequestModel()
//         models.user_id = MyPreferences.getUserID(activity!!)!!
//         models.token =MyPreferences.getToken(activity!!)!!
//        mainViewModel.getAllMatches(models).observe(
//            this,
//            object : Observer<List<MatchesModels?>?> {
//
//                override fun onChanged(usersPostList: List<MatchesModels?>?) {
//                    //listener!!.onRefreshed("Cricket")
//                    if(isVisible) {
//                        mBinding!!.swipeRefresh.isRefreshing = false
//                        if (allmatchesArrayList != null && allmatchesArrayList.size > 0) {
//
//                            updateEmptyViews()
//                            addAllList(usersPostList as java.util.ArrayList<MatchesModels>)
//                            MyUtils.logd(
//                                "pageNo",
//                                "" + pageNo + " Size of List " + usersPostList!!.size
//                            )
//                            adapter!!.setMatchesList(allmatchesArrayList)
//                        }
//                        updateEmptyViews()
//                    }
//                }
//            })
//    }

    private fun addAllList(userPostData: java.util.ArrayList<MatchesModels>) {
        if (isValidRequest()) {
            allmatchesArrayList.clear()
            allmatchesArrayList.addAll(userPostData)
        }
    }

//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        if (activity is OnPageRefreshedListener) {
//            Log.d("Annv - Fragment", "activity " + activity.localClassName)
//           listener = activity as OnPageRefreshedListener
//        }
//    }

    override fun onRefresh() {
        getAllMatches()
    }
}
