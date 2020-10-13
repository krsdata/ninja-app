package ninja11.fantasy

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
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
import ninja11.fantasy.databinding.ActivityViewDocumentBinding


class DocumentsListActivity : BaseActivity() {

    private lateinit var userInfo: UserInfo
    private var mBinding: ActivityViewDocumentBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(this)
        userInfo = (application as SportsFightApplication).userInformations
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_view_document
        )

    /*    val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        mBinding!!.linearPaytm.visibility = View.GONE
        mBinding!!.linearDocuments.visibility = View.GONE
        mBinding!!.linearBankaccounts.visibility = View.GONE

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })


        //mBinding!!.toolbar.setTitle("My Documents")
//        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(mBinding!!.toolbar)
//        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })

        mBinding!!.contactUs.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@DocumentsListActivity, SupportActivity::class.java)
            startActivity(intent)
        })
        getDocumentsList()

    }


    fun getDocumentsList() {
        if(!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this,"No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.token = MyPreferences.getToken(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).getDocumentsList(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    var res = response!!.body()
                    if(res!=null) {
                        if(res.responseObject==null){
                           return
                        }
                        var documentsList = res!!.responseObject!!.documentsList
                        var paytmNoList = res!!.responseObject!!.paytmNoList
                        var bankAccountsList = res!!.responseObject!!.bankAccountsList
                        if(paytmNoList!=null && paytmNoList.size>0) {
                            mBinding!!.linearPaytm.visibility = View.VISIBLE
                            var docModel = paytmNoList.get(0)
                            var message = docModel.message
                            if(!TextUtils.isEmpty(message) && message.equals("Approved")){
                                mBinding!!.imgCheck.setImageResource(R.drawable.checked)
                            }else {
                                mBinding!!.imgCheck.setImageResource(R.drawable.unchecked_doc)
                            }
                            var dataModel = docModel.documentDataModel
                            mBinding!!.paytmNumber.setText(dataModel!!.docNumber)
                        }else {
                            mBinding!!.linearPaytm.visibility = View.GONE
                        }

                        if(documentsList!=null) {
                            mBinding!!.linearDocuments.visibility = View.VISIBLE
                            var docModel = documentsList.get(0)
                            var message = docModel.message
                            var dataModel = docModel.documentDataModel

                            if(!TextUtils.isEmpty(message) && message.equals("Approved")){
                                mBinding!!.documentImgCheck.setImageResource(R.drawable.checked)
                            }else {
                                mBinding!!.documentImgCheck.setImageResource(R.drawable.unchecked_doc)
                            }

                            mBinding!!.documentType.setText(dataModel!!.docType)
                            mBinding!!.documentNumbers.setText(dataModel!!.docNumber)

                            Glide.with(this@DocumentsListActivity)
                                .load(dataModel!!.docUrlFront)
                                .placeholder(R.drawable.ic_photo_camera_black_24dp)
                                .into(mBinding!!.imgDocType)

                        }else {
                            mBinding!!.linearDocuments.visibility = View.GONE
                        }

                        if(bankAccountsList!=null) {
                            mBinding!!.linearBankaccounts.visibility = View.VISIBLE
                            var docModel = bankAccountsList.get(0)
                            var message = docModel.message
                            var dataModel = docModel.documentDataModel

                            if(!TextUtils.isEmpty(message) && message.equals("Approved")){
                                mBinding!!.bankImgCheck.setImageResource(R.drawable.checked)
                            }else {
                                mBinding!!.bankImgCheck.setImageResource(R.drawable.unchecked_doc)
                            }
                            mBinding!!.bankName.setText(dataModel!!.bankName)
                            mBinding!!.bankAccountNumber.setText(dataModel!!.accountNumber)

                            Glide.with(this@DocumentsListActivity)
                                .load(dataModel!!.bankPassbookUrl)
                                .placeholder(R.drawable.ic_photo_camera_black_24dp)
                                .into(mBinding!!.chequeBookImage)

                        }
                    }else {
                        mBinding!!.linearBankaccounts.visibility = View.GONE
                    }

                }

            })

    }


    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {
    }

}
