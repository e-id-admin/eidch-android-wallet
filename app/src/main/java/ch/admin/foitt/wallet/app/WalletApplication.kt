package ch.admin.foitt.wallet.app

import android.app.Application
import android.util.Log.ERROR
import android.util.Log.WARN
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ReportError
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WalletApplication : Application() {
    @Inject lateinit var reportError: ReportError

    override fun onCreate() {
        super.onCreate()
        setupLogging()
    }

    private fun setupLogging() {
        val trees = mutableListOf<Timber.Tree>(
            // Dynatrace tree
            object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    when (priority) {
                        ERROR, WARN -> reportError(message, t)
                    }
                }
            }
        )

        // debug log tree
        if (BuildConfig.DEBUG) {
            trees.add(
                object : Timber.DebugTree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        super.log(priority, "sw_$tag", message, t)
                    }
                }
            )
        }

        trees.forEach { tree ->
            tree
                .takeIf { it !in Timber.forest() }
                ?.apply { Timber.plant(this) }
        }
    }
}
