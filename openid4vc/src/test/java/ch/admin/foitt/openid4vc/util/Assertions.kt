package ch.admin.foitt.openid4vc.util

import org.junit.jupiter.api.Assertions.fail

fun assertTrue(condition: Boolean, message: () -> String) {
    if (!condition) {
        fail<String>(message())
    }
}
