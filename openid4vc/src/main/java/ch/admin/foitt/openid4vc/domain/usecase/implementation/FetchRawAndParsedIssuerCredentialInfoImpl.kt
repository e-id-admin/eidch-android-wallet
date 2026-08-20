package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.ValidateIssuerMetadataJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class FetchRawAndParsedIssuerCredentialInfoImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val validateIssuerMetadataJwt: ValidateIssuerMetadataJwt,
) : FetchRawAndParsedIssuerCredentialInfo {
    override suspend fun invoke(
        issuerEndpoint: URL,
        forceRefresh: Boolean,
    ): Result<RawAndParsedIssuerCredentialInfo, FetchIssuerCredentialInfoError> = coroutineBinding {
        val rawAndParsedInfo = credentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(
            issuerEndpoint = issuerEndpoint,
            forceRefresh = forceRefresh,
        ).bind()
        if (rawAndParsedInfo.issuerCredentialInfo.credentialIssuer != issuerEndpoint) {
            Err(CredentialOfferError.InvalidSignedMetadata("Credential issuers do not match")).bind<RawAndParsedIssuerCredentialInfo>()
        }
        validateIssuerMetadataJwt(
            credentialIssuerIdentifier = issuerEndpoint.toString(),
            jwt = rawAndParsedInfo.rawIssuerCredentialInfo,
            type = TYPE
        ).mapError(ValidateIssuerMetadataJwtError::toFetchIssuerCredentialInfoError)
            .bind()

        rawAndParsedInfo
    }

    companion object {
        const val TYPE = "openidvci-issuer-metadata+jwt"
    }
}
