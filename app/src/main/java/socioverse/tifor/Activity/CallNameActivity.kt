package socioverse.tifor.Activity

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import socioverse.tifor.databinding.ActivityCallNameBinding
import socioverse.tifor.utlities.PreferenceManager

class CallNameActivity : BaseActivity2() {
    private lateinit var binding: ActivityCallNameBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)


        binding.send.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val userId: String = binding.userId.text.toString().trim()
                if (userId.isEmpty()) {
                } else {
                    try {
                        startService(userId)
                        val intent: Intent =
                            Intent(applicationContext, CallActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                    } catch (e: Exception) {
                    }
                }
            }
        })
    }

    private fun startService(userId: String) {
        try {
            val application: Application = application // Android's application context
            val appID: Long = 900851183 // yourAppID
            val appSign: String =
                "e2dbd39a96fcba5c14649d787451893020a522a12891490ac92bd90e2a62ad3f" // yourAppSign
            val userID: String =
                userId // yourUserID, userID should only contain numbers, English characters, and '_'.
            val userName: String = userId // yourUserName
            val callInvitationConfig: ZegoUIKitPrebuiltCallInvitationConfig =
                ZegoUIKitPrebuiltCallInvitationConfig()
            callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true
            val notificationConfig: ZegoNotificationConfig = ZegoNotificationConfig()
            notificationConfig.sound = "zego_uikit_sound_call"
            notificationConfig.channelID = "CallInvitation"
            notificationConfig.channelName = "CallInvitation"
            ZegoUIKitPrebuiltCallInvitationService.init(
                getApplication(), appID, appSign, userID, userName, callInvitationConfig
            )
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            ZegoUIKitPrebuiltCallInvitationService.unInit()
        } catch (e: Exception) {
        }
    }
}