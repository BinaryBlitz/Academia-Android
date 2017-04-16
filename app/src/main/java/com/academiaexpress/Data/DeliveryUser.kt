package com.academiaexpress.Data

class DeliveryUser(var firstName: String?, var lastName: String?, var email: String?, var phoneNumber: String?) {
    fun asString(): String {
        return firstName + splitter + lastName + splitter + email + splitter + phoneNumber
    }

    companion object {
        val splitter = "DeliveryUser"
        val FIRST_NAME_INDEX = 0
        val LAST_NAME_INDEX = 1
        val EMAIL_INDEX = 2
        val PHONE_NUMBER_INDEX = 3

        fun fromString(string: String): DeliveryUser? {
            try {
                // get string array from preferences
                val fields = string.split(splitter.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                return DeliveryUser(fields[FIRST_NAME_INDEX], fields[LAST_NAME_INDEX], fields[EMAIL_INDEX], fields[PHONE_NUMBER_INDEX])
            } catch (e: Exception) {
                return  null
            }
        }
    }
}
