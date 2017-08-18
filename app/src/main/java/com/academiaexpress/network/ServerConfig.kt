package com.academiaexpress.network

import com.academiaexpress.BuildConfig

object ServerConfig {
    val baseUrl =
            if (BuildConfig.DEBUG) "https://academia-delivery-staging.herokuapp.com/api"
            else "http://academia-delivery.herokuapp.com"

    val apiURL = baseUrl + "/"

    val imageUrl: String
        get() {
            return ""
        }

    val prefsName = "AcademiaPrefs"
    val tokenEntity = "auth_token"
    val userEntity = "auth_info"
}
