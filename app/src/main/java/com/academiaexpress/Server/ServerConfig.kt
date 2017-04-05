package com.academiaexpress.Server

object ServerConfig {
    val baseUrl = "http://academia-delivery-staging.herokuapp.com"
    val apiURL = baseUrl + "/"

    val imageUrl: String
        get() {
            return ""
        }

    val prefsName = "ChistoPrefs"
    val tokenEntity = "auth_token"
    val userEntity = "auth_info"
}
