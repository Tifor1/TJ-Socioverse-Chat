package socioverse.tifor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.AndroidUtil;
import socioverse.tifor.Utils.FirebaseUtil;

public class ProfileFragment extends Fragment {

    private ImageView profilePic;
    private EditText usernameInput;
    private EditText phoneInput;
    private Button updateProfileBtn;
    private ProgressBar progressBar;
    private TextView logoutBtn;

    private UserModel currentUserModel;
    private ActivityResultLauncher<Intent> imagePickLauncher;
    private Uri selectedImageUri;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    try {
                        AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error selecting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfileBtn = view.findViewById(R.id.profle_update_btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);

        try {
            getUserData(requireContext());

            updateProfileBtn.setOnClickListener((v -> {
                try {
                    updateBtnClick();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }));

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUtil.logout();
                                Intent intent = new Intent(requireContext(), LogInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Error logging out: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

            profilePic.setOnClickListener((v) -> {
                try {
                    ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent(intent -> {
                        imagePickLauncher.launch(intent);
                        return null;
                    });
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error selecting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void updateBtnClick() {
        String newUsername = usernameInput.getText().toString();
        if (newUsername.isEmpty() || newUsername.length() < 3) {
            usernameInput.setError("Username length should be at least 3 chars");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);

        try {
            if (selectedImageUri != null) {
                FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri).addOnCompleteListener(task -> {
                    try {
                        updateToFirestore();
                    } catch (Exception e) {
                        setInProgress(false);
                        Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                updateToFirestore();
            }
        } catch (Exception e) {
            setInProgress(false);
            Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateToFirestore() {
        try {
            FirebaseUtil.currentUserDetails().set(currentUserModel).addOnCompleteListener(task -> {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            setInProgress(false);
            Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void getUserData(Context context) {
        setInProgress(true);

        try {
            // Fetch the profile picture URL from Firebase Storage
            FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl().addOnSuccessListener(uri -> {
                // Success: Load and display the profile picture using Glide
                AndroidUtil.setProfilePic(context, uri, profilePic);
            }).addOnFailureListener(exception -> {
                // Failure: Handle the error and optionally show a default profile picture
                AndroidUtil.showToast(context, "Failed to fetch profile picture: " + exception.getMessage());
                // Optionally, you can set a default profile picture here
                // profilePic.setImageResource(R.drawable.default_profile_picture);
            }).addOnCompleteListener(task -> {
                // This part executes after the picture is fetched, regardless of success or failure.
                // You can include additional processing here if needed.
            });

            // Fetch the user data from Firestore
            FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
                setInProgress(false);

                if (task.isSuccessful()) {
                    currentUserModel = task.getResult().toObject(UserModel.class);
                    if (currentUserModel != null) {
                        // Update UI with user data
                        usernameInput.setText(currentUserModel.getUsername());
                        phoneInput.setText(currentUserModel.getPhone());
                    } else {
                        // Handle the case where the user data couldn't be fetched.
                        AndroidUtil.showToast(getContext(), "Failed to fetch user data");
                    }
                } else {
                    // Handle the failure to fetch the user data.
                    AndroidUtil.showToast(getContext(), "Failed to fetch user data: " + task.getException().getMessage());
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            // Handle any other exceptions that might occur within the try block
            AndroidUtil.showToast(getContext(), "An error occurred: " + e.getMessage());
        }
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}