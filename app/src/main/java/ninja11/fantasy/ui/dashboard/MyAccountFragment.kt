package ninja11.fantasy.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import ninja11.fantasy.*
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.models.WalletInfo
import skoreon.fantasy.*
import ninja11.fantasy.ui.myaccounts.MyAccountBalanceFragment
import ninja11.fantasy.ui.myaccounts.PlayingHistoryFragment
import ninja11.fantasy.ui.myaccounts.TransactionFragment

import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import ninja11.fantasy.databinding.FragmentMyaccountsBinding

class MyAccountFragment : Fragment() {


    private lateinit var walletInfo: WalletInfo
    private lateinit var userInfo: UserInfo
    private var mBinding: FragmentMyaccountsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_myaccounts, container, false
        )
        return mBinding!!.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userInfo = (activity!!.applicationContext as SportsFightApplication).userInformations
        walletInfo = (activity!!.applicationContext as SportsFightApplication).walletInfo
        (activity as MainActivity)



        val viewpager: ViewPager = view.findViewById(R.id.account_viewpager)
        val tabs: TabLayout = view.findViewById(R.id.account_tabs)
        setupViewPager(viewpager)
        // viewpager.addOnPageChangeListener(this)
        tabs.setupWithViewPager(viewpager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initProfile()
    }
    override fun onResume() {
        super.onResume()
        initProfile()
    }

    public fun initProfile() {
        if(!isVisible){
            return
        }
        if(userInfo!=null) {
            mBinding!!.profileName.setText(userInfo.fullName)
            Glide.with(activity!!)
                .load(MyPreferences.getProfilePicture(activity!!))
                .placeholder(R.drawable.dummy)
                .into(mBinding!!.profileImg)
        }else {
            mBinding!!.profileName.setText("GUEST")
        }

        mBinding!!.btnEditProfile.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, EditProfileActivity::class.java)
            startActivity(intent)
        })

        if(walletInfo!=null){
            var accountStatus = walletInfo.accountStatus
            if(accountStatus!=null){
                if(walletInfo.bankAccountVerified==3){
                    mBinding!!.btnVerifyAccount.setText("REJECTED")
                    mBinding!!.btnVerifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_red)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })

                }else
                if(walletInfo.bankAccountVerified==2){
                    mBinding!!.btnVerifyAccount.setText("Account Verified")
                    mBinding!!.btnVerifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_green)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })
                }else if(walletInfo.bankAccountVerified==1){
                    mBinding!!.btnVerifyAccount.setText("Approval Pending")
                    mBinding!!.btnVerifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_white)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.BLACK)
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })
                }else {
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        val intent = Intent(activity!!, VerifyDocumentsActivity::class.java)
                        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                    })
                }
            }
        }else {
            mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                MyUtils.showToast(activity!! as AppCompatActivity,"verify clicked")
                val intent = Intent(activity!!, VerifyDocumentsActivity::class.java)
                startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
            })
        }

    }

    private fun gotoDocumentsListActivity() {
        val intent = Intent(activity!!, DocumentsListActivity::class.java)
        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = MyAccountViewPagerAdapter(activity!!.supportFragmentManager)
        adapter.addFragment(MyAccountBalanceFragment(this),"Balance")
        adapter.addFragment(PlayingHistoryFragment(this),"Playing History")
        adapter.addFragment(TransactionFragment(this),"Transaction")
        viewPager.adapter = adapter
    }


    internal inner class MyAccountViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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