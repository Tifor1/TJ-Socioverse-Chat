package socioverse.tifor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

import socioverse.tifor.Utils.FirebaseUtil;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageButton searchButton;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        permissionNotification();
        permissionImage();


        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.main_search_btn);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    startActivity(new Intent(MainActivity.this, SearchUserActivity.class));
                } catch (Exception e) {

                }

            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                try {
                    if (item.getItemId() == R.id.menu_chat) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
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
        permissionNotification();
        permissionImage();
    }
}