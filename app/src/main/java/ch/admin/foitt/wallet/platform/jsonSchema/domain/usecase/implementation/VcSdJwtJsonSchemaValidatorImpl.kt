package ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.networknt.schema.InputFormat
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaId
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import timber.log.Timber
import javax.inject.Inject

internal class VcSdJwtJsonSchemaValidatorImpl @Inject constructor() : JsonSchemaValidator {
    override suspend fun invoke(data: String, jsonSchema: String): Result<Unit, JsonSchemaError> = coroutineBinding {
        val jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
        val config = SchemaValidatorsConfig.builder().failFast(true).build()

        // ensure jsonSchema is
        // 1. a valid JsonSchema Draft 2012-12 schema
        // 2. can validate VcSdJwts
        validateJsonSchema(jsonSchema, jsonSchemaFactory, config).bind()

        // validate the data against the jsonSchema
        val schema = runSuspendCatching {
            jsonSchemaFactory.getSchema(jsonSchema, config)
        }.mapError { throwable ->
            Timber.e(throwable, "invalid json schema")
            JsonSchemaError.ValidationFailed
        }.bind()
        val schemaAssertions = schema.validate(data, InputFormat.JSON)
        if (schemaAssertions.isNotEmpty()) {
            Err(JsonSchemaError.ValidationFailed).bind<JsonSchemaError>()
        }
    }

    private suspend fun validateJsonSchema(
        jsonSchema: String,
        jsonSchemaFactory: JsonSchemaFactory,
        config: SchemaValidatorsConfig
    ): Result<Unit, JsonSchemaError> = coroutineBinding {
        // validate that the jsonSchema conforms to Json Schema Draft 2010-12
        val metaSchema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V202012), config)
        val metaSchemaAssertions = metaSchema.validate(jsonSchema, InputFormat.JSON)

        if (metaSchemaAssertions.isNotEmpty()) {
            Err(JsonSchemaError.ValidationFailed).bind<JsonSchemaError>()
        }

        // validate the jsonSchema can validate a VcSdJwt schema
        val vcSdJwtMetaSchema = jsonSchemaFactory.getSchema(vcSdJwtMetaSchemaString, config)
        val vcSdJwtMetaSchemaAssertions = vcSdJwtMetaSchema.validate(jsonSchema, InputFormat.JSON)
        if (vcSdJwtMetaSchemaAssertions.isNotEmpty()) {
            Err(JsonSchemaError.ValidationFailed).bind<JsonSchemaError>()
        }
    }

    companion object {
        private val vcSdJwtMetaSchemaString = """
            {
              "properties": {
                "additionalItems": true,
                "type": "object",
                "required": ["properties", "required"],
                "required": {
                  "type": "array",
                  "allOf": [
                    { "contains": { "const": "vct" } },
                    { "contains": { "const": "iss" } }
                  ]
                },
                "properties": {
                  "type": "object",
                  "required": [ "iss", "vct", "nbf", "exp", "cnf", "status", "sub", "iat" ],
                  "properties": {
                    "vct": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "string" }
                      }
                    },
                    "iss": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "string" }
                      }
                    },
                    "nbf": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "number" }
                      }
                    },
                    "exp": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "number" }
                      }
                    },
                    "cnf": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "object" }
                      }
                    },
                    "status": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "object" }
                      }
                    },
                    "sub": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "string" }
                      }
                    },
                    "iat": {
                      "type": "object",
                      "required": ["type"],
                      "properties": {
                        "type": { "const": "number" }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()
    }
}
