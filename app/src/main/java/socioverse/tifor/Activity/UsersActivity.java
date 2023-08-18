package socioverse.tifor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import socioverse.tifor.Adapter.UserAdapter;
import socioverse.tifor.Model.User;
import socioverse.tifor.databinding.ActivityUsersBinding;
import socioverse.tifor.listeners.UserListener;
import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private Boolean isReceiverAvailable = false;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            database = FirebaseFirestore.getInstance();
            preferenceManager = new PreferenceManager(getApplicationContext());
            // Set up the RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            binding.userRecyclerView.setLayoutManager(layoutManager);
        } catch (Exception e) {

        }


        try {
            setListener();
            getUsers();
        } catch (Exception e) {
            // Handle any exceptions here
        }
    }

    private void setListener() {
        try {
            binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
        } catch (Exception e) {

        }
    }

    private void getUsers() {
        try {
            loading(true);
            database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
                loading(false);
                String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                            continue;
                        }
                        try {
                            User user = new User();
                            user.username = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.fcmToken = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.userId = queryDocumentSnapshot.getId();
                            users.add(user);
                        } catch (Exception e) {

                        }
                    }

                    if (users.size() > 0) {
                        try {
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.userRecyclerView.setAdapter(userAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Toast.makeText(UsersActivity.this, "Error displaying users.", Toast.LENGTH_SHORT).show(); // Use "UsersActivity.this"
                        }
                    } else {
                        showErrorMessage();
                    }

                } else {
                    showErrorMessage();
                }
            });
        } catch (Exception e) {

        }
    }

    private void showErrorMessage() {
        try {
            binding.textErrorMessage.setText(String.format("%s", "{ TF Chat } No Users Available"));
            binding.textErrorMessage.setVisibility(View.VISIBLE);
        } catch (Exception e) {

        }
    }

    private void loading(Boolean isLoading) {
        try {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onUserClicked(User user) {

        try {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
            finish();
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}