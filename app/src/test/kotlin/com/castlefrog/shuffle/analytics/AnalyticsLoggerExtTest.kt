package com.castlefrog.shuffle.analytics

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AnalyticsLoggerExtTest {

    private data class LoggedEvent(val name: String, val data: Map<String, String>)

    private val logged = mutableListOf<LoggedEvent>()
    private val logger = object : AnalyticsLogger {
        override fun logEvent(name: String, data: Map<String, String>) {
            logged.add(LoggedEvent(name, data))
        }
    }

    @Before
    fun setUp() {
        logged.clear()
    }

    @Test
    fun `logButtonTap logs button_tap event`() {
        logger.logButtonTap("my_button")
        assertEquals(1, logged.size)
        assertEquals("button_tap", logged.first().name)
    }

    @Test
    fun `logButtonTap includes button_name in data`() {
        logger.logButtonTap("my_button")
        assertEquals("my_button", logged.first().data["button_name"])
    }

    @Test
    fun `logButtonTap merges extra data with button_name`() {
        logger.logButtonTap("my_button", mapOf("extra_key" to "extra_value"))
        val data = logged.first().data
        assertEquals("my_button", data["button_name"])
        assertEquals("extra_value", data["extra_key"])
    }

    @Test
    fun `logViewHidden logs view_hidden event with view_name`() {
        logger.logViewHidden("my_view")
        assertEquals(1, logged.size)
        val event = logged.first()
        assertEquals("view_hidden", event.name)
        assertEquals("my_view", event.data["view_name"])
    }

    @Test
    fun `logViewVisible logs view_visible event with view_name`() {
        logger.logViewVisible("my_view")
        assertEquals(1, logged.size)
        val event = logged.first()
        assertEquals("view_visible", event.name)
        assertEquals("my_view", event.data["view_name"])
    }
}
