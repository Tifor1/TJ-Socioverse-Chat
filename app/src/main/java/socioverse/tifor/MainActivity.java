package socioverse.tifor;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import socioverse.tifor.Utils.FirebaseUtil;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ChatFragment chatFragment;
    private ProfileFragment profileFragment;
    private String channelId = "socioverse";
    private String channelname = "Socioverse";
    private RemoteMessage remoteMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            showNotification();
        } catch (Exception e) {

        }
        permissionNotification();
        permissionImage();


        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                try {
                    if (item.getItemId() == R.id.menu_chat) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
                    }
                    if (item.getItemId() == R.id.menu_search) {

                        try {
                            Intent intent = new Intent(getApplicationContext(), SearchUserActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {

                        }

                    }
                    if (item.getItemId() == R.id.menu_profile) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
                    }

                } catch (Exception e) {

                }
                return true;
            }
        });

        try {
            bottomNavigationView.setSelectedItemId(R.id.menu_chat);
            getFCMToken();
        } catch (Exception e) {

        }

    }

    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                FirebaseUtil.currentUserDetails().update("fcmToken", token);

            }
        });
    }

    private void permissionNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            int permissionNotif = ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS);

            if (permissionNotif != PackageManager.PERMISSION_GRANTED) {

                String[] NOTIF_PERM = {Manifest.permission.POST_NOTIFICATIONS};
                ActivityCompat.requestPermissions(this, NOTIF_PERM, 10);

            }

        }

    }

    private void permissionImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            int permissionNotif = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES);

            if (permissionNotif != PackageManager.PERMISSION_GRANTED) {

                String[] NOTIF_PERM = {Manifest.permission.READ_MEDIA_IMAGES};
                ActivityCompat.requestPermissions(this, NOTIF_PERM, 11);

            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        showNotification();
        permissionNotification();
        permissionImage();
    }

    private void showNotification() {
        try {

            Uri customSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // Set the custom sound for the notification.
                AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build();

                NotificationChannel channel = new NotificationChannel(channelId, channelname, NotificationManager.IMPORTANCE_HIGH);
                channel.setSound(customSoundUri, audioAttributes);
                channel.setShowBadge(true);
                channel.canShowBadge();
                channel.canBubble();
                long[] vibrationPattern = {100, 50, 100, 50, 100, 50, 100, 50, 100};
                channel.setVibrationPattern(vibrationPattern);

                try {
                    notificationManager.createNotificationChannel(channel);
                } catch (Exception e) {

                }
            }


        } catch (Exception e) {

        }

    }
}