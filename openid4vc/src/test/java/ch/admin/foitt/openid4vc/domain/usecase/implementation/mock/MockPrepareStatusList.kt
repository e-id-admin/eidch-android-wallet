package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import com.nimbusds.jose.Payload
import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import io.mockk.mockk

object MockPrepareStatusList {
    private val payload = mockk<Payload>()

    val signedJWT = mockk<SignedJWT> {
        every { payload } returns this@MockPrepareStatusList.payload
    }
}
