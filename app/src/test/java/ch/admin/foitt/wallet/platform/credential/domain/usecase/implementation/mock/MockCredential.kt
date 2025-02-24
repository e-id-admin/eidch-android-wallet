package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

object MockCredential {
    val vcSdJwtCredentialProd = VcSdJwtCredential(
        id = 1L,
        keyBindingIdentifier = "signingkeyID",
        keyBindingAlgorithm = SigningAlgorithm.ES256,
        payload = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OnByb2QtaWRlbnRmaWVyIiwKICAidmN0IjoidmN0Igp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLmFkalpWdjBSTVhZY2RsVlU3ZnUtV0RraFZrbHQySTJpOU5pSmRlUjZwTGU3b0xHNFZ2THFDX0FPdEU3Zk1TQVMzU3B2OHB4VnREOTJBWk5jOGFxNUV3"
    )

    val vcSdJwtCredentialBeta = VcSdJwtCredential(
        id = 1L,
        keyBindingIdentifier = "signingkeyID",
        keyBindingAlgorithm = SigningAlgorithm.ES256,
        payload = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OmZvbz06aWRlbnRpZmllci1yZWctYS50cnVzdC1pbmZyYS5zd2l5dS1pbnQuYWRtaW4uY2g6YmFyIiwKICAidmN0IjoidmN0Igp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLjRVZTlHZWNrZ21kUjA0Z2dSS3dVX21Ua1RaSVh1Nk5OSUdZb1U5U0duN2tMdkxlQXBMaHhzNUEyYmdtT1NKTV9QSHNBRTdsZEtHS01GNUtzaUY3OGhn"
    )
}
