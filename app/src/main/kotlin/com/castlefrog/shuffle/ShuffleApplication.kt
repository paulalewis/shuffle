package com.castlefrog.shuffle

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.castlefrog.shuffle.analytics.AnalyticsLogger
import com.castlefrog.shuffle.analytics.FirebaseAnalyticsLogger
import com.castlefrog.shuffle.repository.InMemoryShuffleListRepository
import com.castlefrog.shuffle.repository.ShuffleListRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class ShuffleApplication : Application() {
    // private lateinit var shuffleDatabaseService: ShuffleDatabaseService

    private var isDebug: Boolean = true
    lateinit var analyticsLogger: AnalyticsLogger
    lateinit var shuffleListRepository: ShuffleListRepository

    override fun onCreate() {
        super.onCreate()
        isDebug = (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        setUpLogging()
        setupAnalytics()
        setupRoomService()
        setupShuffleListRepository()
    }

    private fun setUpLogging() {
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    when (priority) {
                        Log.WARN, Log.ERROR -> {
                            FirebaseCrashlytics.getInstance().recordException(createException(message, t))
                        }
                        else -> {}
                    }
                }

                private fun createException(message: String, t: Throwable?): Throwable {
                    return t ?: Exception(message)
                }
            })
        }
    }

    private fun setupAnalytics() {
        if (isDebug) {
            analyticsLogger = object : AnalyticsLogger {
                override fun logEvent(name: String, data: Map<String, String>) {
                    Timber.i("Event: $name, Data: $data")
                }
            }
        } else {
            analyticsLogger = FirebaseAnalyticsLogger(FirebaseAnalytics.getInstance(this))
        }
    }

    private fun setupRoomService() {
        // shuffleDatabaseService = ShuffleDatabaseServiceImpl(this)
    }

    private fun setupShuffleListRepository() {
        shuffleListRepository = InMemoryShuffleListRepository()
    }
}

private fun Context.getAppContext(): ShuffleApplication {
    return applicationContext as ShuffleApplication
}

fun Context.getShuffleListRepository(): ShuffleListRepository {
    return getAppContext().shuffleListRepository
}

fun Context.getAnalyticsLogger(): AnalyticsLogger {
    return getAppContext().analyticsLogger
}
