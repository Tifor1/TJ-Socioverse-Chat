package socioverse.tifor.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import socioverse.tifor.Model.User;
import socioverse.tifor.databinding.ItemContainerUsersBinding;
import socioverse.tifor.listeners.UserListener;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private UserListener userListener;

    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUsersBinding itemContainerUsersBinding = ItemContainerUsersBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new UserViewHolder(itemContainerUsersBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.setUserdata(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUsersBinding binding;

        UserViewHolder(ItemContainerUsersBinding itemContainerUsersBinding) {
            super(itemContainerUsersBinding.getRoot());
            binding = itemContainerUsersBinding;

        }

        void setUserdata(User user) {

            try {
                binding.textName.setText(user.username);
                binding.textEmail.setText(user.email);
                binding.imageProfile.setImageBitmap(getUserImage(user.image));
                //// Click User For ChatActivity
                binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            } catch (Exception e) {

            }
        }


    }

    private Bitmap getUserImage(String encodedImage) {

        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }

}
