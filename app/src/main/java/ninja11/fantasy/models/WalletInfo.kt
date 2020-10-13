package ninja11.fantasy.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


 class WalletInfo :Serializable,Cloneable {


    @SerializedName("wallet_amount")
    @Expose
    var walletAmount: Double = 0.0

    @SerializedName("bonus_amount")
    @Expose
    var bonusAmount: Double = 0.0

    @SerializedName("deposit_amount")
    @Expose
    var depositAmount: Double = 0.0

    @SerializedName("prize_amount")
    @Expose
    var prizeAmount: Double = 0.0

    @SerializedName("is_account_verified")
    @Expose
    var accountStatus: AccountDocumentStatus?=null

    @SerializedName("refferal_friends_count")
    @Expose
    var refferalCounts: Int = 0

    @SerializedName("bank_account_verified")
    @Expose
    var bankAccountVerified: Int = 0


    @SerializedName("transaction")
    @Expose
    var transactionList: ArrayList<TransactionModel> ? =null


    public override fun clone(): WalletInfo {
        return super.clone() as WalletInfo
    }



}
