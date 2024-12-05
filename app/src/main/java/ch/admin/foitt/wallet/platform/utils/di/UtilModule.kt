package ch.admin.foitt.wallet.platform.utils.di

import ch.admin.foitt.wallet.platform.utils.BuildConfigProvider
import ch.admin.foitt.wallet.platform.utils.BuildConfigProviderImpl
import ch.admin.foitt.wallet.platform.utils.SafeJson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Named

@Module
@InstallIn(ActivityRetainedComponent::class)
class UtilModule {
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Named("JsonSerializer")
    fun provideJsonSerializer(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }
    }

    @Provides
    fun provideSafeJson(@Named("JsonSerializer") json: Json) = SafeJson(json = json)
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface UtilBindingsModule {
    @Binds
    fun bindBuildConfigProvider(
        provider: BuildConfigProviderImpl
    ): BuildConfigProvider
}