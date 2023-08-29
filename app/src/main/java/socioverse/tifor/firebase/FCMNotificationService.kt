package socioverse.tifor.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import socioverse.tifor.Activity.MainActivity
import socioverse.tifor.R

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMNotificationService : FirebaseMessagingService() {
    private val channelId = "socioverse"
    private val channelname = "Socioverse"

    @SuppressLint("WrongConstant")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            Log.d(TAG, "From: " + remoteMessage.from)
            if (remoteMessage.data.size > 0) {
                try {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE
                    )
                    val customSoundUri =
                        Uri.parse("android.resource://" + packageName + "/" + R.raw.notification)
                    val largeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
                    val notificationBuilder =
                        NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.logo)
                            .setLargeIcon(largeIconBitmap)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle(remoteMessage.notification!!.title)
                            .setContentText(remoteMessage.notification!!.body).setAutoCancel(true)
                            .setSound(customSoundUri).setContentIntent(pendingIntent)
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                        try {
                            val channel = NotificationChannel(
                                channelId, channelname, NotificationManager.IMPORTANCE_MAX
                            )
                            channel.setSound(customSoundUri, audioAttributes)
                            channel.setShowBadge(true)
                            val vibrationPattern =
                                longArrayOf(100, 50, 100, 50, 100, 50, 100, 50, 100)
                            channel.vibrationPattern = vibrationPattern
                            notificationManager.createNotificationChannel(channel)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    notificationManager.notify(0, notificationBuilder.build())
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
            if (remoteMessage.notification != null) {
                Log.d(
                    TAG, "Message Notification Body: " + remoteMessage.notification!!.body
                )
            }
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val TAG = "SocioverseService"
    }
}