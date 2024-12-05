package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

object MockCredential {
    val vcSdjCredentialProd = VcSdJwtCredential(
        id = 1L,
        signingKeyId = "signingkeyID",
        signingAlgorithm = SigningAlgorithm.ES256,
        payload = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6dGR3OmZvbz06aWRlbnRpZmllci1kYXRhLXNlcnZpY2UtZC5iaXQuYWRtaW4uY2g6Y" +
            "XBpOnYxOmRpZDpiYXIiLCJpYXQiOjE3MzA3OTI0MTR9.brSn50CpAmXSI1BsmCeY2kdQFirT2WX4aUJkUnoKKVI"
    )

    val vcSdjCredentialBeta = VcSdJwtCredential(
        id = 1L,
        signingKeyId = "signingkeyID",
        signingAlgorithm = SigningAlgorithm.ES256,
        payload = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6dGR3OmZvbz06aWRlbnRpZmllci1yZWctYS50cnVzdC1pbmZyYS5zd2l5dS1pbnQuY" +
            "WRtaW4uY2g6YXBpOnYxOmRpZDpiYXIiLCJpYXQiOjE3MzA3OTI0MTR9.ftXOqizrqwYK8Tqu9_APr2QqaBP4UwL56m6aEQrR73M"
    )

    val vcSdjCredentialEmptyPayload = VcSdJwtCredential(
        id = 1L,
        signingKeyId = "signingkeyID",
        signingAlgorithm = SigningAlgorithm.ES256,
        payload = ""
    )
}
