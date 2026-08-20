package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt

data class RawAndParsedIssuerCredentialInfo(
    val rawIssuerCredentialInfo: Jwt,
    val issuerCredentialInfo: IssuerCredentialInfo,
)
