package socioverse.tifor.listeners

import socioverse.tifor.Model.User

interface ConversionListener {
    fun OnConversionClicked(user: User?)
}