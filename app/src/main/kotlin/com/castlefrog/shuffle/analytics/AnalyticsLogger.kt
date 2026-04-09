package com.castlefrog.shuffle.analytics

interface AnalyticsLogger {
    fun logEvent(name: String, data: Map<String, String>)
}