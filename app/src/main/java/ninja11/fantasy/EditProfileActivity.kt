package ninja11.fantasy

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import ninja11.fantasy.models.ResponseModel
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import ninja11.fantasy.databinding.ActivityEditProfileBinding
import java.util.*


class EditProfileActivity : BaseActivity() {


    private lateinit var userInfo: UserInfo
    private var mBinding: ActivityEditProfileBinding? = null
    private var photoUrl: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = (application as SportsFightApplication).userInformations
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_edit_profile
        )

       /* val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/


        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })


//        mBinding!!.toolbar.setTitle("Update Profile")
//        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//       mBinding!!.imageBack.setOnClickListener(View.OnClickListener { finish() })

        Glide.with(this)
            .load(MyPreferences.getProfilePicture(this))
            .placeholder(R.drawable.dummy)
            .into(mBinding!!.profileImage)

        updateUserOtherInfo()
        mBinding!!.ivEdit.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                if(!TextUtils.isEmpty(photoUrl)){
                    val intent = Intent(this@EditProfileActivity, FullScreenImageViewActivity::class.java)
                    intent.putExtra(FullScreenImageViewActivity.KEY_IMAGE_URL,photoUrl)
                    startActivity(intent)
                }else
                    if (checkAndRequestPermissions()) {
                        selectImage(BaseActivity.DOCUMENTS_TYPE_PROFILES)
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Permission required ", Toast.LENGTH_LONG)
                            .show()
                    }
            }

        })

        mBinding!!.dateOfBirth.setOnClickListener(View.OnClickListener {
            val c = Calendar.getInstance()
            var mYear = c[Calendar.YEAR]
            var mMonth = c[Calendar.MONTH]
            var mDay = c[Calendar.DAY_OF_MONTH]


            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    mBinding!!.dateOfBirth.setText(
                        dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                    )
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        })

        mBinding!!.btnUpdateProfile.setOnClickListener(View.OnClickListener {

            updateProfile()
        })

        initProfile()
        getProfile()

    }

    private fun updateUserOtherInfo() {
        if(!TextUtils.isEmpty(userInfo.teamName)) {
            mBinding!!.editTeamName.setText(userInfo.teamName)
        }

         if(!TextUtils.isEmpty(userInfo.dateOfBirth)) {
            mBinding!!.dateOfBirth.setText(userInfo.dateOfBirth)
        }

        if(!TextUtils.isEmpty(userInfo.city)) {
            mBinding!!.editCity.setText(userInfo.city)
        }
        if(!TextUtils.isEmpty(userInfo.pinCode)) {
            mBinding!!.editPicode.setText(userInfo.pinCode)
            mBinding!!.spinnerStates.setPrompt(userInfo.state)

        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        mBinding!!.profileImage.setImageBitmap(bitmap)
    }

    override fun onUploadedImageUrl(url: String) {
        this.photoUrl = url
        MyPreferences.setProfilePicture(this@EditProfileActivity,photoUrl)
    }

    private fun initProfile() {
        photoUrl = userInfo.profileImage
        MyPreferences.setProfilePicture(this,photoUrl)
        mBinding!!.editTeamName.setText(userInfo.teamName)
        mBinding!!.updateProfileName.setText(userInfo.fullName)
        mBinding!!.updateEmail.setText(userInfo.userEmail)
        mBinding!!.updateEditMobile.setText(userInfo.mobileNumber)
    }

    private fun updateProfile() {
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        var editName = mBinding!!.updateProfileName.text.toString()
        var mobileNumber = mBinding!!.updateEditMobile.text.toString()
        var emailAddress = mBinding!!.updateEmail.text.toString()
        var cityName = mBinding!!.editCity.text.toString()
        var gender ="male"
        if( !mBinding!!.genderMale.isChecked){
            gender ="female"
        }
        var dateOfBirth = mBinding!!.dateOfBirth.text.toString()
        var state = mBinding!!.spinnerStates.selectedItem.toString()

        if(TextUtils.isEmpty(editName)){
            MyUtils.showToast(this@EditProfileActivity,"Please enter your real name")
            return
        }else if(TextUtils.isEmpty(mobileNumber)){
            MyUtils.showToast(this@EditProfileActivity,"Please enter valid mobile number")
            return
        }else if(mobileNumber.length<10){
            MyUtils.showToast(this@EditProfileActivity,"Please enter valid mobile number")
            return
        }else if(TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)){
            MyUtils.showToast(this@EditProfileActivity,"Please enter valid email address")
            return
        }else if(TextUtils.isEmpty(cityName)){
            MyUtils.showToast(this@EditProfileActivity,"Please enter city Name")
            return
        }else if(TextUtils.isEmpty(dateOfBirth)){
            MyUtils.showToast(this@EditProfileActivity,"Please enter your Date of Birth")
            return
        }

        mBinding!!.loaderLogin.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.image_url = photoUrl
        models.team_name = mBinding!!.editTeamName.text.toString()
        models.name = editName
        models.email = emailAddress
        models.mobile_number = mobileNumber
        models.city = cityName
        models.gender = gender
        models.dateOfBirth = dateOfBirth
       // models.pinCode = pinCode
        models.state = state

        WebServiceClient(this!!).client.create(IApiMethod::class.java).updateProfile(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {



                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.loaderLogin.visibility  =View.GONE
                    var res = response!!.body()
                    if(res!=null && res.status) {
                        userInfo = (application as SportsFightApplication).userInformations
                        userInfo.teamName = editName
                        userInfo.fullName = editName
                        userInfo.city = cityName
                        userInfo.gender = gender
                        userInfo.dateOfBirth = dateOfBirth
                       // userInfo.pinCode = pinCode
                        userInfo.state = state

                        Toast.makeText(this@EditProfileActivity,"Profile updated successfully",Toast.LENGTH_LONG).show()
                        finish()
                    }

                }

            })

    }

    private fun getProfile() {
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        mBinding!!.loaderLogin.visibility = View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!


        WebServiceClient(this!!).client.create(IApiMethod::class.java).getProfile(models)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                    mBinding!!.loaderLogin.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    mBinding!!.loaderLogin.visibility = View.GONE
                    var res = response!!.body()
                    if(res!=null && res.status) {

                        var infoModels = res.infomodel
                        if (infoModels != null) {
                            if (!TextUtils.isEmpty(res.infomodel!!.profileImage)) {
                                MyPreferences.setProfilePicture(this@EditProfileActivity, res.infomodel!!.profileImage)
                            }
                            (application as SportsFightApplication).saveUserInformations(infoModels)
                            userInfo = (application as SportsFightApplication).userInformations
                            initProfile()
                            updateUserOtherInfo()

                        } else {
                            MyUtils.showToast(this@EditProfileActivity, "Something went wrong, please contact admin")
                        }

                    }

                }

            })

    }



}
