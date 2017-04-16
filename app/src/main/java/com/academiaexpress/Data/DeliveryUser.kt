package com.academiaexpress.Data

class DeliveryUser(var firstName: String?, var lastName: String?, var email: String?, var phoneNumber: String?) {

    fun asString(): String {
        return firstName + splitter + lastName + splitter + email + splitter + phoneNumber
    }

    companion object {
        val splitter = "DeliveryUser"

        fun fromString(string: String): DeliveryUser? {
            try {
                val arr = string.split(splitter.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                return DeliveryUser(arr[0], arr[1], arr[2], arr[3])
            } catch (e: Exception) {
                return  null
            }
        }
    }
}
