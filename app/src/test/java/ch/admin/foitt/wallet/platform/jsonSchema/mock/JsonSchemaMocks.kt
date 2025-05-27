package ch.admin.foitt.wallet.platform.jsonSchema.mock

object JsonSchemaMocks {

    val credentialContentValid = """
        {
          "vct":"https://credentials.example.com/identity_credential",
          "iss":"https://example.com/issuer",
          "kid":"keyId",
          "iat":1683000000,
          "exp":1883000000,
          "sub":"6c5c0a49-b589-431d-bae7-219122a9ec2c",
          "address":{
            "country":"DE"
          },
          "cnf":{
            "jwk":{
              "kty":"EC",
              "crv":"P-256",
              "x":"TCAER19Zvu3OHF4j4W4vfSVoHIP1ILilDls7vCeGemc",
              "y":"ZxjiWWbZMQGHVWKVQ4hbSIirsVfuecCE6t4jT9F2HZQ"
            }
          }
        }
    """.trimIndent()

    val credentialMissingRequiredClaim = """
        {
          "vct":"https://credentials.example.com/identity_credential",
          "iss":"https://example.com/issuer"
        }
    """.trimIndent()

    // invalid schema reference
    val jsonSchemaInvalidSchemaReference = """
        {
          "${'$'}schema":"https://example.org/invalid/reference",
          "type": "object",
          "properties": {
            "name": { "type": "string" }
          }
        }
    """.trimIndent()

    // 'type' has to be a string
    val jsonSchemaKeywordMisuse = """
        {
          "${'$'}schema":"https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "properties": {
            "name": { "type": 123 }
          }
        }
    """.trimIndent()

    // 'required' must be an array
    val jsonSchemaKeywordMisuse2 = """
        {
          "${'$'}schema":"https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "properties": {
            "name": { "type": "string" }
          },
          "required": "name"
        }
    """.trimIndent()

    // iss.type required
    val invalidVcSdJwtJsonSchema = """
        {
          "${'$'}schema":"https://json-schema.org/draft/2020-12/schema",
          "properties": {
            "vct": { 
                "type": "string" 
            },
            "iss": { }
          }
        }
    """.trimIndent()

    val minimalValidVcSdJwtJsonSchema = """
        {
          "${'$'}schema":"https://json-schema.org/draft/2020-12/schema",
          "type":"object",
          "properties":{
            "vct":{
              "type":"string"
            },
            "iss":{
              "type":"string"
            },
            "nbf":{
              "type":"number"
            },
            "exp":{
              "type":"number"
            },
            "cnf":{
              "type":"object"
            },
            "status":{
              "type":"object"
            },
            "sub":{
              "type":"string" 
            },
            "iat":{
              "type":"number" 
            }
          },
          "required":[
            "iss",
            "vct"
          ]
        }
    """.trimIndent()

    val extendedValidVcSdJwtJsonSchema = """
        {
          "${'$'}schema":"https://json-schema.org/draft/2020-12/schema",
          "type":"object",
          "properties":{
            "vct":{
              "type":"string"
            },
            "iss":{
              "type":"string"
            },
            "nbf":{
              "type":"number"
            },
            "exp":{
              "type":"number"
            },
            "cnf":{
              "type":"object"
            },
            "status":{
              "type":"object"
            },
            "sub":{
              "type":"string" 
            },
            "iat":{
              "type":"number" 
            },
            "given_name":{
              "type":"string"
            },
            "family_name":{
              "type":"string"
            },
            "email":{
              "type":"string"
            },
            "phone_number":{
              "type":"string"
            },
            "address":{
              "type":"object",
              "properties":{
                "street_address":{
                  "type":"string"
                },
                "locality":{
                  "type":"string"
                },
                "region":{
                  "type":"string"
                },
                "country":{
                  "type":"string"
                }
              }
            },
            "birthdate":{
              "type":"string"
            },
            "is_over_18":{
              "type":"boolean"
            },
            "is_over_21":{
              "type":"boolean"
            },
            "is_over_65":{
              "type":"boolean"
            }
          },
          "required":[
            "iss",
            "vct",
            "kid"
          ]
        }
    """.trimIndent()
}
