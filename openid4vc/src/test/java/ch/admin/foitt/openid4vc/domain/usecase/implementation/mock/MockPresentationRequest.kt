package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

object MockPresentationRequest {
    const val validJwt = "eyJraWQiOiJkaWQ6dGR3OjEyMzQ9OmV4YW1wbGUuY29tOmFwaTp2MTpkaWQ6MTIzNDU2I2tleS0wMSIsImFsZyI6IkVTMjU2In0.eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tIiwiY2xpZW50X2lkX3NjaGVtZSI6ImRpZCIsImlzcyI6ImRpZDpleGFtcGxlOjEyMzQ1IiwicmVzcG9uc2VfdHlwZSI6InZwX3Rva2VuIiwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaWQiOiIzZmE4NWY2NC0wMDAwLTAwMDAtYjNmYy0yYzk2M2Y2NmFmYTYiLCJuYW1lIjoic3RyaW5nIiwicHVycG9zZSI6InN0cmluZyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjNmYTg1ZjY0LTU3MTctNDU2Mi1iM2ZjLTJjOTYzZjY2YWZhNiIsIm5hbWUiOiJBIG5hbWUiLCJmb3JtYXQiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il0sImtiLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2Il19fSwiY29uc3RyYWludHMiOnsiZmllbGRzIjpbeyJwYXRoIjpbIiQubGFzdE5hbWUiXX1dfX1dfSwibm9uY2UiOiJJMDJGaWJMRjRrNUVzZkRPMmpnakRvb1A0QS9adWtRMyIsImNsaWVudF9pZCI6ImRpZDpleGFtcGxlOjEyMzQ1IiwiY2xpZW50X21ldGFkYXRhIjp7ImNsaWVudF9uYW1lIjoiUmVmIFRlc3QiLCJsb2dvX3VyaSI6Ind3dy5leGFtcGxlLmljbyJ9LCJyZXNwb25zZV9tb2RlIjoiZGlyZWN0X3Bvc3QifQ.CU3nJLohuGR58_I5XhyO6uimT0GRk19KXUH7CRoJvMr8jf8nLt4UaiXecFzH9lUM3CitptnxgDynnwvJe1oT2g"

    val validJson = """
{
  "client_id" : "did:example:12345",
  "client_id_scheme" : "did",
  "response_type" : "vp_token",
  "response_mode" : "direct_post",
  "response_uri" : "https://example.com",
  "nonce" : "zW0qUvtH3AczW8MTTSebAFrSbQsqSjc5",
  "presentation_definition" : {
    "id" : "3fa85f64-0000-0000-b3fc-2c963f66afa6",
    "name" : "string",
    "purpose" : "string",
    "input_descriptors" : [
      {
        "id" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "name" : "A name",
        "format" : {
          "vc+sd-jwt" : {
            "sd-jwt_alg_values" : [
              "ES256"
            ],
            "kb-jwt_alg_values" : [
              "ES256"
            ]
          }
        },
        "constraints" : {
          "fields" : [
            {
              "path" : [
                "${'$'}.lastName"
              ]
            }
          ]
        }
      }
    ]
  },
  "client_metadata" : {
    "client_name" : "Ref Test",
    "logo_uri" : "www.example.ico"
  }
}
    """.trimIndent()

    const val invalidJwt = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.5EQDWnS78bM4ZPTgZ2HKbAAbTrkTdBH5JPbmfw34Dp1gOjrfiwVHYcYh7trwSreAA8VVQoQ_No6vCr3oRqkzRg"

    const val invalidJson = "{\"sub\": \"1234567890\",\n\"name\": \"John Doe\",\n\"iat\": 1516239022\n\"}"

    //region Presentation request source
    /* header
{
  "kid": "did:tdw:1234=:example.com:api:v1:did:123456#key-01",
  "alg": "ES256"
}
     */
    /* payload
{
    "response_uri": "https://example.com",
    "client_id_scheme": "did",
    "iss": "did:example:12345",
    "response_type": "vp_token",
    "presentation_definition": {
    "id": "3fa85f64-0000-0000-b3fc-2c963f66afa6",
    "name": "string",
    "purpose": "string",
    "input_descriptors": [
    {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "name": "A name",
        "format": {
        "vc+sd-jwt": {
        "sd-jwt_alg_values": [
        "ES256"
        ],
        "kb-jwt_alg_values": [
        "ES256"
        ]
    }
    },
        "constraints": {
        "fields": [
        {
            "path": [
            "$.lastName"
            ]
        }
        ]
    }
    }
    ]
},
    "nonce": "I02FibLF4k5EsfDO2jgjDooP4A/ZukQ3",
    "client_id": "did:example:12345",
    "client_metadata": {
    "client_name": "Ref Test",
    "logo_uri": "www.example.ico"
},
    "response_mode": "direct_post"
}
     */

    /* keys
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9
q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg==
-----END PUBLIC KEY-----

-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgevZzL1gdAFr88hb2
OF/2NxApJCzGCEDdfSp6VQO30hyhRANCAAQRWz+jn65BtOMvdyHKcvjBeBSDZH2r
1RTwjmYSi9R/zpBnuQ4EiMnCqfMPWiZqB4QdbAd0E7oH50VpuZ1P087G
-----END PRIVATE KEY-----
     */

    //endregion
}
