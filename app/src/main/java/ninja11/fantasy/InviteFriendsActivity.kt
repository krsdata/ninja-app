package ninja11.fantasy
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.databinding.InviteFriendsBinding
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.utils.BindingUtils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class InviteFriendsActivity : AppCompatActivity() {

    var userInfo: UserInfo?=null

    private var mBinding: InviteFriendsBinding? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.invite_friends)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.invite_friends
        )
        mBinding!!.setOnRefernEarn(OnClickListners())
        mBinding!!.imageBack.setOnClickListener(View.OnClickListener { finish() })

//        mBinding!!.toolbar.setTitle("Reffer & Earn")
//        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(mBinding!!.toolbar)
//        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })

         userInfo = (application as SportsFightApplication).userInformations
        //findViewById<TextView>(R.id.invitecode).setText("Your Referral Code is "+userInfo.referalCode)

        mBinding!!.rereralCode.setText(""+userInfo!!.referalCode)
        mBinding!!.moreOptions.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val msgText: String = ("" +
                        "Register on SkoreOn application with referral code " +
                        "*"+userInfo!!.referalCode+"*"+
                        " and get Rs 100 Bonus on Joining.\n" +
                        " Click on " +
                        BindingUtils.BILTY_APK_LINK)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, msgText)
                shareIntent.type = "text/plain"


                startActivity(Intent.createChooser(shareIntent,"Reffer and Earn Rs 100"))

            }


        })
       // setAnimation()
    }

    fun  setAnimation() {
        if (Build.VERSION.SDK_INT > 20) {
            var slide = Slide()
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(400);
            slide.setInterpolator(DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }


    inner class OnClickListners  {
        fun onInvite(view: View?): Unit {
            val msgText: String = ("" +
                    "Register on SkoreOn application with referral code " +
                    "*"+userInfo!!.referalCode+"*"+
                    " and get Rs 100 Bonus on Joining.\n" +
                    " Click on " +
                    BindingUtils.BILTY_APK_LINK)

            val sendIntent = Intent()
            sendIntent.setAction(Intent.ACTION_SEND)
            sendIntent.setType("text/plain")
            sendIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "DeliverDas Share & Earn 51 Rs."
            )
            sendIntent.putExtra(Intent.EXTRA_TEXT, msgText)
            try {
               startActivity(sendIntent)
            } catch (ex: ActivityNotFoundException) {
            }
        }

        fun onWhatsApp(view: View?): Unit {
            val pm: PackageManager = getPackageManager()
            try {
                val msgText: String = ("" +
                        "Register on SkoreOn application with referral code " +
                        "*"+userInfo!!.referalCode+"*"+
                        " and get Rs 100 Bonus on Joining.\n" +
                        " Click on " +
                        BindingUtils.BILTY_APK_LINK)
                val waIntent = Intent(Intent.ACTION_SEND)
                waIntent.setType("text/plain")
                val info: PackageInfo =
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA)
                //Check if package exists or not. If not then code
                //in catch block will be called
                waIntent.setPackage("com.whatsapp")
                waIntent.putExtra(Intent.EXTRA_TEXT, msgText)
                startActivity(
                    Intent
                        .createChooser(
                            waIntent,
                            "Please join us with " + userInfo!!.referalCode
                        )
                )
            } catch (e: PackageManager.NameNotFoundException) {
            }
        }

        fun onFacebook(view: View?): Unit {
            val msgText: String = ("" +
                    "Register on SkoreOn application with referral code " +
                    "*"+userInfo!!.referalCode+"*"+
                    " and get Rs 100 Bonus on Joining.\n" +
                    " Click on " +
                    BindingUtils.BILTY_APK_LINK)
            var facebookAppFound: Boolean = false
            var shareIntent: Intent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_TEXT, msgText)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse(BindingUtils.BILTY_APK_LINK)
            )
            val pm: PackageManager = getPackageManager()
            val activityList: List<ResolveInfo> =
                pm.queryIntentActivities(shareIntent, 0)
            for (app: ResolveInfo in activityList) {
                if ((app.activityInfo.packageName).contains("com.facebook.katana")) {
                    val activityInfo: ActivityInfo = app.activityInfo
                    val name: ComponentName =
                        ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name)
                    shareIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                    shareIntent.setComponent(name)
                    facebookAppFound = true
                    break
                }
            }
            if (!facebookAppFound) {
                val sharerUrl: String =
                    "https://www.facebook.com/sharer/sharer.php?u=" + BindingUtils.BILTY_APK_LINK

                shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl))
            }
            startActivity(shareIntent)
        }

        fun onTwitter(view: View?): Unit {
            val msgText: String = ("" +
                    "Register on SkoreOn application with referral code " +
                    "*"+userInfo!!.referalCode+"*"+
                    " and get Rs 100 Bonus on Joining.\n" +
                    " Click on " +
                    BindingUtils.BILTY_APK_LINK)
            val tweetUrl: StringBuilder =
                StringBuilder("https://twitter.com/intent/tweet?text=")
            tweetUrl.append(TextUtils.isEmpty(msgText))

            if (!TextUtils.isEmpty(msgText)) {
                tweetUrl.append("&hastags=")
                tweetUrl.append(
                    urlEncode(
                        msgText
                    )
                )
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl.toString()))
            val matches: List<ResolveInfo> =
                getPackageManager().queryIntentActivities(intent, 0)
            for (info: ResolveInfo in matches) {
                if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                    intent.setPackage(info.activityInfo.packageName)
                }
            }
            startActivity(intent)
        }

        fun urlEncode(s: String): String? {
            return try {
                URLEncoder.encode(s, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                Log.wtf("wtf", "UTF-8 should always be supported", e)
                throw RuntimeException("URLEncoder.encode() failed for $s")
            }
        }
    }



}