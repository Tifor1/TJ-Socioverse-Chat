package socioverse.tifor;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import socioverse.tifor.Adapter.ChatRecyclerAdapter;
import socioverse.tifor.Model.ChatMessageModel;
import socioverse.tifor.Model.ChatroomModel;
import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.AndroidUtil;
import socioverse.tifor.Utils.FirebaseUtil;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);

        try {
            FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {
                if (t.isSuccessful()) {
                    Uri uri = t.getResult();
                    AndroidUtil.setProfilePic(this, uri, imageView);
                }
            });
        } catch (Exception e) {

        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onBackPressed();
                } catch (Exception e) {

                }
            }
        });

        try {
            otherUsername.setText(otherUser.getUsername());

            sendMessageBtn.setOnClickListener((v -> {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessageToUser(message);
                } else {
                    messageInput.setError("Enter Your Message");

                }
            }));
        } catch (Exception e) {

        }

        try {
            getOrCreateChatroomModel();
            setupChatRecyclerView();
        } catch (Exception e) {

        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       /* // Get the intent that started this activity
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            // Handle the received text here
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                // Set the shared text in the EditText
                messageInput.setText(sharedText);
            }
        }*/
    }

    void setupChatRecyclerView() {
        try {
            Query query = FirebaseUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);

            FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>().setQuery(query, ChatMessageModel.class).build();

            adapter = new ChatRecyclerAdapter(options, getApplicationContext());
            LinearLayoutManager manager = new LinearLayoutManager(this);
            manager.setReverseLayout(true);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(adapter);
            adapter.startListening();
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    recyclerView.smoothScrollToPosition(0);
                }
            });
        } catch (Exception e) {

        }
    }

    void sendMessageToUser(String message) {
        try {
            chatroomModel.setLastMessageTimestamp(Timestamp.now());
            chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
            chatroomModel.setLastMessage(message);
            FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

            ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now());
            FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        sendNotification(message);
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    //first time chat
                    chatroomModel = new ChatroomModel(chatroomId, Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()), Timestamp.now(), "");
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }

    void sendNotification(String message) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try {
                    JSONObject jsonObject = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", currentUser.getUsername());
                    notificationObj.put("body", message);

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", currentUser.getUserId());

                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("data", dataObj);
                    jsonObject.put("to", otherUser.getFcmToken());

                    callApi(jsonObject);

                } catch (Exception e) {
                    // Handle any exception that may occur while constructing the JSON
                    e.printStackTrace();
                }

            }
        });
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).header("Authorization", "Bearer AAAAiq-kvyQ:APA91bFSxdPEg869MvqzVlDlIFoBzhWdtHt-DQDjuw2UgcsjiaYo1XBU9KYl_AviVvdSHiPn84uwGWJHD66GHA6lPGSMYhsLUh7jnEvcf7UsyPHP5R1-CMEybNU2IqSilYIgEN0xbLPL").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
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
    }
}
