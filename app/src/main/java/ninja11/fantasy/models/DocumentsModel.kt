package ninja11.fantasy.models

import java.io.Serializable


class DocumentsModel :Serializable,Cloneable {

    var user_id: String = ""
    var documentType: String = ""

    var panCardName: String = ""
    var panCardNumber: String = ""
    var pancardDocumentUrl: String = ""

    var aadharCardName: String = ""
    var aadharCardNumber: String = ""
    var aadharCardDocumentUrlFront: String = ""
    var aadharCardDocumentUrlBack: String = ""


    var bankName : String = ""
    var accountHolderName : String = ""
    var accountNumber : String = ""
    var ifscCode  : String = ""
    var accountType : String = ""
    var bankPassbookUrl: String = ""
    var paytmNumber: String = ""


    public override fun clone(): DocumentsModel {
        return super.clone() as DocumentsModel
    }


}
