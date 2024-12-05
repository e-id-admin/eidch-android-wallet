package ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation

import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetLocalizedDateTime
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.utils.asDayMonthYear
import ch.admin.foitt.wallet.platform.utils.asHourMinutes
import java.time.ZonedDateTime
import javax.inject.Inject

internal class GetLocalizedDateTimeImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale,
) : GetLocalizedDateTime {
    override fun invoke(dateTime: ZonedDateTime): String {
        val currentLocale = getCurrentAppLocale()
        val date = dateTime.asDayMonthYear(currentLocale)
        val time = dateTime.asHourMinutes(currentLocale)
        return "$date | $time"
    }
}
