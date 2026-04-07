package com.castlefrog.shuffle.logger

interface AnalyticsLogger {
    fun logEvent(name: String, data: Map<String, String>)
}