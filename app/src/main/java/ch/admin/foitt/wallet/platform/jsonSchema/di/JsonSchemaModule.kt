package ch.admin.foitt.wallet.platform.jsonSchema.di

import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.implementation.VcSdJwtJsonSchemaValidatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import javax.inject.Named

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface JsonSchemaModule {
    @Binds
    @Named("VcSdJwtJsonSchemaValidator")
    fun bindJsonSchemaValidator(
        validator: VcSdJwtJsonSchemaValidatorImpl
    ): JsonSchemaValidator
}
