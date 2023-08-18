package socioverse.tifor.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import socioverse.tifor.Adapter.RecentConversionAdapter;
import socioverse.tifor.Model.ChatMessage;
import socioverse.tifor.Model.User;
import socioverse.tifor.R;
import socioverse.tifor.databinding.ActivityMainBinding;
import socioverse.tifor.listeners.ConversionListener;
import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;

public class MainActivity extends BaseActivity2 implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter conversionAdapter;
    private FirebaseFirestore database;
    private Boolean isReceiverAvailable = false;
    private User user;
    private String channelname = "Socioverse";
    private String channelId = "socioverse";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            preferenceManager = new PreferenceManager(getApplicationContext());
            loadUserDetails();
            init();
            getToken();
            setListeners();
            listenConversation();
            showNotification();

            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        } catch (Exception e) {

        }

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {

                }

            }
        });

    }

    private void init() {

        try {
            conversions = new ArrayList<>();
            conversionAdapter = new RecentConversionAdapter(conversions, this);
            binding.conversionRecyclerView.setAdapter(conversionAdapter);
            database = FirebaseFirestore.getInstance();
        } catch (Exception e) {

        }
    }

    private void setListeners() {

        try {
            binding.imageSignOut.setOnClickListener(v -> logOut());
            binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        } catch (Exception e) {

        }
    }


    private void loadUserDetails() {


        try {
            binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
            String encodedString = preferenceManager.getString(Constants.KEY_IMAGE);
            if (encodedString != null) {
                byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.imageProfile.setImageBitmap(bitmap);
            }
        } catch (Exception e) {

        }

    }

    private void showToast(String message) {

        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }

    }

    private void listenConversation() {

        try {
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);

        } catch (Exception e) {

        }
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        try {
            if (error != null) {

                return;

            }

            if (value != null) {

                for (DocumentChange documentChange : value.getDocumentChanges()) {

                    if (documentChange.getType() == DocumentChange.Type.ADDED) {

                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = senderId;
                        chatMessage.receiverId = receiverId;
                        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {

                            chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                        } else {

                            chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);

                        }
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        conversions.add(chatMessage);

                    } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                        for (int i = 0; i < conversions.size(); i++) {

                            String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                            String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                            if (conversions.get(i).senderId.equals(senderId) && conversions.get(i).receiverId.equals(receiverId)) {

                                conversions.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                conversions.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                                break;

                            }

                        }

                    }

                }

                Collections.sort(conversions, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
                conversionAdapter.notifyDataSetChanged();
                binding.conversionRecyclerView.smoothScrollToPosition(0);
                binding.conversionRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }

    };

    private void getToken() {

        try {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
        } catch (Exception e) {

        }

    }

    private void updateToken(String token) {

        try {
            preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
            documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnFailureListener(e -> showToast("Unable To Update Token"));
        } catch (Exception e) {

        }
    }

    private void logOut() {
        try {
            showToast("LogOut..."); // Assuming this is a method to show a toast message
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

            HashMap<String, Object> update = new HashMap<>();
            update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());

            documentReference.update(update).addOnSuccessListener(unused -> {
                // Clear preferences
                preferenceManager.clear();
                startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                finish();

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showToast("Faiuld LogOut");
                }
            });
        } catch (Exception e) {
            // Handle exceptions
        }
    }

    @Override
    public void OnConversionClicked(User user) {

        try {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        } catch (Exception e) {

        }

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
                long[] vibrationPattern = {100, 50, 100, 50, 100, 50, 100, 50, 100};
                channel.setVibrationPattern(vibrationPattern);

                notificationManager.createNotificationChannel(channel);
            }


        } catch (Exception e) {

        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
            loadUserDetails();
            getToken();
            showNotification();
        } catch (Exception e) {

        }
    }
}




