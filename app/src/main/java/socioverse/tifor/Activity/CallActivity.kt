package socioverse.tifor.Activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import socioverse.tifor.databinding.ActivityCallBinding
import socioverse.tifor.utlities.PreferenceManager

class CallActivity : BaseActivity2() {

    private lateinit var binding: ActivityCallBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        val userId = intent.getStringExtra("userId")
        binding.userCode.text = "Invite Code : $userId"

        binding.userId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                try {
                    val newUserId = binding.userId.text.toString().trim()
                    setVoiceCall(newUserId)
                    setVideoCall(newUserId)
                } catch (e: Exception) {
                    // Handle the exception
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun setVoiceCall(targetUserId: String) {
        binding.call.setIsVideoCall(false)
        binding.call.resourceID = "zego_uikit_call"
        binding.call.setInvitees(listOf(ZegoUIKitUser(targetUserId)))
    }

    private fun setVideoCall(targetUserId: String) {
        binding.vCall.setIsVideoCall(true)
        binding.vCall.resourceID = "zego_uikit_call"
        binding.vCall.setInvitees(listOf(ZegoUIKitUser(targetUserId)))
    }
}