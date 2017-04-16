package com.academiaexpress.Data

import com.google.gson.JsonObject
import com.google.gson.JsonParser

class User(var firstName: String?, var lastName: String?, var email: String?, var phoneNumber: String?) {
    fun asString(): String {
        val userObject = JsonObject()
        userObject.addProperty("firstName", firstName)
        userObject.addProperty("lastName", lastName)
        userObject.addProperty("email", email)
        userObject.addProperty("phoneNumber", phoneNumber)
        return userObject.toString()
    }

    companion object {
        fun fromString(string: String): User? {
            try {
                val jsonReader = JsonParser()
                val userObject = jsonReader.parse(string) as JsonObject

                return User(userObject.get("firstName").asString,
                        userObject.get("lastName").asString,
                        userObject.get("email").asString,
                        userObject.get("phoneNumber").asString)
            } catch (e: Exception) {
                return  null
            }
        }
    }
}
