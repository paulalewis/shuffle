package com.castlefrog.shuffle

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.castlefrog.shuffle.analytics.AnalyticsLogger
import com.castlefrog.shuffle.analytics.FirebaseAnalyticsLogger
import com.castlefrog.shuffle.repository.ShuffleListRepositoryImpl
import com.castlefrog.shuffle.repository.ShuffleListRepository
import com.castlefrog.shuffle.repository.room.ShuffleDatabase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

private const val SHUFFLE_PREFS = "shuffle_prefs"

class ShuffleApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private var isDebug: Boolean = true
    lateinit var analyticsLogger: AnalyticsLogger
    lateinit var shuffleListRepository: ShuffleListRepository

    override fun onCreate() {
        super.onCreate()
        isDebug = (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        setUpLogging()
        setupAnalytics()
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
        analyticsLogger = if (isDebug) {
            object : AnalyticsLogger {
                override fun logEvent(name: String, data: Map<String, String>) {
                    Timber.i("Event: $name, Data: $data")
                }
            }
        } else {
            FirebaseAnalyticsLogger(FirebaseAnalytics.getInstance(this))
        }
    }

    private fun setupShuffleListRepository() {
        val db = ShuffleDatabase.getInstance(this, applicationScope)
        shuffleListRepository = ShuffleListRepositoryImpl(
            dao = db.shuffleListDao(),
            sharedPreferences = getSharedPreferences(SHUFFLE_PREFS, MODE_PRIVATE)
        )
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
