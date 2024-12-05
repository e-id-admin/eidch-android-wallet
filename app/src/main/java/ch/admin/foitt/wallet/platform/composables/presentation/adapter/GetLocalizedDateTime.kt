package ch.admin.foitt.wallet.platform.composables.presentation.adapter

import java.time.ZonedDateTime

fun interface GetLocalizedDateTime {
    operator fun invoke(dateTime: ZonedDateTime): String
}
