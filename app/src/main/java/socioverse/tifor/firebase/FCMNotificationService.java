package socioverse.tifor.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import socioverse.tifor.Activity.MainActivity;
import socioverse.tifor.R;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String TAG = "SocioverseService";
    private final String channelId = "socioverse";
    private final String channelname = "Socioverse";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Log.d(TAG, "From: " + remoteMessage.getFrom());

            if (remoteMessage.getData().size() > 0) {
                try {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE);

                    Uri customSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);

                    Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.logo)
                            .setLargeIcon(largeIconBitmap)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle(remoteMessage.getNotification().getTitle())
                            .setContentText(remoteMessage.getNotification().getBody())
                            .setAutoCancel(true)
                            .setSound(customSoundUri)
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build();

                        try {
                            NotificationChannel channel = new NotificationChannel(channelId, channelname, NotificationManager.IMPORTANCE_MAX);
                            channel.setSound(customSoundUri, audioAttributes);
                            channel.setShowBadge(true);
                            long[] vibrationPattern = {100, 50, 100, 50, 100, 50, 100, 50, 100};
                            channel.setVibrationPattern(vibrationPattern);
                            notificationManager.createNotificationChannel(channel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    notificationManager.notify(0, notificationBuilder.build());

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            }
        } catch (Exception e) {

        }
    }
}
