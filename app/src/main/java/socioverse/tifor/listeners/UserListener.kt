package socioverse.tifor.listeners

import socioverse.tifor.Model.User

interface UserListener {
    fun onUserClicked(user: User?)
}