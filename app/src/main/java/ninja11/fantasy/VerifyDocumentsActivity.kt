package ninja11.fantasy

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import ninja11.fantasy.databinding.ActivityVerifyDocumentBinding
import ninja11.fantasy.models.DocumentsModel
import ninja11.fantasy.models.ResponseModel
import ninja11.fantasy.models.UserInfo
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.BaseActivity
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class VerifyDocumentsActivity : BaseActivity() {

    companion object{
        var REQUESTCODE_VERIFY_DOC = 1008

    }

    private var mIsAdharFrontSelected: Boolean=false

    // private var mIsAdharFrontSelected: Boolean = false
    private lateinit var userInfo: UserInfo
    private var mBinding: ActivityVerifyDocumentBinding? = null
    private var isMobileNumberVerified = true
    private var isEmailVeirfied = true

    var pancardDocumentUrl:String= ""
    var adharCardDocumentUrlFront:String= ""
    var adharCardDocumentUrlBack:String= ""
    var bankPassbookUrl:String= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = (application as SportsFightApplication).userInformations
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_verify_document
        )

      /*  val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })


//        mBinding!!.toolbar.setTitle("Verify Documents")
//        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
//        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
//        setSupportActionBar(mBinding!!.toolbar)
//        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
//            finish()
//        })

        customeProgressDialog = CustomeProgressDialog(this)

        initCommunication()
        initDocuments()
        initBankDocuments()
        initPaytm()

    }

    private fun initCommunication() {
        if(isMobileNumberVerified) {
            mBinding!!.linearMobileBorder.setBackgroundResource(R.drawable.btn_selector_verified)
            mBinding!!.verifyMobileMessage.setText("Your mobile number verified")
            mBinding!!.verifyMobileNumber.setText(userInfo.mobileNumber)
            mBinding!!.verifyMobileNumber.setTextColor(resources.getColor(R.color.green))


        }else {
            mBinding!!.linearMobileBorder.setBackgroundResource(R.drawable.btn_selector_not_verified)
            mBinding!!.verifyMobileMessage.setText("Your mobile number not verified")
            mBinding!!.verifyMobileNumber.setText(userInfo.mobileNumber)
            mBinding!!.verifyMobileNumber.setTextColor(resources.getColor(R.color.fab_red))
        }

        if(isEmailVeirfied) {
            mBinding!!.linearEmailBorder.setBackgroundResource(R.drawable.btn_selector_verified)
            mBinding!!.verifyEmailMessage.setText("Your Email Address verified")
            mBinding!!.verifyEmailAddress.setText(userInfo.userEmail)
            mBinding!!.verifyEmailAddress.setTextColor(resources.getColor(R.color.green))
        }else {

            mBinding!!.linearEmailBorder.setBackgroundResource(R.drawable.btn_selector_not_verified)
            mBinding!!.verifyEmailMessage.setText("Your Email Address not verified")
            mBinding!!.verifyEmailAddress.setText(userInfo.userEmail)
            mBinding!!.verifyEmailAddress.setTextColor(resources.getColor(R.color.fab_red))
        }
    }

    private fun initDocuments() {
        showPancardDocuments()
        mBinding!!.txtSelectPancrd.setOnClickListener(View.OnClickListener {
            showPancardDocuments()
            mBinding!!.txtSelectPancrd.setBackgroundResource(R.drawable.rounded_btn_pink)
            mBinding!!.txtSelectPancrd.setTextColor(resources.getColor(R.color.white))

            mBinding!!.txtSelectAdharcard.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.txtSelectAdharcard.setTextColor(resources.getColor(R.color.text))

        })
        mBinding!!.txtSelectAdharcard.setOnClickListener(View.OnClickListener {
            showAdharCardDocuments()
            mBinding!!.txtSelectPancrd.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.txtSelectPancrd.setTextColor(resources.getColor(R.color.text))

            mBinding!!.txtSelectAdharcard.setBackgroundResource(R.drawable.rounded_btn_pink)
            mBinding!!.txtSelectAdharcard.setTextColor(resources.getColor(R.color.white))

        })

        mBinding!!.imgPancard.setOnClickListener(View.OnClickListener {
            selectImage(DOCUMENT_TYPE_PANCARD)
        })

        mBinding!!.imgAdharcardFront.setOnClickListener(View.OnClickListener {
            mIsAdharFrontSelected = true

            selectImage(DOCUMENT_TYPE_ADHARCARD)
        })

        mBinding!!.imgAdharcardBack.setOnClickListener(View.OnClickListener {
            mIsAdharFrontSelected = false
            selectImage(DOCUMENT_TYPE_ADHARCARD)
        })



        mBinding!!.submitDocuments.setOnClickListener(View.OnClickListener {
            //Toast.makeText(this@VerifyDocumentsActivity,"Submitted All Documents like PAN Or Adhar",Toast.LENGTH_LONG).show()
            var models = DocumentsModel()
            models.user_id = userInfo.userId.toString()

            if(mDocumentType.equals(DOCUMENT_TYPE_PANCARD)){
                var pancardName = mBinding!!.editPancardName.text.toString()
                var pancardNumber = mBinding!!.editPancardNumber.text.toString()
                var pancardConfirmNumber = mBinding!!.editPancardConfirmNumber.text.toString()

                if(TextUtils.isEmpty(pancardName)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Name on Pancard")
                    return@OnClickListener
                }else if(TextUtils.isEmpty(pancardNumber)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Pancard Number")
                    return@OnClickListener
                }else if(TextUtils.isEmpty(pancardConfirmNumber)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please Confirm Pancard Number")
                    return@OnClickListener
                }else if(!pancardNumber.equals(pancardConfirmNumber)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Both Pancard number doesnot matched")
                    return@OnClickListener
                }
                else if(TextUtils.isEmpty(pancardDocumentUrl)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please upload clear picture of pancard")
                    return@OnClickListener
                }

                models.panCardName = pancardName
                models.panCardNumber = pancardConfirmNumber
                models.pancardDocumentUrl = pancardDocumentUrl

            }else {
                var adharCardName = mBinding!!.editAdharcardName.text.toString()
                var adharCardNumber = mBinding!!.editAdharNumber.text.toString()
                var adharCardConfirmNumber = mBinding!!.editAdharConfirmNumber.text.toString()

                if(TextUtils.isEmpty(adharCardName)){

                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Name on Adharcard")
                    return@OnClickListener
                }else if(TextUtils.isEmpty(adharCardNumber)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Aadharcard Number")
                    return@OnClickListener
                }else if(TextUtils.isEmpty(adharCardConfirmNumber)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please Confirm Aadharcard Number")
                    return@OnClickListener
                }else if(!adharCardNumber.equals(adharCardConfirmNumber)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Both Pancard number doesnot matched")
                    return@OnClickListener
                }
                else if(TextUtils.isEmpty(adharCardDocumentUrlFront)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please upload Front Side of Adharcard")
                    return@OnClickListener
                }else if(TextUtils.isEmpty(adharCardDocumentUrlBack)){
                    MyUtils.showToast(this@VerifyDocumentsActivity,"Please upload Back Side of Adharcard")
                    return@OnClickListener
                }



                models.aadharCardName = adharCardName
                models.aadharCardNumber = adharCardNumber
                models.aadharCardDocumentUrlFront = adharCardDocumentUrlFront
                models.aadharCardDocumentUrlBack = adharCardDocumentUrlBack
            }
            submitDocuments(models,mDocumentType)
        })

    }

    private fun submitDocuments(models: DocumentsModel, documentType: String ) {
        customeProgressDialog.show()
        models.documentType = documentType
        if(MyUtils.isConnectedWithInternet(this@VerifyDocumentsActivity)){
            WebServiceClient(this@VerifyDocumentsActivity).client.create(IApiMethod::class.java).saveBankDetails(models)
                .enqueue(object : Callback<ResponseModel?> {
                    override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                        MyUtils.showToast(this@VerifyDocumentsActivity,t!!.localizedMessage)
                        hidePanAdharDocuments()
                    }

                    override fun onResponse(
                        call: Call<ResponseModel?>?,
                        response: Response<ResponseModel?>?
                    ) {
                        customeProgressDialog.dismiss()
                        var res = response!!.body()
                        if(res!=null &&  res.status) {
                            if( models.documentType == DOCUMENT_TYPE_PANCARD || models.documentType == DOCUMENT_TYPE_ADHARCARD) {
                                hidePanAdharDocuments()
                            }else if( models.documentType == DOCUMENT_TYPE_BANK_PASSBOOK) {
                               hideBankDocuments()
                            }else if( models.documentType == DOCUMENT_TYPE_PAYTM) {
                                Toast.makeText(this@VerifyDocumentsActivity,res.message,Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }else {
                            MyUtils.showToast(this@VerifyDocumentsActivity,"Unable to upload documents, Please retry again");
                        }

                    }

                })
        }else {
            MyUtils.showToast(this@VerifyDocumentsActivity,"Please check your internet connections")
        }
    }

    private fun hidePanAdharDocuments() {
        mDocumentType = DOCUMENT_TYPE_BANK_PASSBOOK
        mBinding!!.linearDocumentsDetails.visibility  = View.GONE
        mBinding!!.linearBankaccountDetails.visibility = View.VISIBLE
        mBinding!!.linearPaytmDetails.visibility = View.GONE

//        mBinding!!.linearBankaccountDetails.setVisibility(View.VISIBLE);
//        mBinding!!.linearBankaccountDetails.setAlpha(0.0f);
//        mBinding!!.linearBankaccountDetails.animate()
//            .translationY(mBinding!!.linearBankaccountDetails.getHeight().toFloat())
//            .alpha(1.0f)
//            .setListener(null)
    }

    private fun hideBankDocuments() {
        mBinding!!.linearDocumentsDetails.visibility  = View.GONE
        mBinding!!.linearBankaccountDetails.visibility = View.GONE
        mBinding!!.linearPaytmDetails.visibility = View.VISIBLE

//        mBinding!!.linearPaytmDetails.setVisibility(View.VISIBLE);
//        mBinding!!.linearPaytmDetails.setAlpha(0.0f);
//        mBinding!!.linearPaytmDetails.animate()
//            .translationY(mBinding!!.linearPaytmDetails.getHeight().toFloat())
//            .alpha(1.0f)
//            .setListener(null)
    }


    private fun initBankDocuments() {
        mBinding!!.linearBankaccountDetails.visibility = View.GONE
        mBinding!!.imgBankPassbook.setOnClickListener(View.OnClickListener {

            selectImage(DOCUMENT_TYPE_BANK_PASSBOOK)
        })

        mBinding!!.submitBankDocuments.setOnClickListener(View.OnClickListener {
            Toast.makeText(this@VerifyDocumentsActivity,"Submitted All Bank Documents",Toast.LENGTH_LONG).show()
            var models = DocumentsModel()
            models.user_id = userInfo.userId.toString()
            var bankName = mBinding!!.editBankName.text.toString()
            var accountHolderName = mBinding!!.editAccountHolderName.text.toString()
            var accountNumber = mBinding!!.editAccountNumber.text.toString()
            var ifscCode = mBinding!!.editAccountIfscCode.text.toString()
            var accountType = mBinding!!.editAccoutType.text.toString()

            if(TextUtils.isEmpty(bankName)){

                MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Your Bank Name")
                return@OnClickListener
            }else if(TextUtils.isEmpty(accountHolderName)){
                MyUtils.showToast(this@VerifyDocumentsActivity,"Please your name on Bank card")
                return@OnClickListener
            }else if(TextUtils.isEmpty(accountNumber)){
                MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter bank account number")
                return@OnClickListener
            }
            else if(TextUtils.isEmpty(ifscCode)){
                MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter IFSC Code")
                return@OnClickListener
            }else  if(TextUtils.isEmpty(accountType)){
                 MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Account Type")
                 return@OnClickListener
             }else  if(TextUtils.isEmpty(bankPassbookUrl)){
                 MyUtils.showToast(this@VerifyDocumentsActivity,"Please upload passbook or cheque clear image")
                 return@OnClickListener
             }

            models.bankName = bankName
            models.accountHolderName = accountHolderName
            models.accountNumber = accountNumber
            models.ifscCode = ifscCode
            models.accountType = accountType
            models.bankPassbookUrl = bankPassbookUrl

            submitDocuments(models,mDocumentType)

        })
    }


    private fun initPaytm() {
        mBinding!!.linearPaytmDetails.visibility = View.GONE

        mBinding!!.btnSubmitVerification.setOnClickListener(View.OnClickListener {
            var models = DocumentsModel()
            models.user_id = userInfo.userId.toString()
            var paytmNumber = mBinding!!.editPaytmNumber.text.toString()
            models.paytmNumber = paytmNumber

            if(TextUtils.isEmpty(paytmNumber)){
                MyUtils.showToast(this@VerifyDocumentsActivity,"Please enter Your Paytm Number")
                return@OnClickListener
            }

            models.documentType = DOCUMENT_TYPE_PAYTM


            if(MyUtils.isConnectedWithInternet(this@VerifyDocumentsActivity)){
               submitDocuments(models, DOCUMENT_TYPE_PAYTM)
            }else {
                MyUtils.showToast(this@VerifyDocumentsActivity,"Please check your internet connections");
            }
        })

    }

    private fun showPancardDocuments() {
        mDocumentType = DOCUMENT_TYPE_PANCARD
        mBinding!!.linearUploadPancard.visibility = View.VISIBLE
        mBinding!!.linearUploadAdharcard.visibility = View.GONE
    }

    private fun showAdharCardDocuments() {
        mDocumentType = DOCUMENT_TYPE_ADHARCARD
        mBinding!!.linearUploadPancard.visibility = View.GONE
        mBinding!!.linearUploadAdharcard.visibility = View.VISIBLE
    }






    override fun onBitmapSelected(bitmap: Bitmap) {
        if(mDocumentType.equals(DOCUMENT_TYPE_PANCARD)){
            mBinding!!.imgPancard.setImageBitmap(bitmap)
        }else if(mDocumentType.equals(DOCUMENT_TYPE_ADHARCARD)){
            if(mIsAdharFrontSelected){
                mBinding!!.imgAdharcardFront.setImageBitmap(bitmap)
            }else {
                mBinding!!.imgAdharcardBack.setImageBitmap(bitmap)
            }
        }else if(mDocumentType.equals(DOCUMENT_TYPE_BANK_PASSBOOK)){
            mBinding!!.imgBankPassbook.setImageBitmap(bitmap)
        }
    }

    override fun onUploadedImageUrl(url: String) {
        if(mDocumentType.equals(DOCUMENT_TYPE_PANCARD)){
            pancardDocumentUrl = url
        }else if(mDocumentType.equals(DOCUMENT_TYPE_ADHARCARD)){
            if(mIsAdharFrontSelected){
                adharCardDocumentUrlFront = url
            }else {
                adharCardDocumentUrlBack = url
            }
        }else if(mDocumentType.equals(DOCUMENT_TYPE_BANK_PASSBOOK)){
            bankPassbookUrl = url
        }
    }


}
