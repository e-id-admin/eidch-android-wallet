package ch.admin.foitt.jsonSchema.domain.model

sealed interface JsonSchemaError {
    data object Unexpected : JsonSchemaError
}
