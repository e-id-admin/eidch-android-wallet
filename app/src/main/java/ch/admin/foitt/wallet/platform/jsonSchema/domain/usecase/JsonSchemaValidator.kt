package ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase

import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import com.github.michaelbull.result.Result

/**
 * Providing functionality for working with JSON Schemas
 * https://json-schema.org/draft/2020-12/release-notes
 */
interface JsonSchemaValidator {

    /**
     * Validates the provided JSON object against the given JSON Schema.
     *
     * @param data The JSON object to be validated.
     * @param jsonSchema The raw JSON schema to be used for validation.
     *
     * @return Ok(Unit) if the JSON object is valid according to the schema;
     *          otherwise Err().
     *
     */
    suspend operator fun invoke(data: String, jsonSchema: String): Result<Unit, JsonSchemaError>
}
