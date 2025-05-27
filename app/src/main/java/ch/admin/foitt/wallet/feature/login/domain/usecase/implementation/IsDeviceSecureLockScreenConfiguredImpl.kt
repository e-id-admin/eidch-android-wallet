package ch.admin.foitt.wallet.feature.login.domain.usecase.implementation

import android.app.KeyguardManager
import android.content.Context
import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDeviceSecureLockScreenConfigured
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class IsDeviceSecureLockScreenConfiguredImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : IsDeviceSecureLockScreenConfigured {
    private val keyguardManager = appContext.getSystemService(KeyguardManager::class.java)

    @CheckResult
    override fun invoke(): Boolean = keyguardManager.isDeviceSecure
}
