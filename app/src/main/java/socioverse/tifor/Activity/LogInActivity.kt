package socioverse.tifor.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import socioverse.tifor.databinding.ActivityLogInBinding
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager

class LogInActivity : AppCompatActivity() {
    private var binding: ActivityLogInBinding? = null
    private var auth: FirebaseAuth? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            preferenceManager = PreferenceManager(applicationContext)
            if (preferenceManager!!.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
        }
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        try {
            auth = FirebaseAuth.getInstance()
            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } catch (e: Exception) {
        }
        binding!!.buttonSignIn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                try {
                    if (isValidSignInDetails) {
                        val email = binding!!.inputEmail.text.toString()
                        val password = binding!!.inputPassword.text.toString()
                        auth!!.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    logInChat()
                                    Toast.makeText(
                                        applicationContext,
                                        "please wait...",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    loading(false)
                                    Toast.makeText(
                                        this@LogInActivity,
                                        "Password, E-mail Check {Otherwise your account is not Created} So SignUp now",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener { }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LogInActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding!!.textCreateNewAccount.setOnClickListener {
            try {
                val intent = Intent(this@LogInActivity, SignUpActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@LogInActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val isValidSignInDetails: Boolean
        private get() = if (binding!!.inputEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
            binding!!.inputEmail.error = "Enter E-mail"
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding!!.inputEmail.text.toString())
                .matches()
        ) {
            binding!!.inputEmail.error = "Enter Valid E-mail"
            false
        } else if (binding!!.inputPassword.text.toString().trim { it <= ' ' }.isEmpty()) {
            binding!!.inputPassword.error = "Enter Password"
            false
        } else {
            loading(true)
            true
        }

    private fun loading(isLoading: Boolean) {
        try {
            if (isLoading) {
                binding!!.buttonSignIn.visibility = View.INVISIBLE
                binding!!.progressBar.visibility = View.VISIBLE
            } else {
                binding!!.progressBar.visibility = View.INVISIBLE
                binding!!.buttonSignIn.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
        }
    }

    private fun logInChat() {
        try {
            val database = FirebaseFirestore.getInstance()
            database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding!!.inputEmail.text.toString())
                .whereEqualTo(
                    Constants.KEY_PASSWORD, binding!!.inputPassword.text.toString()
                ).get().addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    if (task.isSuccessful && task.result != null && task.result!!
                            .documents.size > 0
                    ) {
                        val documentSnapshot = task.result!!.documents[0]
                        preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager!!.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                        preferenceManager!!.putString(
                            Constants.KEY_NAME, documentSnapshot.getString(
                                Constants.KEY_NAME
                            )
                        )
                        preferenceManager!!.putString(
                            Constants.KEY_EMAIL, documentSnapshot.getString(
                                Constants.KEY_EMAIL
                            )
                        )
                        preferenceManager!!.putString(
                            Constants.KEY_IMAGE, documentSnapshot.getString(
                                Constants.KEY_IMAGE
                            )
                        )
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        loading(false)
                    }
                }
        } catch (e: Exception) {
        }
    }
}