package ninja11.fantasy.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce
import kotlinx.android.synthetic.main.dialog_progress.*
import ninja11.fantasy.R


class CustomeProgressDialog(context: Context?) : Dialog(context!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

       /* val doubleBounce: Sprite = DoubleBounce()
        spin_kit.setIndeterminateDrawable(doubleBounce)*/
    }

}