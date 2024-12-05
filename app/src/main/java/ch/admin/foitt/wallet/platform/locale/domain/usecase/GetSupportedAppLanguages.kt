package ch.admin.foitt.wallet.platform.locale.domain.usecase

import java.util.Locale

interface GetSupportedAppLanguages {
    operator fun invoke(): List<Locale>
}
