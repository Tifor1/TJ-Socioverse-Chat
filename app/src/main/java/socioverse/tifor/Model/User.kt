package socioverse.tifor.Model

import java.io.Serializable

class User : Serializable {
    @JvmField
    var username: String? = null

    @JvmField
    var image: String? = null

    @JvmField
    var email: String? = null

    @JvmField
    var fcmToken: String? = null

    @JvmField
    var userId: String? = null
}