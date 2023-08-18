package socioverse.tifor.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import socioverse.tifor.databinding.ActivitySignUpBinding;
import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            auth = FirebaseAuth.getInstance();
            preferenceManager = new PreferenceManager(getApplicationContext());
        } catch (Exception e) {

        }

        binding.textprivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse("https://doc-hosting.flycricket.io/socioverse/a988998a-db42-4b5d-a0b5-b9af1fe9d7e1/privacy"));
                    startActivity(browserIntent);
                } catch (Exception e) {

                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

        binding.buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    if (isValidSignInDetails()) {

                        String email = binding.inputEmail.getText().toString(), password = binding.inputPassword.getText().toString();
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    signUpChat();

                                    Toast.makeText(SignUpActivity.this, "Your Data Save In Our Storage", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {

                                    loading(false);
                                    Toast.makeText(SignUpActivity.this, "Password, E-mail Check {Otherwise your account is Created} So LogIn now", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

                    }
                } catch (Exception e) {

                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.layoutImage.setOnClickListener(v -> {

            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                picImage.launch(intent);
            } catch (Exception e) {

                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }


        });

    }

    private Boolean isValidSignInDetails() {

        if (binding.inputName.getText().toString().trim().isEmpty()) {

            binding.inputName.setError("Enter your Name");
            return false;

        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {

            binding.inputEmail.setError("Enter your Email");
            return false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {

            binding.inputEmail.setError("Enter Valid E-mail");
            return false;


        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {

            binding.inputPassword.setError("Enter Password");
            return false;
        } else if (encodedImage == null) {

            showToast("Select Profile Image");

            return false;

        } else {
            loading(true);
            return true;

        }

    }

    private void loading(Boolean isLoading) {

        try {
            if (isLoading) {

                binding.buttonSignUp.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);

            } else {

                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.buttonSignUp.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {

        }

    }

    private void signUpChat() {

        try {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> user = new HashMap<>();
            user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
            user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
            user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
            user.put(Constants.KEY_IMAGE, encodedImage);
            database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {

                loading(false);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

            });
        } catch (Exception e) {

        }


    }

    private String encodeImage(Bitmap bitmap) {

        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    private final ActivityResultLauncher<Intent> picImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        try {
            if (result.getResultCode() == RESULT_OK) {

                if (result.getData() != null) {

                    Uri imageUri = result.getData().getData();
                    try {

                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodedImage = encodeImage(bitmap);

                    } catch (FileNotFoundException e) {

                        e.printStackTrace();

                    }

                }

            }
        } catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }


    });

    private void showToast(String message) {

        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            return;

        }


    }
}
