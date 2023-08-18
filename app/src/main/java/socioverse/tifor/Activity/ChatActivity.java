package socioverse.tifor.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import socioverse.tifor.Adapter.ChatAdapter;
import socioverse.tifor.Model.ChatMessage;
import socioverse.tifor.Model.User;
import socioverse.tifor.databinding.ActivityChatBinding;
import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            setListeners();
            loadReceiverData();
            init();
            listenMessage();
        } catch (Exception e) {

        }
    }

    private void init() {

        try {
            preferenceManager = new PreferenceManager(getApplicationContext());
            chatMessages = new ArrayList<>();
            chatAdapter = new ChatAdapter(chatMessages, getBitmapFromcodedString(receiverUser.image), preferenceManager.getString(Constants.KEY_USER_ID));
            binding.chatRecyclerView.setAdapter(chatAdapter);
            database = FirebaseFirestore.getInstance();
        } catch (Exception e) {

        }
    }

    private void sendMessage() {

        try {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.userId);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if (conversionId != null) {

                updateConversion(binding.inputMessage.getText().toString());

            } else {

                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.userId);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.username);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);

            }

            try {
                if (!isReceiverAvailable) {

                    sendNotification(binding.inputMessage.getText().toString().trim());


                }
            } catch (Exception e) {

            }
            binding.inputMessage.setText(null);
        } catch (Exception e) {

        }
    }

    private void showToast(String message) {

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void sendNotification(String messageBody) {

        try {
            User user = new User();

            String senderName = preferenceManager.getString(Constants.KEY_NAME);

            JSONObject jsonObject = new JSONObject();

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", senderName);
            notificationObj.put("body", messageBody);

            JSONObject dataObj = new JSONObject();
            dataObj.put("userId", Constants.KEY_USER_ID);

            jsonObject.put("notification", notificationObj);
            jsonObject.put("data", dataObj);
            jsonObject.put("to", receiverUser.fcmToken);

            callApi(jsonObject);

        } catch (Exception e) {
            // Handle any exception that may occur while constructing the JSON
            e.printStackTrace();
        }

    }

    void callApi(JSONObject jsonObject) {
        try {
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            String url = "https://fcm.googleapis.com/fcm/send";
            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder().url(url).post(body).header("Authorization", "Bearer AAAAiq-kvyQ:APA91bFSxdPEg869MvqzVlDlIFoBzhWdtHt-DQDjuw2UgcsjiaYo1XBU9KYl_AviVvdSHiPn84uwGWJHD66GHA6lPGSMYhsLUh7jnEvcf7UsyPHP5R1-CMEybNU2IqSilYIgEN0xbLPL").build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    // Handle the failure of the API call
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // Handle the response of the API call, if needed
                    if (response.isSuccessful()) {
                        // The API call was successful
                    } else {

                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private void listenAvailabilityOfReceiver() {

        try {
            database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.userId).addSnapshotListener(ChatActivity.this, (value, error) -> {

                if (error != null) {

                    return;

                }

                if (value != null) {

                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {

                        int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();

                        isReceiverAvailable = availability == 1;

                    }
                    receiverUser.fcmToken = value.getString(Constants.KEY_FCM_TOKEN);
                    if (receiverUser.image == null) {
                        receiverUser.image = value.getString(Constants.KEY_IMAGE);
                        chatAdapter.setReceiverProfileImage(getBitmapFromcodedString(receiverUser.image));
                        chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                    }

                }

                if (isReceiverAvailable) {

                    binding.textAvailability.setVisibility(View.VISIBLE);

                } else {

                    binding.textAvailability.setVisibility(View.GONE);

                }


            });
        } catch (Exception e) {

        }

    }

    private void listenMessage() {

        try {
            database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.userId).addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.userId).whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);

        } catch (Exception e) {

        }
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        try {
            if (error != null) {

                return;

            }

            if (value != null) {

                int count = chatMessages.size();
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        chatMessages.add(chatMessage);

                    }

                }

                Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
                if (count == 0) {

                    chatAdapter.notifyDataSetChanged();

                } else {

                    chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                }
                binding.chatRecyclerView.setVisibility(View.VISIBLE);

            }
            binding.progressBar.setVisibility(View.GONE);
            if (conversionId == null) {

                checkForConversion();

            }
        } catch (Exception e) {

        }

    };

    private Bitmap getBitmapFromcodedString(String encodedImage) {


            if (encodedImage != null) {
                byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } else {

                return null;

            }

    }

    private void loadReceiverData() {

        try {
            receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
            binding.textName.setText(receiverUser.username);
        } catch (Exception e) {

        }

    }

    private void setListeners() {
        try {
            binding.imageBack.setOnClickListener(v -> onBackPressed());
            binding.layoutSend.setOnClickListener(v -> sendMessage());
        } catch (Exception e) {

        }
    }

    private String getReadableDateTime(Date date) {

            return new SimpleDateFormat("dd,MMM,yyyy - hh:mm a", Locale.getDefault()).format(date);

    }

    private void addConversion(HashMap<String, Object> conversion) {

        try {
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(conversion).addOnSuccessListener(documentReference -> conversionId = documentReference.getId());

        } catch (Exception e) {

        }
    }

    private void updateConversion(String message) {

        try {
            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
            documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
        } catch (Exception e) {

        }

    }

    private void checkForConversion() {

        try {
            if (chatMessages.size() != 0) {

                checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.userId);
                checkForConversionRemotely(receiverUser.userId, preferenceManager.getString(Constants.KEY_USER_ID));

            }
        } catch (Exception e) {

        }

    }

    private void checkForConversionRemotely(String senderId, String receiverId) {

        try {
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_SENDER_ID, senderId).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId).get().addOnCompleteListener(conversionOnCompleteListener);

        } catch (Exception e) {

        }
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {

        try {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {

                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                conversionId = documentSnapshot.getId();

            }
        } catch (Exception e) {

        }

    };

    @Override
    protected void onResume() {
        try {
            listenAvailabilityOfReceiver();
        } catch (Exception e) {

        }
        super.onResume();
    }
}