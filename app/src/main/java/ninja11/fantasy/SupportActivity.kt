package ninja11.fantasy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.databinding.ActivitySupportsBinding
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.utils.BindingUtils

class SupportActivity : AppCompatActivity() {

    var userInfo: UserInfo?=null
    private var mBinding: ActivitySupportsBinding? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_supports
        )

       /* val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/


        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })



//        mBinding!!.toolbar.setTitle("Support Team")
//        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(mBinding!!.toolbar)
//        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })


        mBinding!!.callPhone.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.fromParts(
                        "tel",
                        BindingUtils.PHONE_NUMBER,
                        null
                    )
                )
                startActivity(intent)

            }


        })

        mBinding!!.callSms.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

               try {
                    val uri =
                        Uri.parse("smsto:" + BindingUtils.PHONE_NUMBER)
                    val it = Intent(Intent.ACTION_SENDTO, uri)
                    it.putExtra("sms_body", "")
                    startActivity(it)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@SupportActivity,
                        "Sms not send" + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        })

        mBinding!!.callEmail.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val emailIntent = Intent(
                    Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", BindingUtils.EMAIL, null
                    )
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact SkoreOn Support Team")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "")
                startActivity(Intent.createChooser(emailIntent, "Send email..."))

            }
        })
//
//        mBinding!!.chatWithUs.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View?) {
//
//                val intent = Intent(this@SupportActivity, WebActivity::class.java)
//                intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_CHAT)
//                if (Build.VERSION.SDK_INT > 20) {
//                    val options =
//                        ActivityOptions.makeSceneTransitionAnimation(this@SupportActivity)
//                    startActivity(intent, options.toBundle())
//                } else {
//                    startActivity(intent)
//                }
//
//            }
//        })


        mBinding!!.callWhatsapp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                raiseIssuesOnWhatsApp();

            }
        })

    }

    fun raiseIssuesOnWhatsApp() {
        try {
            val text = ""
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("http://api.whatsapp.com/send?phone=${BindingUtils.PHONE_NUMBER}&text=$text")
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


}