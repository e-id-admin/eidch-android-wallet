package ch.admin.foitt.openid4vc.domain.model.anycredential

import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.recoverCatching
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun String.toBusinessExpiryInstant(): Instant? = toInstant() ?: runSuspendCatching {
    OffsetDateTime.parse(this).toLocalDate().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)
}.recoverCatching {
    LocalDate.parse(this).atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)
}.get()

internal fun String.toInstant(): Instant? = runSuspendCatching {
    Instant.ofEpochSecond(this.toLong())
}.recoverCatching {
    Instant.parse(this)
}.get()
