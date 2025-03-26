package ch.admin.foitt.openid4vc.di

import ch.admin.foitt.openid4vc.data.CredentialOfferRepositoryImpl
import ch.admin.foitt.openid4vc.data.FetchDidLogRepositoryImpl
import ch.admin.foitt.openid4vc.data.PresentationRequestRepositoryImpl
import ch.admin.foitt.openid4vc.data.TypeMetadataRepositoryImpl
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.repository.FetchDidLogRepository
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMaps
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import ch.admin.foitt.openid4vc.domain.usecase.CreateJWSKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.GenerateKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialPresentation
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateAnyDescriptorMapsImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateAnyVerifiablePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateCredentialRequestProofJwtImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateDidJwkImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateJWSKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.DeclinePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.DeleteKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchCredentialByConfigImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchIssuerCredentialInformationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchPresentationRequestImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchVerifiableCredentialImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GenerateKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.ResolveDidImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.SubmitAnyCredentialPresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.VerifyJwtSignatureImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtDescriptorMap
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtDescriptorMapImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtVerifiablePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchVcSdJwtCredentialImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.VerifyPublicKeyImpl
import ch.admin.foitt.openid4vc.utils.SafeJson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.Clock
import javax.inject.Named

@Module(includes = [OpenId4VcModule::class])
@InstallIn(ActivityRetainedComponent::class)
class ExternalOpenId4VcModule {
    @Provides
    internal fun provideCredentialOfferRepository(
        httpClient: HttpClient,
        safeJson: SafeJson,
    ): CredentialOfferRepository = CredentialOfferRepositoryImpl(httpClient, safeJson)
}

@Module(includes = [OpenId4VCBindings::class])
@InstallIn(ActivityRetainedComponent::class)
interface ExternalOpenId4VcBindings {
    @Binds
    fun bindVerifyJwtSignature(
        useCase: VerifyJwtSignatureImpl
    ): VerifyJwtSignature
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class OpenId4VcModule {

    @ActivityRetainedScoped
    @Provides
    fun provideHttpClient(engine: HttpClientEngine, @Named("OpenId4VcJsonSerializer") jsonSerializer: Json): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    @Provides
    fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Named("OpenId4VcJsonSerializer")
    fun provideJsonSerializer(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            classDiscriminatorMode = ClassDiscriminatorMode.NONE
        }
    }

    @Provides
    fun provideSafeJson(@Named("OpenId4VcJsonSerializer") json: Json) = SafeJson(json)

    @ActivityRetainedScoped
    @Provides
    fun provideClock(): Clock = Clock.systemUTC()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface OpenId4VCBindings {
    @Binds
    @ActivityRetainedScoped
    fun bindFetchDidLogRepository(
        repository: FetchDidLogRepositoryImpl
    ): FetchDidLogRepository

    @Binds
    fun bindPresentationRequestRepository(
        repository: PresentationRequestRepositoryImpl
    ): PresentationRequestRepository

    @Binds
    fun bindFetchIssuerCredentialInformation(
        useCase: FetchIssuerCredentialInformationImpl
    ): FetchIssuerCredentialInformation

    @Binds
    fun bindFetchVerifiableCredential(
        useCase: FetchVerifiableCredentialImpl
    ): FetchVerifiableCredential

    @Binds
    fun bindCreateCredentialRequestProofJwt(
        useCase: CreateCredentialRequestProofJwtImpl
    ): CreateCredentialRequestProofJwt

    @Binds
    fun bindCreateJWSKeyPair(
        useCase: CreateJWSKeyPairImpl
    ): CreateJWSKeyPair

    @Binds
    fun bindFetchPresentationRequest(
        useCase: FetchPresentationRequestImpl
    ): FetchPresentationRequest

    @Binds
    fun bindSubmitAnyCredentialPresentation(
        useCase: SubmitAnyCredentialPresentationImpl
    ): SubmitAnyCredentialPresentation

    @Binds
    fun bindCreateAnyVerifiablePresentation(
        useCase: CreateAnyVerifiablePresentationImpl
    ): CreateAnyVerifiablePresentation

    @Binds
    fun bindCreateVcSdJwtVerifiablePresentation(
        useCase: CreateVcSdJwtVerifiablePresentationImpl
    ): CreateVcSdJwtVerifiablePresentation

    @Binds
    fun bindCreateAnyDescriptorMaps(
        useCase: CreateAnyDescriptorMapsImpl
    ): CreateAnyDescriptorMaps

    @Binds
    fun bindCreateVcSdJwtDescriptorMap(
        useCase: CreateVcSdJwtDescriptorMapImpl
    ): CreateVcSdJwtDescriptorMap

    @Binds
    fun bindDeclinePresentation(
        useCase: DeclinePresentationImpl
    ): DeclinePresentation

    @Binds
    fun bindGetKeyPair(
        useCase: GetKeyPairImpl
    ): GetKeyPair

    @Binds
    fun bindGenerateKeyPair(
        useCase: GenerateKeyPairImpl
    ): GenerateKeyPair

    @Binds
    fun bindDeleteKeyPair(
        useCase: DeleteKeyPairImpl
    ): DeleteKeyPair

    @Binds
    fun bindCreateDidJwk(
        useCase: CreateDidJwkImpl
    ): CreateDidJwk

    @Binds
    fun bindFetchCredentialByConfig(
        useCase: FetchCredentialByConfigImpl
    ): FetchCredentialByConfig

    @Binds
    fun bindFetchVcSdJwtCredential(
        useCase: FetchVcSdJwtCredentialImpl
    ): FetchVcSdJwtCredential

    @Binds
    fun bindVerifyPublicKey(
        verifier: VerifyPublicKeyImpl
    ): VerifyPublicKey

    @Binds
    fun bindResolveDid(
        resolver: ResolveDidImpl
    ): ResolveDid

    @Binds
    @ActivityRetainedScoped
    fun bindTypeMetadataRepository(
        repo: TypeMetadataRepositoryImpl
    ): TypeMetadataRepository
}
