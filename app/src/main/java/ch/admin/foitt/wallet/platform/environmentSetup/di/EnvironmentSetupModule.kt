package ch.admin.foitt.wallet.platform.environmentSetup.di

import ch.admin.foitt.wallet.platform.environmentSetup.data.MainEnvironmentSetupRepositoryImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@Module
@InstallIn(ActivityRetainedComponent::class)
object EnvironmentSetupModule {
    /**
     * Defines the default or main environment setup used for production builds.
     * Used by [ch.admin.foitt.wallet.platform.environmentSetup.di.EnvironmentSetupModule] to decide on the preferred environment setup.
     * Must have the lowest IntKey of all implementations to act as a fallback if no other implementation is provided.
     */
    @Provides
    @IntoMap
    @IntKey(Int.MIN_VALUE)
    fun provideMainEnvironmentRepositorySetup(): EnvironmentSetupRepository {
        return MainEnvironmentSetupRepositoryImpl()
    }

    /** Provide prefered environment setup by returning the [EnvironmentSetupRepository] with the highest key (aka priority) value.
     *  The default setup is defined in [ch.admin.foitt.wallet.platform.environmentSetup.di.MainEnvironmentRepositorySetupModule] with a prio of 0.
     *  There might be higher-prio setups defined in flavors, e.g. to use dev environment
     */
    @Provides
    fun provideEnvironmentSetup(
        availableSetups: Map<Int, @JvmSuppressWildcards EnvironmentSetupRepository>
    ): EnvironmentSetupRepository {
        val preferredEnvironmentSetup = availableSetups.maxByOrNull { it.key }
        return checkNotNull(preferredEnvironmentSetup?.value) { "No environment setups were provided" }
    }
}
