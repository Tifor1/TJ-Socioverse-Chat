package socioverse.tifor.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import socioverse.tifor.Adapter.ChatAdapter
import socioverse.tifor.Model.ChatMessage
import socioverse.tifor.Model.User
import socioverse.tifor.databinding.ActivityChatBinding
import socioverse.tifor.utlities.Constants
import socioverse.tifor.utlities.PreferenceManager
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.Objects

class ChatActivity : BaseActivity() {
    private var binding: ActivityChatBinding? = null
    private var receiverUser: User? = null
    private var chatMessages: MutableList<ChatMessage>? = null
    private var chatAdapter: ChatAdapter? = null
    private var preferenceManager: PreferenceManager? = null
    private var database: FirebaseFirestore? = null
    private var conversionId: String? = null
    private var isReceiverAvailable = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        try {
            // Enable system default mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            setListeners()
            loadReceiverData()
            init()
            listenMessage()
            call()
        } catch (e: Exception) {
        }
        binding!!.inputMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                try {
                    // Show or hide the Send button based on EditText's content
                    if (charSequence.length > 0) {
                        binding!!.layoutSend.visibility = AppCompatImageView.VISIBLE
                    } else {
                        binding!!.layoutSend.visibility = AppCompatImageView.GONE
                    }
                } catch (e: Exception) {
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun init() {
        try {
            preferenceManager = PreferenceManager(applicationContext)
            chatMessages = ArrayList()
            chatAdapter = ChatAdapter(
                chatMessages as ArrayList<ChatMessage>,
                getBitmapFromcodedString(receiverUser!!.image)!!,
                preferenceManager!!.getString(
                    Constants.KEY_USER_ID
                )!!
            )
            binding!!.chatRecyclerView.adapter = chatAdapter
            database = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
        }
    }

    private fun sendMessage() {
        try {
            val message = HashMap<String, Any?>()
            message[Constants.KEY_SENDER_ID] = preferenceManager!!.getString(Constants.KEY_USER_ID)
            message[Constants.KEY_RECEIVER_ID] = receiverUser!!.userId
            message[Constants.KEY_MESSAGE] = binding!!.inputMessage.text.toString()
            message[Constants.KEY_TIMESTAMP] = Date()
            database!!.collection(Constants.KEY_COLLECTION_CHAT).add(message)
            if (conversionId != null) {
                updateConversion(binding!!.inputMessage.text.toString())
            } else {
                val conversion = HashMap<String, Any?>()
                conversion[Constants.KEY_SENDER_ID] =
                    preferenceManager!!.getString(Constants.KEY_USER_ID)
                conversion[Constants.KEY_SENDER_NAME] =
                    preferenceManager!!.getString(Constants.KEY_NAME)
                conversion[Constants.KEY_SENDER_IMAGE] =
                    preferenceManager!!.getString(Constants.KEY_IMAGE)
                conversion[Constants.KEY_RECEIVER_ID] = receiverUser!!.userId
                conversion[Constants.KEY_RECEIVER_NAME] = receiverUser!!.username
                conversion[Constants.KEY_RECEIVER_EMAIL] = receiverUser!!.email
                conversion[Constants.KEY_RECEIVER_IMAGE] = receiverUser!!.image
                conversion[Constants.KEY_LAST_MESSAGE] = binding!!.inputMessage.text.toString()
                conversion[Constants.KEY_TIMESTAMP] = Date()
                addConversion(conversion)
            }
            try {
                if (!isReceiverAvailable) {
                    sendNotification(binding!!.inputMessage.text.toString().trim { it <= ' ' })
                }
            } catch (e: Exception) {
            }
            if (binding!!.inputMessage.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding!!.inputMessage.error = "Enter Text"
            }
            binding!!.inputMessage.text = null
        } catch (e: Exception) {
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(messageBody: String) {
        try {
            val user = User()
            val senderName = preferenceManager!!.getString(Constants.KEY_NAME)
            val jsonObject = JSONObject()
            val notificationObj = JSONObject()
            notificationObj.put("title", senderName)
            notificationObj.put("body", messageBody)
            val dataObj = JSONObject()
            dataObj.put("userId", Constants.KEY_USER_ID)
            jsonObject.put("notification", notificationObj)
            jsonObject.put("data", dataObj)
            jsonObject.put("to", receiverUser!!.fcmToken)
            callApi(jsonObject)
        } catch (e: Exception) {
            // Handle any exception that may occur while constructing the JSON
            e.printStackTrace()
        }
    }

    fun callApi(jsonObject: JSONObject) {
        try {


            val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
            val client = OkHttpClient()
            val url = "https://fcm.googleapis.com/fcm/send"
            val body: RequestBody = jsonObject.toString().toRequestBody(JSON)
            val request: Request = Request.Builder().url(url).post(body).header(
                "Authorization",
                "Bearer AAAAiq-kvyQ:APA91bFSxdPEg869MvqzVlDlIFoBzhWdtHt-DQDjuw2UgcsjiaYo1XBU9KYl_AviVvdSHiPn84uwGWJHD66GHA6lPGSMYhsLUh7jnEvcf7UsyPHP5R1-CMEybNU2IqSilYIgEN0xbLPL"
            ).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle the failure of the API call
                    e.printStackTrace()
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    // Handle the response of the API call, if needed
                    if (response.isSuccessful) {
                        // The API call was successful
                    } else {
                    }
                }

            })
        } catch (e: Exception) {
        }
    }

    private fun listenAvailabilityOfReceiver() {
        try {
            database!!.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser!!.userId!!
            )
                .addSnapshotListener(this@ChatActivity) { value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (value != null) {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            val availability =
                                Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY))
                                    .toInt()
                            isReceiverAvailable = availability == 1
                        }
                        receiverUser!!.fcmToken = value.getString(Constants.KEY_FCM_TOKEN)
                        if (receiverUser!!.image == null) {
                            receiverUser!!.image = value.getString(Constants.KEY_IMAGE)
                            chatAdapter!!.setReceiverProfileImage(
                                getBitmapFromcodedString(
                                    receiverUser!!.image
                                )!!
                            )
                            chatAdapter!!.notifyItemRangeChanged(0, chatMessages!!.size)
                        }
                    }
                    if (isReceiverAvailable) {
                        binding!!.textAvailability.visibility = View.VISIBLE
                    } else {
                        binding!!.textAvailability.visibility = View.GONE
                    }
                }
        } catch (e: Exception) {
        }
    }

    private fun listenMessage() {
        try {
            database!!.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(
                Constants.KEY_SENDER_ID, preferenceManager!!.getString(
                    Constants.KEY_USER_ID
                )
            ).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser!!.userId)
                .addSnapshotListener(eventListener)
            database!!.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser!!.userId).whereEqualTo(
                    Constants.KEY_RECEIVER_ID, preferenceManager!!.getString(Constants.KEY_USER_ID)
                ).addSnapshotListener(eventListener)
        } catch (e: Exception) {
        }
    }

    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            try {
                if (error != null) {
                    return@EventListener
                }
                if (value != null) {
                    val count = chatMessages!!.size
                    for (documentChange in value.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            val chatMessage = ChatMessage()
                            chatMessage.senderId =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)
                            chatMessage.receiverId =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            chatMessage.message =
                                documentChange.document.getString(Constants.KEY_MESSAGE)
                            chatMessage.dateTime = getReadableDateTime(
                                documentChange.document.getDate(
                                    Constants.KEY_TIMESTAMP
                                )
                            )
                            chatMessage.dateObject =
                                documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                            chatMessages!!.add(chatMessage)
                        }
                    }
                    Collections.sort(chatMessages) { obj1: ChatMessage, obj2: ChatMessage ->
                        obj1.dateObject!!.compareTo(
                            obj2.dateObject
                        )
                    }
                    if (count == 0) {
                        chatAdapter!!.notifyDataSetChanged()
                    } else {
                        chatAdapter!!.notifyItemRangeInserted(
                            chatMessages!!.size, chatMessages!!.size
                        )
                        binding!!.chatRecyclerView.smoothScrollToPosition(chatMessages!!.size - 1)
                    }
                    binding!!.chatRecyclerView.visibility = View.VISIBLE
                }
                binding!!.progressBar.visibility = View.GONE
                if (conversionId == null) {
                    checkForConversion()
                }
            } catch (e: Exception) {
            }
        }

    private fun getBitmapFromcodedString(encodedImage: String?): Bitmap? {
        return if (encodedImage != null) {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }

    private fun loadReceiverData() {
        try {
            receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User?
            binding!!.textName.text = receiverUser!!.username
        } catch (e: Exception) {
        }
    }

    private fun setListeners() {
        try {
            binding!!.imageBack.setOnClickListener { v: View? -> onBackPressed() }
            binding!!.layoutSend.setOnClickListener { v: View? -> sendMessage() }
        } catch (e: Exception) {
        }
    }

    private fun getReadableDateTime(date: Date?): String {
        return SimpleDateFormat("dd,MMM,yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversion(conversion: HashMap<String, Any?>) {
        try {
            database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(conversion)
                .addOnSuccessListener { documentReference: DocumentReference ->
                    conversionId = documentReference.id
                }
        } catch (e: Exception) {
        }
    }

    private fun updateConversion(message: String) {
        try {
            val documentReference =
                database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(
                    conversionId!!
                )
            documentReference.update(
                Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, Date()
            )
        } catch (e: Exception) {
        }
    }

    private fun checkForConversion() {
        try {
            if (chatMessages!!.size != 0) {
                checkForConversionRemotely(
                    preferenceManager!!.getString(Constants.KEY_USER_ID), receiverUser!!.userId
                )
                checkForConversionRemotely(
                    receiverUser!!.userId, preferenceManager!!.getString(
                        Constants.KEY_USER_ID
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    private fun checkForConversionRemotely(senderId: String?, receiverId: String?) {
        try {
            database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId).whereEqualTo(
                    Constants.KEY_RECEIVER_ID, receiverId
                ).get().addOnCompleteListener(conversionOnCompleteListener)
        } catch (e: Exception) {
        }
    }

    private val conversionOnCompleteListener = OnCompleteListener { task: Task<QuerySnapshot?> ->
        try {
            if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
                val documentSnapshot = task.result!!.documents[0]
                conversionId = documentSnapshot.id
            }
        } catch (e: Exception) {
        }
    }

    private fun call() {
        audioCall()
        videoCall()
    }

    private fun audioCall() {
        binding!!.imageCall.setOnClickListener {
            try {
                val intent = Intent(applicationContext, CallNameActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
            }
        }
    }

    private fun videoCall() {
        binding!!.imageVcall.setOnClickListener {
            try {
                val intent = Intent(applicationContext, CallNameActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
            }
        }
    }

    override fun onResume() {
        try {
            listenAvailabilityOfReceiver()
        } catch (e: Exception) {
        }
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        call()
    }
}