package socioverse.tifor.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import socioverse.tifor.ChatActivity;
import socioverse.tifor.Model.UserModel;
import socioverse.tifor.R;
import socioverse.tifor.Utils.AndroidUtil;
import socioverse.tifor.Utils.FirebaseUtil;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    private Context context;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        try {
            holder.usernameText.setText(model.getUsername());
            /*   holder.phoneText.setText(model.getPhone());*/
            if (model.getUserId().equals(FirebaseUtil.currentUserId())) {
                holder.usernameText.setText(model.getUsername() + " (Me)");
            }

            try {
                FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {
                    try {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                        } else {
                            // Handle failure here, e.g., set a default profile picture
                            holder.profilePic.setImageResource(R.drawable.person_icon);
                        }
                    } catch (Exception e) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Handle failure here, e.g., set a default profile picture
                holder.profilePic.setImageResource(R.drawable.person_icon);
            }

            holder.itemView.setOnClickListener(v -> {
                //navigate to chat activity
                try {
                    Intent intent = new Intent(context, ChatActivity.class);
                    AndroidUtil.passUserModelAsIntent(intent, model);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {

        }
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
            return new UserModelViewHolder(view);
        } catch (Exception e) {
            return null;
        }
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView phoneText;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                usernameText = itemView.findViewById(R.id.user_name_text);
                phoneText = itemView.findViewById(R.id.phone_text);
                profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            } catch (Exception e) {

            }
        }
    }
}