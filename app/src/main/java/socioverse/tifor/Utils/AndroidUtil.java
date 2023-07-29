package socioverse.tifor.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import socioverse.tifor.Model.UserModel;

public class AndroidUtil {

    public static void showToast(Context context, String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {

        }
    }

    public static void passUserModelAsIntent(Intent intent, UserModel model) {
        try {
            intent.putExtra("username", model.getUsername());
            intent.putExtra("phone", model.getPhone());
            intent.putExtra("userId", model.getUserId());
            intent.putExtra("fcmToken", model.getFcmToken());
        } catch (Exception e) {

        }
    }

    public static UserModel getUserModelFromIntent(Intent intent) {
        try {
            UserModel userModel = new UserModel();
            userModel.setUsername(intent.getStringExtra("username"));
            userModel.setPhone(intent.getStringExtra("phone"));
            userModel.setUserId(intent.getStringExtra("userId"));
            userModel.setFcmToken(intent.getStringExtra("fcmToken"));
            return userModel;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView) {
        try {
            Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
        } catch (Exception e) {

        }
    }
}