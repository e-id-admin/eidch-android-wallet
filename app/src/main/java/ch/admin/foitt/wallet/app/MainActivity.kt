package ch.admin.foitt.wallet.app

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ch.admin.foitt.wallet.app.presentation.MainScreen
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userInteraction: UserInteraction

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.setRecentsScreenshotEnabled(false)
        }
        setContent {
            MainScreen(this)
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userInteraction()
    }
}
