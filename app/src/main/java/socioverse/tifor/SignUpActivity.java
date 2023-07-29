package socioverse.tifor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.FirebaseUtil;
import socioverse.tifor.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    // Update the input type of the EditText based on the CheckBox state
                    if (isChecked) {
                        // Show the password
                        binding.inputPassword.setTransformationMethod(null);
                    } else {
                        // Hide the password
                        binding.inputPassword.setTransformationMethod(new PasswordTransformationMethod());
                    }
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });

        binding.textprivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse("https://doc-hosting.flycricket.io/socioverse/a988998a-db42-4b5d-a0b5-b9af1fe9d7e1/privacy"));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });

        binding.buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isValidSignInDetails()) {
                        loading(true);

                        String name = binding.inputName.getText().toString().trim();
                        String email = binding.inputEmail.getText().toString().trim();
                        String password = binding.inputPassword.getText().toString().trim();
                        String number = binding.inputNumber.getText().toString().trim();

                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                try {
                                    firestore.collection("users").document(FirebaseAuth.getInstance().getUid()).set(new UserModel(number, name, email, password, Timestamp.now(), FirebaseUtil.currentUserId()));
                                    loading(false);
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                } catch (Exception e) {
                                    showToast(e.getMessage());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loading(false);
                                showToast(e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });

        binding.textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            }
        });
    }

    private boolean isValidSignInDetails() {
        String username = binding.inputName.getText().toString().trim();
        String number = binding.inputNumber.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        String phoneNumberPattern = "^[0-9]{10}.$";

        if (username.isEmpty()) {
            binding.inputName.setError("Enter your Name");
            return false;
        } else if (username.length() < 3) {
            binding.inputName.setError("Name length should be at least 3 characters");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.inputEmail.setError("Enter your E-mail");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString().trim()).matches()) {
            binding.inputEmail.setError("Enter Valid E-mail");
            return false;
        } else if (!binding.inputEmail.getText().toString().equals(binding.inputEmail.getText().toString().toLowerCase())) {
            binding.inputEmail.setError("Do not use Capital Letters in the E-mail");
            return false;
        } else if (password.isEmpty()) {
            binding.inputPassword.setError("Enter a Valid Password");
            return false;
        } else if (password.length() < 6) {
            binding.inputPassword.setError("Password length should be at 6 characters");
            return false;
        } else if (number.isEmpty()) {
            binding.inputNumber.setError("Enter a Phone Number");
            return false;
        } else if (!number.matches(phoneNumberPattern)) {
            binding.inputNumber.setError("Invalid Phone Number");
            return false;
        } else if (!Patterns.PHONE.matcher(number).matches()) {
            binding.inputNumber.setError("Enter Valid Number");
            return false;
        } else {
            // If all validations pass, return true to indicate that the sign-in details are valid.
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
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