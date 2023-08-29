package socioverse.tifor.Model

import java.util.Date

class ChatMessage {
    @JvmField
    var senderId: String? = null

    @JvmField
    var receiverId: String? = null

    @JvmField
    var message: String? = null

    @JvmField
    var dateTime: String? = null

    @JvmField
    var dateObject: Date? = null

    @JvmField
    var conversionId: String? = null

    @JvmField
    var conversionName: String? = null

    @JvmField
    var conversionImage: String? = null
}