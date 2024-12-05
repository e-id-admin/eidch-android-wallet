package ch.admin.foitt.wallet.platform.deeplink.di

import ch.admin.foitt.wallet.platform.deeplink.data.DeepLinkIntentRepositoryImpl
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.SetDeepLinkIntent
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation.HandleDeeplinkImpl
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation.SetDeepLinkIntentImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class DeepLinkModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface DeepLinkBindingsModule {
    @Binds
    @ActivityRetainedScoped
    fun bindIntentRepository(
        repo: DeepLinkIntentRepositoryImpl
    ): DeepLinkIntentRepository

    @Binds
    fun bindHandleDeeplink(
        useCase: HandleDeeplinkImpl
    ): HandleDeeplink

    @Binds
    fun bindSetDeepLinkIntent(
        useCase: SetDeepLinkIntentImpl
    ): SetDeepLinkIntent
}
