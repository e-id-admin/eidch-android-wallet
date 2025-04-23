package ch.admin.foitt.jsonSchema.di

import ch.admin.foitt.jsonSchema.domain.JsonSchema
import ch.admin.foitt.jsonSchema.domain.implementation.JsonSchemaImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface JsonSchemaModule {
    @Binds
    fun bindJsonSchema(
        validator: JsonSchemaImpl
    ): JsonSchema
}
