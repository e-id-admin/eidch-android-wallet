package ch.admin.foitt.wallet.platform.jsonSchema

import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.implementation.VcSdJwtJsonSchemaValidatorImpl
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.credentialContentValid
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.credentialMissingRequiredClaim
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.extendedValidVcSdJwtJsonSchema
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.invalidVcSdJwtJsonSchema
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.jsonSchemaInvalidSchemaReference
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.jsonSchemaKeywordMisuse
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.jsonSchemaKeywordMisuse2
import ch.admin.foitt.wallet.platform.jsonSchema.mock.JsonSchemaMocks.minimalValidVcSdJwtJsonSchema
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VcSdJwtJsonSchemaValidatorImplTest {

    private lateinit var jsonSchemaValidator: JsonSchemaValidator

    @BeforeEach
    fun setUp() {
        jsonSchemaValidator = VcSdJwtJsonSchemaValidatorImpl()
    }

    @Test
    fun `Invalid Json schema for validating vc sd-jwt returns error`() = runTest {
        jsonSchemaValidator(credentialContentValid, invalidVcSdJwtJsonSchema).assertErr()
    }

    @Test
    fun `Valid and minimal Json schema for validating vc sd-jwt returns success`() = runTest {
        jsonSchemaValidator(credentialContentValid, minimalValidVcSdJwtJsonSchema).assertOk()
    }

    @Test
    fun `Valid and extended Json schema for validating vc sd-jwt returns success`() = runTest {
        jsonSchemaValidator(credentialContentValid, extendedValidVcSdJwtJsonSchema).assertOk()
    }

    @Test
    fun `Json schema validations for invalid json schema (invalid schema reference) returns an error`() = runTest {
        jsonSchemaValidator(
            credentialContentValid,
            jsonSchemaInvalidSchemaReference
        ).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Json schema validations for invalid json schema (type must be a string) returns an error`() = runTest {
        jsonSchemaValidator(credentialContentValid, jsonSchemaKeywordMisuse).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Json schema validations for invalid json schema ('required' must be an array) returns an error`() = runTest {
        jsonSchemaValidator(credentialContentValid, jsonSchemaKeywordMisuse2).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }

    @Test
    fun `Json schema validation for credential missing a required claim returns an error`() = runTest {
        jsonSchemaValidator(
            credentialMissingRequiredClaim,
            extendedValidVcSdJwtJsonSchema
        ).assertErrorType(JsonSchemaError.ValidationFailed::class)
    }
}
