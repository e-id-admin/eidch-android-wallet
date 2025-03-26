package ch.admin.foitt.wallet.platform.utils

import android.text.format.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun ZonedDateTime.asDayAndMonth(locale: Locale = Locale.getDefault()): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "ddMMM")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asHourMinutes(locale: Locale = Locale.getDefault()): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "HHmm")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asDayMonthYear(locale: Locale = Locale.getDefault()): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "ddMMMyyyy")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asDayFullMonthYear(locale: Locale = Locale.getDefault()): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "ddMMMMyyyy")
    return formatPattern(localizedPattern, locale)
}

fun ZonedDateTime.asMonthYear(locale: Locale = Locale.getDefault()): String {
    val localizedPattern = DateFormat.getBestDateTimePattern(locale, "MMMM yyyy")
    return formatPattern(localizedPattern, locale).uppercase(locale)
}

fun Long.epochSecondsToZonedDateTime(): ZonedDateTime = Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault())

private fun ZonedDateTime.formatPattern(
    pattern: String,
    locale: Locale,
) = format(DateTimeFormatter.ofPattern(pattern, locale))
