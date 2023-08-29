package socioverse.tifor.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import socioverse.tifor.databinding.ActivityProfileBinding
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager

class ProfileActivity : BaseActivity2() {
    private var binding: ActivityProfileBinding? = null
    private var preferenceManager: PreferenceManager? = null
    private var database: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        try {
            preferenceManager = PreferenceManager(applicationContext)
            loadUserDetails()
            database = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
        }
        binding!!.profleUpdateBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                Toast.makeText(applicationContext, "Add Later", Toast.LENGTH_SHORT).show()
            }
        })
        binding!!.logoutBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                try {
                    logOut()
                } catch (e: Exception) {
                }
            }
        })
    }

    private fun loadUserDetails() {
        try {
            binding!!.profileUsername.setText(preferenceManager!!.getString(Constants.KEY_NAME))
            val email: String? = preferenceManager!!.getString(Constants.KEY_EMAIL)
            binding!!.profileEmail.setText(email)
            val encodedString: String? = preferenceManager!!.getString(Constants.KEY_IMAGE)
            if (encodedString != null) {
                val bytes: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
                val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding!!.profileImageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
        }
    }

    private fun logOut() {
        try {
            showToast("LogOut...") // Assuming this is a method to show a toast message
            val database: FirebaseFirestore = FirebaseFirestore.getInstance()
            val documentReference: DocumentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                    (preferenceManager!!.getString(Constants.KEY_USER_ID))!!
                )
            val update: HashMap<String, Any> = HashMap()
            update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())
            documentReference.update(update)
                .addOnSuccessListener(OnSuccessListener({ unused: Void? ->
                    // Clear preferences
                    preferenceManager!!.clear()
                    startActivity(Intent(applicationContext, LogInActivity::class.java))
                    finish()
                })).addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        showToast("Faiuld LogOut")
                    }
                })
        } catch (e: Exception) {
            // Handle exceptions
        }
    }
}