package ch.admin.foitt.wallet.platform.locale.domain.usecase

import java.util.Locale

interface GetCurrentAppLocale {
    operator fun invoke(): Locale
}
