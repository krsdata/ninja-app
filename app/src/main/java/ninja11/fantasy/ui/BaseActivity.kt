package ninja11.fantasy.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.anim.FlashAnim
import ninja11.fantasy.utils.HardwareInfoManager
import com.google.firebase.iid.FirebaseInstanceId
import ninja11.fantasy.MainActivity
import ninja11.fantasy.R
import ninja11.fantasy.models.ResponseModel
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


abstract class BaseActivity : AppCompatActivity() {
    var notificationToken: String=""
    lateinit var customeProgressDialog: CustomeProgressDialog
    private var image_uri: Uri? = null
    var mBitmap:Bitmap?  =null
    var mDocumentType = ""

    companion object {
        val DOCUMENTS_TYPE_PROFILES = "profile"
        var DOCUMENT_TYPE_PANCARD = "pancard"
        var DOCUMENT_TYPE_ADHARCARD = "adharcard"
        var DOCUMENT_TYPE_BANK_PASSBOOK = "passbook"
        var DOCUMENT_TYPE_PAYTM = "paytm"
        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        val PICK_IMAGE_REQUEST_CAMERA = 1001
        val PICK_IMAGE_REQUEST_GALLERY = 1002
        private val PERMISSION_CODE = 1001;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(this)
        //uploadReceiver = SingleUploadBroadcastReceiver()
    }

    fun showDeadLineAlert(){
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
       // builder.setTitle("Warning")
        //set message for alert dialog
        builder.setMessage("Deadline has been passed")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("OK"){dialogInterface, which ->
            finish()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    override fun onResume() {
        super.onResume()
        //uploadReceiver!!.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
       // uploadReceiver!!.unregister(this);
    }

    public fun showMatchTimeUpDialog() {
        var flashbar = Flashbar.Builder(this)
            .gravity(Flashbar.Gravity.BOTTOM)
            .title(getString(R.string.app_name))
            .message("Time Up Editing your team, match went to live.")
            .backgroundDrawable(R.color.color_card)
            .showIcon()
            .icon(R.drawable.app_logo)
            .iconAnimation(
                FlashAnim.with(this)
                    .animateIcon()
                    .pulse()
                    .alpha()
                    .duration(750)
                    .accelerate()
            )
            .build()
        flashbar.show()
        Handler().postDelayed(Runnable {
            var intent = Intent(this@BaseActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }, 2000L)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CAMERA) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mBitmap = data!!.extras!!.get("data") as Bitmap
                }else {
                    mBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, image_uri)
                }
                onBitmapSelected(mBitmap!!)
                uploadBase64ImageToServer(mBitmap!!)

            }else if (requestCode == PICK_IMAGE_REQUEST_GALLERY) {

                val selectedImage = data!!.data
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    var source = ImageDecoder.createSource(this.contentResolver, selectedImage!!)
                    mBitmap = ImageDecoder.decodeBitmap(source)

                    uploadBase64ImageToServer(mBitmap!!)
                } else {

                    var mImageCaptureUri = data.getData();
                    var path = getRealPathFromURI(mImageCaptureUri) //from Gallery

                    if (path == null) {
                        path = mImageCaptureUri!!.getPath(); //from File Manager
                    }

                    if (path != null) {
                        mBitmap = modifyOrientation(BitmapFactory.decodeFile(path),path)
                        onBitmapSelected(mBitmap!!)
                        uploadBase64ImageToServer(mBitmap!!)
                    }


                }

            }


        }
    }

    @Throws(IOException::class)
    open fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String?): Bitmap? {
        val ei = ExifInterface(image_absolute_path)
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> bitmap
        }
    }

    open fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    open fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
        val matrix = Matrix()
        var orientationH = 0.0f
        var orientationV = 0.0f
        if (horizontal) {
            orientationH =  -1.0f
        } else {
            orientationH =  1.0f
        }

        if (vertical) {
            orientationV =  -1.0f
        } else {
            orientationV =  1.0f
        }

        matrix.preScale(orientationH,orientationV)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

fun getRealPathFromURI(contentUri: Uri?): String? {
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor = managedQuery(contentUri, proj, null, null, null) ?: return null
    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    return cursor.getString(column_index)
}
    private fun uploadBase64ImageToServer(bitmap: Bitmap) {
        var base64Images = getImageUrl(bitmap)
        customeProgressDialog!!.show()
//        var requestModel = RequestModel()
//        requestModel.user_id = MyPreferences.getUserID(this)!!
//        requestModel.documents_type = mDocumentType
//        requestModel.image_bytes = base64Images
        WebServiceClient(this).client.create(IApiMethod::class.java).uploadImage(base64Images,MyPreferences.getUserID(this)!!,mDocumentType)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                    Toast.makeText(this@BaseActivity,""+t!!.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    if(!isFinishing) {
                        customeProgressDialog!!.dismiss()
                        var res = response!!.body()
                        if (res != null && res.status) {
                            var photoUrl = res.image_url
                            onBitmapSelected(mBitmap!!)
                            onUploadedImageUrl(photoUrl)
                            Toast.makeText(this@BaseActivity,""+res!!.message, Toast.LENGTH_LONG).show()
                        }else {
                            Toast.makeText(this@BaseActivity,""+res!!.message, Toast.LENGTH_LONG).show()
                        }
                    }

                }

            })

    }

    fun getImageUrl(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


    fun checkAndRequestPermissions(): Boolean {
        val camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writepermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)


        val listPermissionsNeeded = ArrayList<String>()

        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    fun selectImage(mDocumentType:String) {
        if(!checkAndRequestPermissions()){
              return
        }
        this.mDocumentType = mDocumentType
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@BaseActivity)
        builder.setTitle("Add Photo!")
        builder.setItems(options, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, items: Int) {
                if (options[items] == "Take Photo") {
                    openCamera(PICK_IMAGE_REQUEST_CAMERA)
                } else if (options[items] == "Choose from Gallery") {
                    selectGalleryImage()
                } else if (options[items] == "Cancel") {
                    dialog!!.dismiss()
                }
            }

        })
        builder.show()
    }

    private fun openCamera(requestCode: Int) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, requestCode)

    }

    private fun selectGalleryImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //permission already granted
                pickImageFromGallery();
            }
        }
        else{
            //system OS is < Marshmallow
            pickImageFromGallery();
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_GALLERY)
    }



    inner class MyAsyncTask : AsyncTask<Unit, Unit, String>() {
        override fun doInBackground(vararg params: Unit): String {
            return FirebaseInstanceId.getInstance().getToken(getString(R.string.gcm_default_sender_id), "FCM")!!
        }
        override fun onPostExecute(result: String) {
            notificationToken =result
            var userId = MyPreferences.getUserID(applicationContext)!!
            var notid = notificationToken
            if(!TextUtils.isEmpty(notid) && !TextUtils.isEmpty(userId)){
                var request = RequestModel()
                request.user_id =userId
                request.device_id =notid!!
                request.deviceDetails = HardwareInfoManager(this@BaseActivity).collectData()
                WebServiceClient(applicationContext).client.create(IApiMethod::class.java).deviceNotification(request)
                    .enqueue(object : Callback<UsersPostDBResponse?> {
                        override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                        }

                        override fun onResponse(
                            call: Call<UsersPostDBResponse?>?,
                            response: Response<UsersPostDBResponse?>?
                        ) {


                        }

                    })
            }
        }

    }

    abstract fun onBitmapSelected(bitmap:Bitmap)
    abstract fun onUploadedImageUrl(url:String)
}