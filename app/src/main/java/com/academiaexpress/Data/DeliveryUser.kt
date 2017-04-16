package com.academiaexpress.Data

class DeliveryUser(var firstName: String?, var lastName: String?, var email: String?, var phoneNumber: String?) {

    fun asString(): String {
        return firstName + splitter + lastName + splitter + email + splitter + phoneNumber
    }

    companion object {
        val splitter = "DeliveryUser"

        fun fromString(string: String): DeliveryUser? {
            try {
                // get string array from preferences
                val fields = string.split(splitter.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                return DeliveryUser(fields[0], fields[1], fields[2], fields[3])
            } catch (e: Exception) {
                return  null
            }
        }
    }
}
