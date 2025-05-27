package ch.admin.foitt.wallet.platform.jsonSchema.domain.model

sealed interface JsonSchemaError {
    data object ValidationFailed : JsonSchemaError
}
