package ch.admin.foitt.openid4vc.util

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.asErr
import com.github.michaelbull.result.asOk
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import kotlin.reflect.KClass

fun <T, V> Result<T, V>.assertOk(): T {
    assertTrue(isOk) { "an error occurred: ${asErr<T, V, V>().error}" }
    return get()!!
}

fun <T, V> Result<T, V>.assertErr(): V {
    assertTrue(isErr) { "an unexpected success occurred: ${(asOk<T, V, T>()).value}" }
    return getError()!!
}

inline fun <T, V : Any, reified U : V> Result<T, V>.assertErrorType(type: KClass<U>): U {
    val error = assertErr()
    assertTrue(type.isInstance(error)) { "the error is not of the right type: ${error::class.simpleName}" }
    return error as U
}
