package ninja11.fantasy

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.databinding.ActivityUpdateApplicationBinding
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.DownloadController


class UpdateApplicationActivity : BaseActivity() {

    private var mBinding: ActivityUpdateApplicationBinding? = null


    companion object{

        val REQUEST_CODE_APK_UPDATE : String = "apkupdateurl"
        val REQUEST_RELEASE_NOTE : String = "release_note"
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    lateinit var downloadController: DownloadController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_update_application
        )
        customeProgressDialog = CustomeProgressDialog(this)

        val apkUrl  = intent.getStringExtra(REQUEST_CODE_APK_UPDATE)
        val releaseNote  = intent.getStringExtra(REQUEST_RELEASE_NOTE)
        if(!TextUtils.isEmpty(releaseNote)) {
            mBinding!!.releaseNote.setText(releaseNote)
        }
        downloadController = DownloadController(this, apkUrl,customeProgressDialog)


       /* val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        mBinding!!.toolbar.setTitle("SkoreOn Update")
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })



        mBinding!!.addCash.setOnClickListener(View.OnClickListener {
//            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
//            startActivity(browserIntent)
            checkStoragePermission()

        })

    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                downloadController.enqueueDownload()
            } else {
                // Permission request was denied.
               // maincontainer.showSnackbar(R.string.storage_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }
    }
    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                downloadController.enqueueDownload()
            }else {
                requestStoragePermission()
            }

        }
    }
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
//                mainLayout.showSnackbar(
//                    R.string.storage_access_required,
//                    Snackbar.LENGTH_INDEFINITE, R.string.ok
//                ) {
//                    requestPermissions(
//                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                        PERMISSION_REQUEST_STORAGE
//                    )
//                }
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }

        }




    }


}
