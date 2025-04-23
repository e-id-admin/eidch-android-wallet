package ch.admin.foitt.jsonSchema.domain

import ch.admin.foitt.jsonSchema.domain.model.JsonSchemaError
import com.github.michaelbull.result.Result

/**
 * Providing functionality for working with JSON Schemas
 * https://json-schema.org/draft/2020-12/release-notes
 */
interface JsonSchema {

    /**
     * Validates the provided JSON object against the given JSON Schema.
     *
     * @param jsonObject The JSON object to be validated.
     * @param jsonSchema The raw JSON schema to be used for validation.
     *
     * @return Ok(Unit) if the JSON object is valid according to the schema;
     *          otherwise Err().
     *
     */
    fun validate(jsonObject: ByteArray, jsonSchema: ByteArray): Result<Unit, JsonSchemaError>
}
