package ch.admin.foitt.sriValidator.di

import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.implementation.SRIValidatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface SRIValidatorModule {
    @Binds
    fun bindSRIValidator(
        validator: SRIValidatorImpl
    ): SRIValidator
}
