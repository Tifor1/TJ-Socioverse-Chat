package socioverse.tifor.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import socioverse.tifor.databinding.ActivitySignUpBinding
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {
    private var binding: ActivitySignUpBinding? = null
    private var auth: FirebaseAuth? = null
    private var preferenceManager: PreferenceManager? = null
    private var encodedImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        try {
            auth = FirebaseAuth.getInstance()
            preferenceManager = PreferenceManager(applicationContext)
        } catch (e: Exception) {
        }
        binding!!.textprivacyPolicy.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW)
                browserIntent.data =
                    Uri.parse("https://doc-hosting.flycricket.io/socioverse/a988998a-db42-4b5d-a0b5-b9af1fe9d7e1/privacy")
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        binding!!.buttonSignUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                try {
                    if (isValidSignInDetails) {
                        val email = binding!!.inputEmail.text.toString()
                        val password = binding!!.inputPassword.text.toString()
                        auth!!.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        signUpChat()
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "Your Data Save In Our Storage",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent =
                                            Intent(this@SignUpActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        loading(false)
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "Password, E-mail Check {Otherwise your account is Created} So LogIn now",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding!!.textSignIn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                try {
                    val intent = Intent(this@SignUpActivity, LogInActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding!!.layoutImage.setOnClickListener({ v: View? ->
            try {
                val intent: Intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                picImage.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private val isValidSignInDetails: Boolean
        private get() {
            if (binding!!.inputName.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding!!.inputName.error = "Enter your Name"
                return false
            } else if (binding!!.inputEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding!!.inputEmail.error = "Enter your Email"
                return false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding!!.inputEmail.text.toString())
                    .matches()
            ) {
                binding!!.inputEmail.error = "Enter Valid E-mail"
                return false
            } else if (binding!!.inputPassword.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding!!.inputPassword.error = "Enter Password"
                return false
            } else if (encodedImage == null) {
                showToast("Select Profile Image")
                return false
            } else {
                loading(true)
                return true
            }
        }

    private fun loading(isLoading: Boolean) {
        try {
            if (isLoading) {
                binding!!.buttonSignUp.visibility = View.INVISIBLE
                binding!!.progressBar.visibility = View.VISIBLE
            } else {
                binding!!.progressBar.visibility = View.INVISIBLE
                binding!!.buttonSignUp.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
        }
    }

    private fun signUpChat() {
        try {
            val database = FirebaseFirestore.getInstance()
            val user = HashMap<String, Any?>()
            user[Constants.KEY_NAME] = binding!!.inputName.text.toString()
            user[Constants.KEY_EMAIL] = binding!!.inputEmail.text.toString()
            user[Constants.KEY_PASSWORD] = binding!!.inputPassword.text.toString()
            user[Constants.KEY_IMAGE] = encodedImage
            database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(
                { documentReference: DocumentReference ->
                    loading(false)
                    preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager!!.putString(Constants.KEY_USER_ID, documentReference.id)
                    preferenceManager!!.putString(
                        Constants.KEY_NAME,
                        binding!!.inputName.text.toString()
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_EMAIL,
                        binding!!.inputEmail.text.toString()
                    )
                    preferenceManager!!.putString(Constants.KEY_IMAGE, encodedImage)
                })
        } catch (e: Exception) {
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val picImage =
        registerForActivityResult<Intent, ActivityResult>(ActivityResultContracts.StartActivityForResult(),
            { result: ActivityResult ->
                try {
                    if (result.resultCode == RESULT_OK) {
                        if (result.data != null) {
                            val imageUri: Uri? = result.data!!.data
                            try {
                                val inputStream: InputStream? =
                                    contentResolver.openInputStream((imageUri)!!)
                                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                                binding!!.imageProfile.setImageBitmap(bitmap)
                                binding!!.textAddImage.visibility = View.GONE
                                encodedImage = encodeImage(bitmap)
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            })

    private fun showToast(message: String) {
        try {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            return
        }
    }
}