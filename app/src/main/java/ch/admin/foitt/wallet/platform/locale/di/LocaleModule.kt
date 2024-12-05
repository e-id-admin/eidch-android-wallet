package ch.admin.foitt.wallet.platform.locale.di

import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetSupportedAppLanguages
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetCurrentAppLocaleImpl
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetLocalizedDisplayImpl
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetSupportedAppLanguagesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface LocaleModule {
    @Binds
    fun bindGetCurrentAppLocale(
        useCase: GetCurrentAppLocaleImpl
    ): GetCurrentAppLocale

    @Binds
    fun bindGetSupportedLanguages(
        useCase: GetSupportedAppLanguagesImpl
    ): GetSupportedAppLanguages

    @Binds
    fun bindGetLocalizedDisplay(
        useCase: GetLocalizedDisplayImpl
    ): GetLocalizedDisplay
}
