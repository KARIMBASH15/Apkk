package com.example

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyDWNcGEMdSanI722ZBNQ-YZQ9F4j_9N0YQ")
                .setApplicationId("1:99061582921:web:c497ec6a1ac90961f58398")
                .setProjectId("dwar-ce8f2")
                .setStorageBucket("dwar-ce8f2.firebasestorage.app")
                .setGcmSenderId("99061582921")
                .build()
            FirebaseApp.initializeApp(context, options)
        }
    }
}
