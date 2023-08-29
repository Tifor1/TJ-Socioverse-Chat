package socioverse.tifor.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import socioverse.tifor.Adapter.UserAdapter.UserViewHolder
import socioverse.tifor.Model.User
import socioverse.tifor.databinding.ItemContainerUsersBinding
import socioverse.tifor.listeners.UserListener

class UserAdapter(private val users: List<User>, private val userListener: UserListener) :
    RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUsersBinding =
            ItemContainerUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(itemContainerUsersBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserdata(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(var binding: ItemContainerUsersBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun setUserdata(user: User) {
            try {
                binding.textName.text = user.username
                binding.textEmail.text = user.email
                binding.imageProfile.setImageBitmap(getUserImage(user.image))
                //// Click User For ChatActivity
                binding.root.setOnClickListener { v: View? -> userListener.onUserClicked(user) }
            } catch (e: Exception) {
            }
        }
    }

    private fun getUserImage(encodedImage: String?): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}