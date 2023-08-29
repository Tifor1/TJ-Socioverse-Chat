package socioverse.tifor.Adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import socioverse.tifor.Model.ChatMessage
import socioverse.tifor.databinding.ItemContainerReceivedMessageBinding
import socioverse.tifor.databinding.ItemContainerSentMessageBinding

class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private var receiverProfileImage: Bitmap,
    private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setReceiverProfileImage(bitmap: Bitmap) {
        receiverProfileImage = bitmap
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(
                chatMessages[position], receiverProfileImage
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    internal class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }

    internal class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap?) {
            try {
                binding.textMessage.text = chatMessage.message
                binding.textDateTime.text = chatMessage.dateTime
                if (receiverProfileImage != null) {
                    binding.imageProfile.setImageBitmap(receiverProfileImage)
                }
            } catch (e: Exception) {
                // Handle exceptions here if needed
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}