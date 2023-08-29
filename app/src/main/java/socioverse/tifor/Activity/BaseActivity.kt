package socioverse.tifor.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager

open class BaseActivity : AppCompatActivity() {
    private var documentReference: DocumentReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            val preferenceManager = PreferenceManager(applicationContext)
            val database = FirebaseFirestore.getInstance()
            val userId = preferenceManager.getString(Constants.KEY_USER_ID)
            if (userId != null) {
                documentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(userId)
            }
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            documentReference!!.update(Constants.KEY_AVAILABILITY, 0)
        } catch (e: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            documentReference!!.update(Constants.KEY_AVAILABILITY, 1)
        } catch (e: Exception) {
        }
    }
}