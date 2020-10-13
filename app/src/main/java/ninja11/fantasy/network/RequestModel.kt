package ninja11.fantasy.network

import ninja11.fantasy.utils.HardwareInfo
import java.util.ArrayList


public class RequestModel {


    var image_bytes: String=""
    var action_type: String=""
    var team_id: Int=0
    var device_id: String = ""
    var username: String =""
    var password:String = ""
    var deviceDetails: HardwareInfo?=null

    var version_code: Int =0
    var user_id: String =""
    var documents_type: String =""
    var token: String =""
    var withdraw_amount: Int =0
    var match_id: String =""
    var contest_id: String =""

    var email: String =""
    var role_type: Int =0
    var user_type: String =""
    var name: String?=""
    var image_url: String?=""
    var referral_code: String?=""
    var team_name: String?=""
    var mobile_number: String?=""
    var otp: String?=""
    var created_team_id: ArrayList<Int>?=null


    var deposit_amount: String=""
    var transaction_id: String=""
    var payment_mode: String=""
    var payment_status: String=""


    var city: String =""
    var gender: String =""
    var dateOfBirth: String =""
    var pinCode: String =""
    var state: String =""

    var current_password: String=""
    var new_password: String=""

    var discountOnBonusAmount: String=""
    var totalPaidAmount: String=""
    var entryFees: String=""

}
