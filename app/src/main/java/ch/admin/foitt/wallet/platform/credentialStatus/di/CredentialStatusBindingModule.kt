package ch.admin.foitt.wallet.platform.credentialStatus.di

import ch.admin.foitt.wallet.platform.credentialStatus.data.CredentialStatusRepositoryImpl
import ch.admin.foitt.wallet.platform.credentialStatus.domain.repository.CredentialStatusRepository
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchStatusFromTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ParseTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.FetchCredentialStatusImpl
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.FetchStatusFromTokenStatusListImpl
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.ParseTokenStatusListImpl
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.UpdateAllCredentialStatusesImpl
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.UpdateCredentialStatusImpl
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation.ValidateTokenStatusListImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CredentialStatusBindingModule {
    @Binds
    fun bindFetchCredentialStatus(
        useCase: FetchCredentialStatusImpl
    ): FetchCredentialStatus

    @Binds
    fun bindFetchStatusFromTokenStatusList(
        useCase: FetchStatusFromTokenStatusListImpl
    ): FetchStatusFromTokenStatusList

    @Binds
    fun bindParseTokenStatusList(
        useCase: ParseTokenStatusListImpl
    ): ParseTokenStatusList

    @Binds
    fun bindUpdateAllCredentialStatuses(
        useCase: UpdateAllCredentialStatusesImpl
    ): UpdateAllCredentialStatuses

    @Binds
    fun bindUpdateCredentialStatus(
        useCase: UpdateCredentialStatusImpl
    ): UpdateCredentialStatus

    @Binds
    fun bindValidateTokenStatusList(
        useCase: ValidateTokenStatusListImpl
    ): ValidateTokenStatusList

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialStatusRepository(
        repository: CredentialStatusRepositoryImpl
    ): CredentialStatusRepository
}
