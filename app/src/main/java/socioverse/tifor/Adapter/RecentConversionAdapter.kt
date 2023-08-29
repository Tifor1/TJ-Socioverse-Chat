package socioverse.tifor.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import socioverse.tifor.Adapter.RecentConversionAdapter.ConversionViewHolder
import socioverse.tifor.Model.ChatMessage
import socioverse.tifor.Model.User
import socioverse.tifor.databinding.ItemContainerRecentConversionBinding
import socioverse.tifor.listeners.ConversionListener

class RecentConversionAdapter(
    private val chatMessages: List<ChatMessage>, private val conversionListener: ConversionListener
) : RecyclerView.Adapter<ConversionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemContainerRecentConversionBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class ConversionViewHolder(var binding: ItemContainerRecentConversionBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage) {
            try {
                binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage))
                binding.textName.text = chatMessage.conversionName
                binding.textRecentMessage.text = chatMessage.message
            } catch (e: Exception) {
                e.printStackTrace()
            }
            binding.root.setOnClickListener { v: View? ->
                try {
                    val user = User()
                    user.userId = chatMessage.conversionId
                    user.username = chatMessage.conversionName
                    user.image = chatMessage.conversionImage
                    conversionListener.OnConversionClicked(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getConversionImage(encodedImage: String?): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}