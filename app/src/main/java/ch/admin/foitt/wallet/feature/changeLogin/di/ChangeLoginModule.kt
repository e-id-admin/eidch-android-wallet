package ch.admin.foitt.wallet.feature.changeLogin.di

import ch.admin.foitt.wallet.feature.changeLogin.data.repository.CurrentPassphraseAttemptsRepositoryImpl
import ch.admin.foitt.wallet.feature.changeLogin.data.repository.NewPassphraseConfirmationAttemptsRepositoryImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.CurrentPassphraseAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.NewPassphraseConfirmationAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.DeleteCurrentPassphraseAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.DeleteNewPassphraseConfirmationAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.GetCurrentPassphraseAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.GetNewPassphraseConfirmationAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.IncreaseFailedCurrentPassphraseAttemptsCounter
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.IncreaseFailedNewPassphraseConfirmationAttemptsCounter
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.ChangePassphraseImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.DeleteCurrentPassphraseAttemptsImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.DeleteNewPassphraseConfirmationAttemptsImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.GetCurrentPassphraseAttemptsImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.GetNewPassphraseConfirmationAttemptsImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.IncreaseFailedCurrentPassphraseAttemptsCounterImpl
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.IncreaseFailedNewPassphraseConfirmationAttemptsCounterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface ChangeLoginModule {
    @Binds
    fun bindChangePassphrase(useCase: ChangePassphraseImpl): ChangePassphrase

    @Binds
    fun bindCurrentPassphraseAttemptsRepository(
        repo: CurrentPassphraseAttemptsRepositoryImpl
    ): CurrentPassphraseAttemptsRepository

    @Binds
    fun bindDeleteCurrentPassphraseAttempts(
        useCase: DeleteCurrentPassphraseAttemptsImpl
    ): DeleteCurrentPassphraseAttempts

    @Binds
    fun bindGetCurrentPassphraseAttempts(
        useCase: GetCurrentPassphraseAttemptsImpl
    ): GetCurrentPassphraseAttempts

    @Binds
    fun bindIncreaseFailedCurrentPassphraseAttemptsCounter(
        useCase: IncreaseFailedCurrentPassphraseAttemptsCounterImpl
    ): IncreaseFailedCurrentPassphraseAttemptsCounter

    @Binds
    fun bindNewPassphraseConfirmationAttemptsRepository(
        repo: NewPassphraseConfirmationAttemptsRepositoryImpl
    ): NewPassphraseConfirmationAttemptsRepository

    @Binds
    fun bindDeleteNewPassphraseConfirmationAttempts(
        useCase: DeleteNewPassphraseConfirmationAttemptsImpl
    ): DeleteNewPassphraseConfirmationAttempts

    @Binds
    fun bindGetNewPassphraseConfirmationAttempts(
        useCase: GetNewPassphraseConfirmationAttemptsImpl
    ): GetNewPassphraseConfirmationAttempts

    @Binds
    fun bindIncreaseFailedNewPassphraseAttemptsCounter(
        useCase: IncreaseFailedNewPassphraseConfirmationAttemptsCounterImpl
    ): IncreaseFailedNewPassphraseConfirmationAttemptsCounter
}
