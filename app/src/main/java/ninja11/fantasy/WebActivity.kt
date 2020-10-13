package ninja11.fantasy

import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity


class WebActivity : AppCompatActivity() {
    companion object{
        val KEY_URL:String = ""
    }
    var mWebview: WebView? = null
    var URL: String? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        setEnterAnimations()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        mWebview = findViewById<View>(R.id.web_body) as WebView
        findViewById<View>(R.id.image_back).setOnClickListener(View.OnClickListener {
            finish()

        })
        URL = intent.getStringExtra(KEY_URL)
        loadURL()
    }

    fun  setEnterAnimations() {
        if (Build.VERSION.SDK_INT > 20) {
            var slide = Slide()
            slide.setSlideEdge(Gravity.BOTTOM);
            slide.setDuration(400);
            slide.setInterpolator(DecelerateInterpolator())
            getWindow().setExitTransition(slide)
            getWindow().setEnterTransition(slide)
        }
    }


//
//    override fun finish() {
//        super.finish()
//        overridePendingTransition(R.anim.hold, R.anim.grow_linear_animation);
//    }

    /**
     * Load url.
     */
    fun loadURL() {
        mWebview!!.webViewClient = MyWebViewClient()
        mWebview!!.settings.javaScriptEnabled = true
        mWebview!!.loadUrl(URL)
    }



    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            // showProgress(false, "");
        }
    }
}