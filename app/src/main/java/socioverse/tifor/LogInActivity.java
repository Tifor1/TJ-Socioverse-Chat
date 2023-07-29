package socioverse.tifor;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.AndroidUtil;
import socioverse.tifor.Utils.FirebaseUtil;
import socioverse.tifor.databinding.ActivityLogInBinding;


public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        try {
            if (FirebaseUtil.isLoggedIn()) {
                startActivity(new Intent(LogInActivity.this, MainActivity.class));
                finish();
            }

            if (getIntent().getExtras() != null) {
                //from notification
                String userId = getIntent().getExtras().getString("userId");
                FirebaseUtil.allUserCollectionReference().document(userId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            UserModel model = task.getResult().toObject(UserModel.class);

                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isValidSignInDetails()) {
                        loading(true);

                        String email = binding.inputEmail.getText().toString().trim();
                        String password = binding.inputPassword.getText().toString().trim();

                        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                loading(false);

                                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loading(false);
                                Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.textCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Boolean isValidSignInDetails() {

        String password = binding.inputPassword.getText().toString().trim();

        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.inputEmail.setError("Enter E-mail");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            binding.inputEmail.setError("Enter Valid E-mail");
            return false;
        } else if (!binding.inputEmail.getText().toString().equals(binding.inputEmail.getText().toString().toLowerCase())) {
            binding.inputEmail.setError("No Use Capital Letters");
            return false;
        } else if (password.isEmpty()) {
            binding.inputPassword.setError("Enter Password");
            return false;
        } else if (password.length() < 6) {
            binding.inputPassword.setError("Password length should be at least 6 characters");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (FirebaseUtil.isLoggedIn()) {
                startActivity(new Intent(LogInActivity.this, MainActivity.class));
                finish();
            } else {
                // Do nothing if not logged in
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}