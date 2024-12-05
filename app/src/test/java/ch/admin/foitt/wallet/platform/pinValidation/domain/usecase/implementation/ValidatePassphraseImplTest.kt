package ch.admin.foitt.wallet.platform.pinValidation.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseConstraints
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.implementation.ValidatePassphraseImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidatePassphraseImplTest {

    @Test
    fun `passphrase should fulfill default passphrase constraints`() {
        val usecase = ValidatePassphraseImpl(constraints = PassphraseConstraints())
        assertEquals(PassphraseValidationState.INVALID_MIN_LENGTH, usecase.invoke("123"))
        assertEquals(PassphraseValidationState.INVALID_MIN_LENGTH, usecase.invoke(""))
        assertEquals(PassphraseValidationState.INVALID_MIN_LENGTH, usecase.invoke("abcde"))

        assertEquals(PassphraseValidationState.VALID, usecase.invoke("123456"))
        assertEquals(PassphraseValidationState.VALID, usecase.invoke("abcdef"))
        assertEquals(PassphraseValidationState.VALID, usecase.invoke("pa55phra53"))
    }
}
