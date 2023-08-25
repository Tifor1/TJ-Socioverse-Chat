package socioverse.tifor.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import socioverse.tifor.databinding.ActivityProfileBinding;
import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;

public class ProfileActivity extends BaseActivity2 {

    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            preferenceManager = new PreferenceManager(getApplicationContext());
            loadUserDetails();
            database = FirebaseFirestore.getInstance();
        } catch (Exception e) {

        }

        binding.profleUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Add Later", Toast.LENGTH_SHORT).show();

            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    logOut();
                } catch (Exception e) {

                }

            }
        });


    }

    private void loadUserDetails() {


        try {

            binding.profileUsername.setText(preferenceManager.getString(Constants.KEY_NAME));
            String email = preferenceManager.getString(Constants.KEY_EMAIL);
            binding.profileEmail.setText(email);
            String encodedString = preferenceManager.getString(Constants.KEY_IMAGE);
            if (encodedString != null) {
                byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.profileImageView.setImageBitmap(bitmap);
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
}