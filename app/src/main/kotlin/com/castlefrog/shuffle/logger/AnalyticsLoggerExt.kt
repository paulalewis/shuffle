package com.castlefrog.shuffle.logger

private object Event {
    const val BUTTON_TAP = "button_tap"
    const val VIEW_HIDDEN = "view_hidden"
    const val VIEW_VISIBLE = "view_visible"
}

private object Key {
    const val BUTTON_NAME = "button_name"
    const val VIEW_NAME = "view_name"
}

fun AnalyticsLogger.logButtonTap(
    name: String,
    data: Map<String, String> = emptyMap(),
) {
    logEvent(
        Event.BUTTON_TAP,
        data.apply {
            plus(Key.BUTTON_NAME to name)
        }
    )
}

fun AnalyticsLogger.logViewHidden(name: String) {
    logEvent(
        Event.VIEW_HIDDEN,
        mapOf(Key.VIEW_NAME to name)
    )
}

fun AnalyticsLogger.logViewVisible(name: String) {
    logEvent(
        Event.VIEW_VISIBLE,
        mapOf(Key.VIEW_NAME to name)
    )
}
