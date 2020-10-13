package ninja11.fantasy.ui.dashboard
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import ninja11.fantasy.MainActivity
import ninja11.fantasy.R
import ninja11.fantasy.ui.mymatches.MyCompletedMatchesFragment
import ninja11.fantasy.ui.mymatches.MyLiveMatchesFragment
import ninja11.fantasy.ui.mymatches.MyUpcomingMatchesFragment

class MyMatchesFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_mymatches, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showToolbar()
        val viewpager: ViewPager = view.findViewById(R.id.viewpager)
        val tabs: TabLayout = view.findViewById(R.id.tabs)
        setupViewPager(viewpager)
        // viewpager.addOnPageChangeListener(this)
        tabs.setupWithViewPager(viewpager)

    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(activity!!.supportFragmentManager)
        adapter.addFragment(MyUpcomingMatchesFragment(),"Upcoming")
        adapter.addFragment(MyLiveMatchesFragment(),"Live")
        adapter.addFragment(MyCompletedMatchesFragment(),"Completed")
        viewPager.adapter = adapter
    }

    fun onRefresh(){

    }
    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager,
        FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }
}