package ch.admin.foitt.wallet.platform.scaffold.domain.model

import androidx.activity.SystemBarStyle

fun interface SystemBarsSetter {
    operator fun invoke(
        statusBarStyle: SystemBarStyle,
        navigationBarStyle: SystemBarStyle,
    )
}
