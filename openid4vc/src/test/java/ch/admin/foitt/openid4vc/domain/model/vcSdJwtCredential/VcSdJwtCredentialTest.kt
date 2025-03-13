package ch.admin.foitt.openid4vc.domain.model.vcSdJwtCredential

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class VcSdJwtCredentialTest {
    @Test
    fun `Credential containing non-disclosable claims in the jwt returns true`() = runTest {
        val vcSdJwtCredential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_NON_DISCLOSABLE_CLAIMS)

        assertTrue(vcSdJwtCredential.hasNonDisclosableClaims())
    }

    @Test
    fun `Credential not containing non-disclosable claims in the jwt returns false`() = runTest {
        val vcSdJwtCredential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE)

        assertFalse(vcSdJwtCredential.hasNonDisclosableClaims())
    }

    @ParameterizedTest
    @MethodSource("generateTestInputs")
    fun `getClaimsJson returns only the disclosable claims as json`(input: Pair<String, String>) = runTest {
        val vcSdJwtCredential = createVcSdJwtCredential(input.first)

        assertEquals(
            SafeJsonTestInstance.json.parseToJsonElement(input.second),
            vcSdJwtCredential.getClaimsToSave(),
        )
    }

    @Test
    fun `getClaimsJson returns json containing technical and non-technical claims`() = runTest {
        val vcSdJwtCredential = createVcSdJwtCredential(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE)

        assertEquals(
            SafeJsonTestInstance.json.parseToJsonElement(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE_PLUS_TECHNICAL_CLAIMS_JSON),
            vcSdJwtCredential.getClaimsForPresentation()
        )
    }

    private fun createVcSdJwtCredential(payload: String) = VcSdJwtCredential(
        keyBindingIdentifier = null,
        keyBindingAlgorithm = null,
        payload = payload,
    )

    private companion object {
        @JvmStatic
        fun generateTestInputs() = listOf(
            VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE to VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE_JSON,
            VcSdJwtMocks.VC_SD_JWT_NON_DISCLOSABLE_CLAIMS to VcSdJwtMocks.VC_SD_JWT_NON_DISCLOSABLE_CLAIMS_JSON
        )
    }
}
