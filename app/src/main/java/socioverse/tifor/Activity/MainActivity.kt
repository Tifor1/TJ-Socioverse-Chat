package socioverse.tifor.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import socioverse.tifor.Adapter.RecentConversionAdapter
import socioverse.tifor.Model.ChatMessage
import socioverse.tifor.Model.User
import socioverse.tifor.R
import socioverse.tifor.databinding.ActivityMainBinding
import socioverse.tifor.listeners.ConversionListener
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager
import java.util.Collections

class MainActivity : BaseActivity2(), ConversionListener {
    private var binding: ActivityMainBinding? = null
    private var preferenceManager: PreferenceManager? = null
    private var conversions: MutableList<ChatMessage>? = null
    private var conversionAdapter: RecentConversionAdapter? = null
    private var database: FirebaseFirestore? = null
    private val user: User? = null
    private val channelname = "Socioverse"
    private val channelId = "socioverse"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        try {
            preferenceManager = PreferenceManager(applicationContext)
            loadUserDetails()
            init()
            token
            setListeners()
            listenConversation()
            permissionNotification()
            showNotification()

            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } catch (e: Exception) {
        }
        binding!!.imageProfile.setOnClickListener {
            try {
                val intent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
            }
        }
    }

    private fun init() {
        try {
            conversions = ArrayList()
            conversionAdapter = RecentConversionAdapter(conversions as ArrayList<ChatMessage>, this)
            binding!!.conversionRecyclerView.adapter = conversionAdapter
            database = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
        }
    }

    private fun setListeners() {
        try {
            binding!!.imageSignOut.setOnClickListener { v: View? -> logOut() }
            binding!!.fabNewChat.setOnClickListener { v: View? ->
                startActivity(
                    Intent(
                        applicationContext, UsersActivity::class.java
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    private fun loadUserDetails() {
        try {
            binding!!.textName.text = preferenceManager!!.getString(Constants.KEY_NAME)
            val encodedString = preferenceManager!!.getString(Constants.KEY_IMAGE)
            if (encodedString != null) {
                val bytes = Base64.decode(encodedString, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding!!.imageProfile.setImageBitmap(bitmap)
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

    private fun listenConversation() {
        try {
            database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(
                Constants.KEY_SENDER_ID, preferenceManager!!.getString(
                    Constants.KEY_USER_ID
                )
            ).addSnapshotListener(eventListener)
            database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(
                Constants.KEY_RECEIVER_ID, preferenceManager!!.getString(
                    Constants.KEY_USER_ID
                )
            ).addSnapshotListener(eventListener)
        } catch (e: Exception) {
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            try {
                if (error != null) {
                    return@EventListener
                }
                if (value != null) {
                    for (documentChange in value.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            val senderId =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)
                            val receiverId =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            val chatMessage = ChatMessage()
                            chatMessage.senderId = senderId
                            chatMessage.receiverId = receiverId
                            if (preferenceManager!!.getString(Constants.KEY_USER_ID) == senderId) {
                                chatMessage.conversionImage =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                                chatMessage.conversionName =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_NAME)
                                chatMessage.conversionId =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            } else {
                                chatMessage.conversionImage =
                                    documentChange.document.getString(Constants.KEY_SENDER_IMAGE)
                                chatMessage.conversionName =
                                    documentChange.document.getString(Constants.KEY_SENDER_NAME)
                                chatMessage.conversionId =
                                    documentChange.document.getString(Constants.KEY_SENDER_ID)
                            }
                            chatMessage.message =
                                documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                            chatMessage.dateObject =
                                documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                            conversions!!.add(chatMessage)
                        } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                            for (i in conversions!!.indices) {
                                val senderId =
                                    documentChange.document.getString(Constants.KEY_SENDER_ID)
                                val receiverId =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                                if (conversions!![i].senderId == senderId && conversions!![i].receiverId == receiverId) {
                                    conversions!![i].message =
                                        documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                                    conversions!![i].dateObject =
                                        documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                                    break
                                }
                            }
                        }
                    }
                    Collections.sort(conversions) { obj1: ChatMessage, obj2: ChatMessage ->
                        obj2.dateObject!!.compareTo(
                            obj1.dateObject
                        )
                    }
                    conversionAdapter!!.notifyDataSetChanged()
                    binding!!.conversionRecyclerView.smoothScrollToPosition(0)
                    binding!!.conversionRecyclerView.visibility = View.VISIBLE
                    binding!!.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
            }
        }
    private val token: Unit
        private get() {
            try {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
                    updateToken(
                        token
                    )
                }
            } catch (e: Exception) {
            }
        }

    private fun updateToken(token: String) {
        try {
            preferenceManager!!.putString(Constants.KEY_FCM_TOKEN, token)
            val database = FirebaseFirestore.getInstance()
            val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager!!.getString(Constants.KEY_USER_ID)!!
            )
            documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener { e: Exception? -> showToast("Unable To Update Token") }
        } catch (e: Exception) {
        }
    }

    private fun logOut() {
        try {
            showToast("LogOut...") // Assuming this is a method to show a toast message
            val database = FirebaseFirestore.getInstance()
            val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager!!.getString(Constants.KEY_USER_ID)!!
            )
            val update = HashMap<String, Any>()
            update[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
            documentReference.update(update).addOnSuccessListener { unused: Void? ->
                // Clear preferences
                preferenceManager!!.clear()
                startActivity(Intent(applicationContext, LogInActivity::class.java))
                finish()
            }.addOnFailureListener { showToast("Faiuld LogOut") }
        } catch (e: Exception) {
            // Handle exceptions
        }
    }

    override fun OnConversionClicked(user: User?) {
        try {
            val intent = Intent(applicationContext, ChatActivity::class.java)
            intent.putExtra(Constants.KEY_USER, user)
            startActivity(intent)
        } catch (e: Exception) {
        }
    }

    private fun showNotification() {
        try {
            val customSoundUri =
                Uri.parse("android.resource://" + packageName + "/" + R.raw.notification)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // Set the custom sound for the notification.
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                val channel =
                    NotificationChannel(channelId, channelname, NotificationManager.IMPORTANCE_HIGH)
                channel.setSound(customSoundUri, audioAttributes)
                channel.setShowBadge(true)
                val vibrationPattern = longArrayOf(100, 50, 100, 50, 100, 50, 100, 50, 100)
                channel.vibrationPattern = vibrationPattern
                notificationManager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
        }
    }

    private fun permissionNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionNotif = ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionNotif != PackageManager.PERMISSION_GRANTED) {
                val NOTIF_PERM = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                ActivityCompat.requestPermissions(this, NOTIF_PERM, 10)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            loadUserDetails()
            token
            permissionNotification()
            showNotification()
        } catch (e: Exception) {
        }
    }
}