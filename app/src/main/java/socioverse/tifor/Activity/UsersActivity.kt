package socioverse.tifor.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import socioverse.tifor.Adapter.UserAdapter
import socioverse.tifor.Model.User
import socioverse.tifor.databinding.ActivityUsersBinding
import socioverse.tifor.listeners.UserListener
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager

class UsersActivity : BaseActivity(), UserListener {
    private var binding: ActivityUsersBinding? = null
    private var preferenceManager: PreferenceManager? = null
    var database: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        try {
            database = FirebaseFirestore.getInstance()
            preferenceManager = PreferenceManager(applicationContext)

            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            // Set up the RecyclerView
            val layoutManager = LinearLayoutManager(this)
            binding!!.userRecyclerView.layoutManager = layoutManager
        } catch (e: Exception) {
        }
        try {
            users
        } catch (e: Exception) {
            // Handle any exceptions here
        }
    }

    // Use "UsersActivity.this"
    private val users: Unit
        private get() {
            try {
                loading(true)
                database!!.collection(Constants.KEY_COLLECTION_USERS).get()
                    .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                        loading(false)
                        val currentUserId = preferenceManager!!.getString(Constants.KEY_USER_ID)
                        if (task.isSuccessful && task.result != null) {
                            val users: MutableList<User> = ArrayList()
                            for (queryDocumentSnapshot in task.result!!) {
                                if (currentUserId == queryDocumentSnapshot.id) {
                                    continue
                                }
                                try {
                                    val user = User()
                                    user.username =
                                        queryDocumentSnapshot.getString(Constants.KEY_NAME)
                                    user.email =
                                        queryDocumentSnapshot.getString(Constants.KEY_EMAIL)
                                    user.image =
                                        queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
                                    user.fcmToken =
                                        queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                                    user.userId = queryDocumentSnapshot.id
                                    users.add(user)
                                } catch (e: Exception) {
                                }
                            }
                            if (users.size > 0) {
                                try {
                                    val userAdapter = UserAdapter(users, this)
                                    binding!!.userRecyclerView.adapter = userAdapter
                                    binding!!.userRecyclerView.visibility = View.VISIBLE
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@UsersActivity,
                                        "Error Displaying Users.",
                                        Toast.LENGTH_SHORT
                                    ).show() // Use "UsersActivity.this"
                                }
                            } else {
                                showErrorMessage()
                            }
                        } else {
                            showErrorMessage()
                        }
                    }
            } catch (e: Exception) {
            }
        }

    private fun showErrorMessage() {
        try {
            binding!!.textErrorMessage.text = String.format("%s", "{ TF Chat } No Users Available")
            binding!!.textErrorMessage.visibility = View.VISIBLE
        } catch (e: Exception) {
        }
    }

    private fun loading(isLoading: Boolean) {
        try {
            if (isLoading) {
                binding!!.progressBar.visibility = View.VISIBLE
            } else {
                binding!!.progressBar.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
        }
    }

    override fun onUserClicked(user: User?) {
        try {
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra(Constants.KEY_USER, user)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
        }
    }

}