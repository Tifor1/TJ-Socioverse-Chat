package socioverse.tifor.Activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import socioverse.tifor.utlities.Constants;
import socioverse.tifor.utlities.PreferenceManager;


public class BaseActivity2 extends AppCompatActivity {


    private DocumentReference documentReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            String userId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (userId != null) {
                documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(userId);
            }
        } catch (Exception e) {

        }

    }

    @Override
    protected void onStop() {
        try {
            documentReference.update(Constants.KEY_AVAILABILITY, 0);
        } catch (Exception e) {

        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            documentReference.update(Constants.KEY_AVAILABILITY, 1);
        } catch (Exception e) {

        }


    }
}
