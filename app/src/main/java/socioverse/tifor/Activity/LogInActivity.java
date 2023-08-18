package socioverse.tifor.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import socioverse.tifor.databinding.ActivityLogInBinding;
import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;


public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;
    private FirebaseAuth auth;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            preferenceManager = new PreferenceManager(getApplicationContext());
            if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {

        }
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            auth = FirebaseAuth.getInstance();
            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } catch (Exception e) {

        }


        binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isValidSignInDetails()) {
                        String email = binding.inputEmail.getText().toString(), password = binding.inputPassword.getText().toString();
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    logInChat();
                                    Toast.makeText(getApplicationContext(), "please wait...", Toast.LENGTH_LONG).show();
                                } else {

                                    loading(false);
                                    Toast.makeText(LogInActivity.this, "Password, E-mail Check {Otherwise your account is not Created} So SignUp now", Toast.LENGTH_SHORT).show();

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

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

        if (binding.inputEmail.getText().toString().trim().isEmpty()) {

            binding.inputEmail.setError("Enter E-mail");
            return false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {

            binding.inputEmail.setError("Enter Valid E-mail");
            return false;

        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {

            binding.inputPassword.setError("Enter Password");
            return false;

        } else {

            loading(true);
            return true;

        }

    }

    private void loading(Boolean isLoading) {

        try {
            if (isLoading) {

                binding.buttonSignIn.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);

            } else {

                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.buttonSignIn.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {

        }

    }

    private void logInChat() {

        try {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString()).whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString()).get().addOnCompleteListener(task -> {

                if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {

                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                    preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {

                    loading(false);

                }
            });
        } catch (Exception e) {

        }
    }
}