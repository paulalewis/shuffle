package com.castlefrog.shuffle.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsLogger(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsLogger {
    override fun logEvent(name: String, data: Map<String, String>) {
        firebaseAnalytics.logEvent(name, Bundle().apply {
            data.forEach { (key, value) ->
                putString(key, value)
            }
        })
    }
}