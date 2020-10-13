package ninja11.fantasy.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import ninja11.fantasy.utils.HardwareInfoManager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ninja11.fantasy.R
import ninja11.fantasy.SplashScreenActivity
import ninja11.fantasy.UpdateApplicationActivity
import ninja11.fantasy.network.IApiMethod
import ninja11.fantasy.network.RequestModel
import ninja11.fantasy.network.WebServiceClient
import ninja11.fantasy.ui.home.models.UsersPostDBResponse
import org.json.JSONObject
import ninja11.fantasy.utils.MyPreferences
import ninja11.fantasy.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val NOTIFICATION_ID = 1000
    private val FCM_TAG = "MyFirebaseToken"
    private var notificationUtils: NotificationUtils? = null
    val KEY_ACTION = "action"
    val KEY_TITLE = "title"
    val KEY_MESSAGE = "message"
    val KEY_UPDATE_APK = "apk_update_url"
    val KEY_RELEASE_NOTE = "release_note"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(FCM_TAG, token)
        var userId = MyPreferences.getUserID(applicationContext)!!
        var notid = FirebaseInstanceId.getInstance().getToken(getString(R.string.gcm_default_sender_id), "FCM")
        if(!TextUtils.isEmpty(notid) && !TextUtils.isEmpty(userId)){
            var request = RequestModel()
            request.user_id =userId
            request.device_id =notid!!
            request.deviceDetails = HardwareInfoManager(this).collectData()
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)



        remoteMessage?.let { message ->


            // TODO(developer): Handle FCM messages here.
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            MyUtils.logd(FCM_TAG,"From: " + remoteMessage.from)
            if(applicationContext!=null) {
                if (remoteMessage.data.size > 0) {
                    Log.e(
                        FCM_TAG,
                        "Data Payload: " + remoteMessage.data.toString()
                    )
                    try {
                        val gson = Gson()
                        val resultData = gson.toJson(remoteMessage.data)
                        val json = JSONObject(resultData)
                        // handleDataMessage(json)
                        var action = ""
                        var title = ""
                        var message = ""
                        var url = URL("https://cdn.britannica.com/63/211663-050-A674D74C/Jonny-Bairstow-batting-semifinal-match-England-Australia-2019.jpg");
                        var image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                        if (json.has(KEY_ACTION)) {
                            action = json.getString(KEY_ACTION)
                            try {
                                if (json.has(KEY_TITLE)) {
                                    title = json.getString(KEY_TITLE)
                                }
                            } catch (e: java.lang.Exception) {
                            }

                            try {
                                if (json.has(KEY_MESSAGE)) {
                                    message = json.getString(KEY_MESSAGE)
                                }
                            } catch (e: java.lang.Exception) {
                            }

                            when(action){
                                  "notify" ->
                                      notifyUsers(applicationContext, title, message)
                                      //    sendNotification(message,image)
                                   "logout"->
                                        MyPreferences.clear(applicationContext)
                                   "update"->
                                       updateApplicationRequired(applicationContext, title, message,json.getString(KEY_UPDATE_APK),json.getString(KEY_RELEASE_NOTE))

                            }
                        }


                    } catch (e: Exception) {
                        Log.e(
                            FCM_TAG, "Exception: " + e.message
                        )
                    }

                }
            }

        }

    }

    private fun sendNotification(
        messageBody: String,
        image: Bitmap
    ) {
        val intent = getIntentNotify()
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this)
                .setLargeIcon(image)
                .setSmallIcon(R.mipmap.ic_launcher_logo)
                .setContentTitle(messageBody)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(image)
                ) /*Notification with Image*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
    fun notifyUsers(context: Context, title : String, message: String) {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (!powerManager.isInteractive) {
                return
            }
        } else {
            if (!powerManager.isScreenOn) {
                return
            }
        }
        val intent: Intent = getIntentNotify()!!
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val stackBuilder =
            TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationId = 1
            val channelId = "SkoreOnNotify"
            val channelName = "SkoreOnNotify"
            val importance = NotificationManager.IMPORTANCE_HIGH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                notificationManager.createNotificationChannel(mChannel)
            }
            val mBuilder =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle())
            mBuilder.setContentIntent(resultPendingIntent)
            mBuilder.setAutoCancel(true)
            notificationManager.notify(notificationId, mBuilder.build())
        } else {
            val builder =
                NotificationCompat.Builder(context) // Set Ticker Message
                    .setSmallIcon(R.mipmap.ic_launcher_logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setAutoCancel(true) // Set PendingIntent into Notification
                    .setContentIntent(resultPendingIntent)
            // Sets an ID for the notification
            notificationManager.notify(
                NOTIFICATION_ID,
                builder.build()
            )
        }
    }


    private fun getIntentNotify(): Intent? {
        val intent = Intent(
            applicationContext,
            SplashScreenActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }



    fun updateApplicationRequired(
        context: Context,
        title: String,
        messagedd: String,
        apkupdateurl: String,
        releasenote: String
    ) {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (!powerManager.isInteractive) {
                return
            }
        } else {
            if (!powerManager.isScreenOn) {
                return
            }
        }
        val intent: Intent = getIntentUpdateActvity(apkupdateurl,releasenote)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val stackBuilder =
            TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationId = 1
            val channelId = "SkoreOnUpdate"
            val channelName = "SkoreOnUpdate"
            val importance = NotificationManager.IMPORTANCE_HIGH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                notificationManager.createNotificationChannel(mChannel)
            }
            val mBuilder =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_logo)
                    .setContentTitle(title)
                    .setContentText(releasenote)
            mBuilder.setContentIntent(resultPendingIntent)
            mBuilder.setAutoCancel(true)
            notificationManager.notify(notificationId, mBuilder.build())
        } else {
            val builder =
                NotificationCompat.Builder(context) // Set Ticker Message
                    .setSmallIcon(R.mipmap.ic_launcher_logo)
                    .setContentTitle(title)
                    .setContentText(releasenote)
                    .setAutoCancel(true) // Set PendingIntent into Notification
                    .setContentIntent(resultPendingIntent)
            // Sets an ID for the notification
            notificationManager.notify(
                NOTIFICATION_ID,
                builder.build()
            )
        }
    }


    private fun getIntentUpdateActvity(apkupdateurl: String, releasenote: String): Intent? {
        val intent = Intent(
            applicationContext,
            UpdateApplicationActivity::class.java
        )
        intent.putExtra(UpdateApplicationActivity.REQUEST_CODE_APK_UPDATE,apkupdateurl)
        intent.putExtra(UpdateApplicationActivity.REQUEST_RELEASE_NOTE,releasenote)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

}