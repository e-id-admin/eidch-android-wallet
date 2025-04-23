package ch.admin.foitt.jsonSchema.domain.implementation

import ch.admin.foitt.jsonSchema.domain.JsonSchema
import ch.admin.foitt.jsonSchema.domain.model.JsonSchemaError
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import javax.inject.Inject

class JsonSchemaImpl @Inject constructor() : JsonSchema {
    override fun validate(jsonObject: ByteArray, jsonSchema: ByteArray): Result<Unit, JsonSchemaError> {
        return Ok(Unit)
    }
}
