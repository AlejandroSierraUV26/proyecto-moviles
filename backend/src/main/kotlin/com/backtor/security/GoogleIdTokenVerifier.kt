package com.backtor.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload


object GoogleIdTokenVerifier {
    private val transport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()

    private val verifier = com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setAudience(listOf("968858227331-rmh6rbmoq58pao5birsh7pcl72ielda6.apps.googleusercontent.com")) // ⚠️ usa tu clientId real aquí
        .build()

    fun verifyToken(idTokenString: String): GoogleIdToken.Payload? {
        val idToken = verifier.verify(idTokenString)
        return idToken?.payload
    }
}

