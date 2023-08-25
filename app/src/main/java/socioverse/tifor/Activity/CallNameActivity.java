package socioverse.tifor.Activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

import socioverse.tifor.databinding.ActivityCallNameBinding;
import socioverse.tifor.utlities.PreferenceManager;

public class CallNameActivity extends BaseActivity2 {

    private ActivityCallNameBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userId = binding.userId.getText().toString().trim();
                if (userId.isEmpty()) {

                } else {

                    try {
                        startService(userId);
                        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    } catch (Exception e) {

                    }

                }


            }
        });

    }

    private void startService(String userId) {
        try {

            Application application = getApplication(); // Android's application context
            long appID = 900851183;   // yourAppID
            String appSign = "e2dbd39a96fcba5c14649d787451893020a522a12891490ac92bd90e2a62ad3f";  // yourAppSign
            String userID = userId; // yourUserID, userID should only contain numbers, English characters, and '_'.
            String userName = userId;  // yourUserName

            ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
            callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true;
            ZegoNotificationConfig notificationConfig = new ZegoNotificationConfig();
            notificationConfig.sound = "zego_uikit_sound_call";
            notificationConfig.channelID = "CallInvitation";
            notificationConfig.channelName = "CallInvitation";
            ZegoUIKitPrebuiltCallInvitationService.init(getApplication(), appID, appSign, userID, userName, callInvitationConfig);
        } catch (Exception e) {

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ZegoUIKitPrebuiltCallInvitationService.unInit();
        } catch (Exception e) {

        }
    }
}