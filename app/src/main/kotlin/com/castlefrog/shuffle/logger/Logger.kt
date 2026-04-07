package com.castlefrog.shuffle.logger

interface Logger {
    fun d(tag: String? = null, msg: String? = null, tr: Throwable? = null)
    fun w(tag: String? = null, msg: String? = null, tr: Throwable? = null)
    fun e(tag: String? = null, msg: String? = null, tr: Throwable? = null)
}