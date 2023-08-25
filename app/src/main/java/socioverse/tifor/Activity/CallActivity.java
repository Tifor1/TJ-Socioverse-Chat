package socioverse.tifor.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

import socioverse.tifor.databinding.ActivityCallBinding;
import socioverse.tifor.utlities.PreferenceManager;

public class CallActivity extends BaseActivity2 {

    private ActivityCallBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        String userId = getIntent().getStringExtra("userId");

        binding.userCode.setText("Invite Code :" + userId);

        binding.userId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                try {
                    String userId = binding.userId.getText().toString().trim();
                    setVoiceCall(userId);
                    setVIdeoCall(userId);
                } catch (Exception e) {

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }


    private void setVoiceCall(String targetUserId) {

        binding.call.setIsVideoCall(false);
        binding.call.setResourceID("zego_uikit_call");
        binding.call.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId)));


    }

    private void setVIdeoCall(String targetUserId) {

        binding.vCall.setIsVideoCall(true);
        binding.vCall.setResourceID("zego_uikit_call");
        binding.vCall.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId)));

    }
}