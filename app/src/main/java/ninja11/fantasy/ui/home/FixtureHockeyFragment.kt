package ninja11.fantasy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja11.fantasy.R
import ninja11.fantasy.adaptors.MatchesAdapter
import ninja11.fantasy.models.MatchesModels
import ninja11.fantasy.databinding.FragmentAllGamesBinding


class FixtureHockeyFragment : Fragment() {

    private var mBinding: FragmentAllGamesBinding? = null
    lateinit var adapter: MatchesAdapter
    var checkinArrayList = ArrayList<MatchesModels>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_all_games, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.allGameViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)


        initDummyContent();

        adapter =
            MatchesAdapter(activity!!, checkinArrayList)
        mBinding!!.allGameViewRecycler.adapter = adapter

    }

    private fun initDummyContent() {
//        checkinArrayList.clear()
//        var matchModels2 = MatchesModels()
//        matchModels2.viewType = MatchesAdapter.TYPE_BANNERS
//        matchModels2.matchBanners = ArrayList()
//        matchModels2.matchBanners!!.add(MatchBannersModel())
//        matchModels2.matchBanners!!.add(MatchBannersModel())
//        checkinArrayList.add(matchModels2)
//
//        var matchModels3 = MatchesModels()
//        matchModels3.viewType = MatchesAdapter.TYPE_UPCOMING_MATCHES
//        checkinArrayList.add(matchModels3)
    }
}
