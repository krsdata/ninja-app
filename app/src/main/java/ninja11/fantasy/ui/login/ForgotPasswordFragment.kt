package ninja11.fantasy.ui.login

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ninja11.fantasy.R
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import ninja11.fantasy.databinding.FragmentForgotPasswordBinding
import ninja11.fantasy.utils.CustomeProgressDialog
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordFragment(val emailAddress: String) : DialogFragment() {

    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var mBinding: FragmentForgotPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.dialog_theme)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_forgot_password, container, false);
        return mBinding!!.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.editEmail.setText(emailAddress)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.imgClose.setOnClickListener(View.OnClickListener {
            dismiss()
        })
        mBinding!!.btnForgotpassword.setOnClickListener(View.OnClickListener {
            var emailAddress = mBinding!!.editEmail.text.toString()
            if(TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)){
                MyUtils.showMessage(activity!! as AppCompatActivity,"Please enter valid email address");
            }else {
                if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
                    MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
                } else {
                    customeProgressDialog.show()
                    var models = RequestModel()
                    models.email = emailAddress

                    WebServiceClient(activity!!).client.create(IApiMethod::class.java)
                        .forgotPassword(models)
                        .enqueue(object : Callback<UsersPostDBResponse?> {
                            override fun onFailure(
                                call: Call<UsersPostDBResponse?>?,
                                t: Throwable?
                            ) {
                                customeProgressDialog.dismiss()
                            }

                            override fun onResponse(
                                call: Call<UsersPostDBResponse?>?,
                                response: Response<UsersPostDBResponse?>?
                            ) {
                                customeProgressDialog.dismiss()
                                var res = response!!.body()
                                if (res != null && res!!.status) {
                                    MyUtils.showMessage(activity!!, res!!.message)
                                    dismiss()
                                } else {
                                    if (res != null) {
                                        MyUtils.showMessage(activity!!, res!!.message)
                                    } else {
                                        MyUtils.showMessage(
                                            activity!!,
                                            "Server not responding, please contact admin"
                                        )
                                    }
                                }

                            }

                        })
                }
            }
        })

    }


    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()!!.setLayout(width, height)
        }
    }


}