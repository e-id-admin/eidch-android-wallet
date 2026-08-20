package ch.admin.foitt.wallet.platform.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.recover

fun <V, E> Result<V, E>.ignoreErrorUnless(condition: Boolean): Result<V?, E> = if (condition) {
    this
} else {
    this.recover {
        null
    }
}
