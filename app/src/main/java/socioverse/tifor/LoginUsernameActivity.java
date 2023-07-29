package socioverse.tifor;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.FirebaseUtil;
import socioverse.tifor.databinding.ActivityLoginUsernameBinding;

public class LoginUsernameActivity extends AppCompatActivity {

    private ActivityLoginUsernameBinding binding;
    private String phoneNumber;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginUsernameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        try {
            phoneNumber = getIntent().getExtras().getString("phone");
            getUsername();
        } catch (NullPointerException e) {
            e.printStackTrace();
            // Handle the case when "phone" extra is not provided in the intent.
            // For example, show an error message or take appropriate action.
            // Replace the comment with your desired error handling logic.
        }

        binding.checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    // Update the input type of the EditText based on the CheckBox state
                    if (isChecked) {
                        // Show the password
                        binding.inputPassword1.setTransformationMethod(null);
                    } else {
                        // Hide the password
                        binding.inputPassword1.setTransformationMethod(new PasswordTransformationMethod());
                    }
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });

        binding.buttonSignUp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setUsername();

            }
        });
    }

    void setUsername() {
        String username = binding.loginUsername.getText().toString().trim();
        if (username.isEmpty() || username.length() < 3) {
            binding.loginUsername.setError("Name length should be at least 3 chars");
            return;
        }
        loading(true);
        if (userModel != null) {
            userModel.setUsername(username);
        } else {
            try {

                String email = binding.inputEmail1.getText().toString().trim();
                String password = binding.inputPassword1.getText().toString().trim();

                userModel = new UserModel(phoneNumber, username, email, password, Timestamp.now(), FirebaseUtil.currentUserId());
            } catch (Exception e) {
                e.printStackTrace();
                // Handle any unexpected exceptions here.
                // Replace the comment with your desired error handling logic.
            }
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loading(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    void getUsername() {
        loading(true);
        try {
            FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    loading(false);
                    if (task.isSuccessful()) {
                        userModel = task.getResult().toObject(UserModel.class);
                        if (userModel != null) {
                            binding.loginUsername.setText(userModel.getUsername());
                        }
                    } else {
                        // Handle the exception when the data retrieval fails.
                        // For example, show an error message or take appropriate action.
                        // Replace the comment with your desired error handling logic.
                        Exception e = task.getException();
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            // Handle any unexpected exceptions here.
            // Replace the comment with your desired error handling logic.
        }
    }

    private Boolean isValidSignInDetails() {

        String userName = binding.loginUsername.getText().toString().trim();
        String password = binding.inputPassword1.getText().toString().trim();

        if (userName.isEmpty() || userName.length() < 3) {
            binding.loginUsername.setError("Name length should be at 3 chars");
            return false;
        } else if (binding.inputEmail1.getText().toString().trim().isEmpty()) {
            binding.inputEmail1.setError("Enter your Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail1.getText().toString()).matches()) {
            binding.inputEmail1.setError("Enter Valid E-mail");
            return false;
        } else if (!binding.inputEmail1.getText().toString().equals(binding.inputEmail1.getText().toString().toLowerCase())) {
            binding.inputEmail1.setError("Do not use Capital Letters in the E-mail");
            return false;
        } else if (!binding.inputEmail1.getText().toString().equals(binding.inputEmail1.getText().toString().toLowerCase())) {
            binding.inputEmail1.setError("No Use Capital Letters");
            return false;
        } else if (password.isEmpty()) {
            binding.inputPassword1.setError("Enter Valid Password");
            return false;
        } else if (password.length() < 6) {
            binding.inputPassword1.setError("Password length should be at 6 characters");
            return false;
        } else {
            return true;
        }

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp1.setVisibility(View.INVISIBLE);
            binding.progressBar1.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar1.setVisibility(View.INVISIBLE);
            binding.buttonSignUp1.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            return;
        }
    }
}