package ninja11.fantasy.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ninja11.fantasy.R
import ninja11.fantasy.UpdateApplicationActivity
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.DownloadController
//import com.paytm.pgsdk.PaytmOrder
//import com.paytm.pgsdk.PaytmPGService
//import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import ninja11.fantasy.databinding.FragmentUpdateApkBinding

class UpdateAppDialogFragment(
    val updateApkUrl: String,
    val releaseNote: String
) : DialogFragment() {

    private var mBinding: FragmentUpdateApkBinding? = null
    lateinit var downloadController: DownloadController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_update_apk, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var customeProgressDialog = CustomeProgressDialog(activity)

        downloadController = DownloadController(activity!!, updateApkUrl,customeProgressDialog)
        mBinding!!.imgClose.setOnClickListener(View.OnClickListener {
            dismiss()
        })
        mBinding!!.updateApk.setOnClickListener(View.OnClickListener {

            checkStoragePermission()
        })

        mBinding!!.releaseNote.setText(releaseNote)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == UpdateApplicationActivity.PERMISSION_REQUEST_STORAGE) {
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

            if(activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
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
                    UpdateApplicationActivity.PERMISSION_REQUEST_STORAGE
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    UpdateApplicationActivity.PERMISSION_REQUEST_STORAGE
                )
            }

        }




    }

}