package com.academiaexpress.Server

object ServerConfig {
    val baseUrl = "http://academia-delivery.herokuapp.com"
    val apiURL = baseUrl + "/"

    val imageUrl: String
        get() {
            return ""
        }

    val prefsName = "AcademiaPrefs"
    val tokenEntity = "auth_token"
    val userEntity = "auth_info"
}
