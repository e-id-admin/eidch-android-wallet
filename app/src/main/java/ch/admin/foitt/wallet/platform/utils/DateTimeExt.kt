@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.utils

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun ZonedDateTime.asHourMinutes(locale: Locale): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "HHmm")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asDayMonthYear(locale: Locale): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "ddMMMyyyy")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asDayFullMonthYear(locale: Locale): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "ddMMMMyyyy")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asDayFullMonthYearHoursMinutes(locale: Locale): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "ddMMMMyyyy hh:mm a")
    return formatPattern(localizedPattern, locale).uppercase(locale)
}

fun ZonedDateTime.asDayMonthYearHoursMinutesWithPipe(locale: Locale): String {
    val localizedDatePattern = DateFormat.getBestDateTimePattern(locale, "ddMMyyyy")
    val localizedTimePattern = DateFormat.getBestDateTimePattern(locale, "HH:mm")
    return formatPattern("$localizedDatePattern | $localizedTimePattern", locale).uppercase(locale)
}

fun ZonedDateTime.asDayMonthYearHoursMinutesWith(separator: String, locale: Locale): String {
    val localizedDatePattern = DateFormat.getBestDateTimePattern(locale, "ddMMyyyy")
    val localizedTimePattern = DateFormat.getBestDateTimePattern(locale, "HH:mm")
    return formatPattern("$localizedDatePattern$separator$localizedTimePattern", locale).uppercase(locale)
}

fun ZonedDateTime.asBestLocalizedForPattern(locale: Locale, pattern: String): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, pattern)
    return this.formatPattern(localizedPattern, locale)
}

@Composable
fun Instant.asDayMonthYear(): String {
    val localizedDatePattern = DateFormat.getBestDateTimePattern(currentLocale, "dd.MM.yyyy")
    return toZonedDateTime().formatPattern(localizedDatePattern, currentLocale).uppercase(currentLocale)
}

@Composable
fun Instant.asHourMinutes(): String {
    val localizedDatePattern = DateFormat.getBestDateTimePattern(currentLocale, "HH:mm")
    return toZonedDateTime().formatPattern(localizedDatePattern, currentLocale).uppercase(currentLocale)
}

private val currentLocale
    @Composable
    get() = LocalConfiguration.current.locales[0]

fun Instant.toZonedDateTime(): ZonedDateTime = atZone(ZoneId.systemDefault())

fun Long.epochSecondsToZonedDateTime(): ZonedDateTime = Instant.ofEpochSecond(this).toZonedDateTime()

private fun ZonedDateTime.formatPattern(
    pattern: String,
    locale: Locale,
) = format(DateTimeFormatter.ofPattern(pattern, locale))
