package ch.admin.foitt.wallet.platform.trustRegistry.di

import ch.admin.foitt.wallet.platform.trustRegistry.data.TrustStatementRepositoryImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedVerificationClaims
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessVerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateNonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateVerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.CheckActorComplianceImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.FetchVcSchemaTrustStatusImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustDomainFromDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustUrlFromDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.IsTrustedDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessIdentityTrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessIdentityV1TrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessProtectedIssuanceAuthorizationTrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessProtectedIssuanceTrustListStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessVerificationAuthorizationTrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateIdentityTrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateNonComplianceTrustListStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateProtectedIssuanceAuthorizationTrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateProtectedIssuanceTrustListStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementJwtImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementStatusImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateVerificationAuthorizationTrustStatementImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TrustRegistryModule {

    @Provides
    fun provideProtectedVerificationClaims() = ProtectedVerificationClaims()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface TrustRegistryBindings {
    @Binds
    fun bindProcessIdentityTrustStatement(
        useCase: ProcessIdentityTrustStatementImpl
    ): ProcessIdentityTrustStatement

    @Binds
    fun bindProcessIdentityV1TrustStatement(
        useCase: ProcessIdentityV1TrustStatementImpl
    ): ProcessIdentityV1TrustStatement

    @Binds
    fun bindFetchVcSchemaTrustStatus(
        useCase: FetchVcSchemaTrustStatusImpl
    ): FetchVcSchemaTrustStatus

    @Binds
    fun bindCheckActorCompliance(
        useCase: CheckActorComplianceImpl
    ): CheckActorCompliance

    @Binds
    fun bindGetTrustDomainFromDid(
        useCase: GetTrustDomainFromDidImpl
    ): GetTrustDomainFromDid

    @Binds
    fun bindGetTrustUrlFromDid(
        useCase: GetTrustUrlFromDidImpl
    ): GetTrustUrlFromDid

    @Binds
    fun bindIsTrustedDid(
        useCase: IsTrustedDidImpl
    ): IsTrustedDid

    @Binds
    @ActivityRetainedScoped
    fun bindTrustStatementRepository(
        repo: TrustStatementRepositoryImpl
    ): TrustStatementRepository

    @Binds
    fun bindValidateTrustStatement(
        useCase: ValidateTrustStatementImpl
    ): ValidateTrustStatement

    @Binds
    fun bindValidateTrustStatementJwt(
        useCase: ValidateTrustStatementJwtImpl
    ): ValidateTrustStatementJwt

    @Binds
    fun bindValidateIdentityTrustStatement(
        useCase: ValidateIdentityTrustStatementImpl
    ): ValidateIdentityTrustStatement

    @Binds
    fun bindValidateNonComplianceTrustListStatement(
        useCase: ValidateNonComplianceTrustListStatementImpl
    ): ValidateNonComplianceTrustListStatement

    @Binds
    fun bindValidateTrustStatementStatus(
        useCase: ValidateTrustStatementStatusImpl
    ): ValidateTrustStatementStatus

    @Binds
    fun bindProcessProtectedIssuanceTrustListStatement(
        useCase: ProcessProtectedIssuanceTrustListStatementImpl
    ): ProcessProtectedIssuanceTrustListStatement

    @Binds
    fun bindValidateProtectedIssuanceTrustListStatement(
        useCase: ValidateProtectedIssuanceTrustListStatementImpl
    ): ValidateProtectedIssuanceTrustListStatement

    @Binds
    fun bindProcessProtectedIssuanceAuthorizationTrustStatement(
        useCase: ProcessProtectedIssuanceAuthorizationTrustStatementImpl
    ): ProcessProtectedIssuanceAuthorizationTrustStatement

    @Binds
    fun bindValidateProtectedIssuanceAuthorizationTrustStatement(
        useCase: ValidateProtectedIssuanceAuthorizationTrustStatementImpl
    ): ValidateProtectedIssuanceAuthorizationTrustStatement

    @Binds
    fun bindProcessVerificationAuthorizationTrustStatement(
        useCase: ProcessVerificationAuthorizationTrustStatementImpl
    ): ProcessVerificationAuthorizationTrustStatement

    @Binds
    fun bindValidateVerificationAuthorizationTrustStatement(
        useCase: ValidateVerificationAuthorizationTrustStatementImpl
    ): ValidateVerificationAuthorizationTrustStatement
}
