package com.academiaexpress.Data

class DeliveryUser(var firstName: String?, var secondName: String?, var email: String?, var phoneNumber: String?) {

    fun asString(): String {
        return firstName + "DeliveryUser" +
                secondName + "DeliveryUser" +
                email + "DeliveryUser" +
                phoneNumber + "DeliveryUser"
    }

    companion object {
        fun fromString(string: String): DeliveryUser {
            val arr = string.split("DeliveryUser".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return DeliveryUser(arr[0], arr[1], arr[2], "")
        }
    }
}
