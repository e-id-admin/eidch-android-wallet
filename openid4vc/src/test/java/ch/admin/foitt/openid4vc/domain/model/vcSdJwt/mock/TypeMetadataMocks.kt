package ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock

object TypeMetadataMocks {
    const val VCT_INTEGRITY = "sha256-abcdef"
    const val TYPE_METADATA_INVALID = """{}"""
    val typeMetadataVctUrl = createTypeMetadata("https://credentials.example.com/identity_credential")
    val typeMetadataVctOther = createTypeMetadata("somethingOtherThanInTheCredential")

    private fun createTypeMetadata(vct: String) = """
        {
          "vct": "$vct",
          "name": "Betelgeuse Education Credential - Preliminary Version",
          "description": "This is our development version of the education credential. Don't panic.",
          "extends": "https://galaxy.example.com/galactic-education-credential-0.9",
          "extends#integrity": "sha256-9cLlJNXN-TsMk-PmKjZ5t0WRL5ca_xGgX3c1VLmXfh-WRL5",
          "display": [
            {
              "lang": "en-US",
              "name": "Betelgeuse Education Credential",
              "description": "An education credential for all carbon-based life forms on Betelgeusians",
              "rendering": {
                "simple": {
                  "logo": {
                    "uri": "https://betelgeuse.example.com/public/education-logo.png",
                    "uri#integrity": "sha256-LmXfh-9cLlJNXN-TsMk-PmKjZ5t0WRL5ca_xGgX3c1V",
                    "alt_text": "Betelgeuse Ministry of Education logo"
                  },
                  "background_color": "#12107c",
                  "text_color": "#FFFFFF"
                },
                "svg_templates": [
                  {
                    "uri": "https://betelgeuse.example.com/public/credential-english.svg",
                    "uri#integrity": "sha256-8cLlJNXN-TsMk-PmKjZ5t0WRL5ca_xGgX3c1VLmXfh-9c",
                    "properties": {
                      "orientation": "landscape",
                      "color_scheme": "light",
                      "contrast": "high"
                    }
                  },
                  {
                    "uri": "https://betelgeuse.example.com/public/credential-english.svg",
                    "uri#integrity": "sha256-8cLlJNXN-TsMk-PmKjZ5t0WRL5ca_xGgX3c1VLmXfh-9c",
                    "properties": {
                      "orientation": "landscape",
                      "color_scheme": "light",
                      "contrast": "high"
                    }
                  }
                ]
              }
            },
            {
              "lang": "de-DE",
              "name": "Betelgeuse-Bildungsnachweis",
              "rendering": {
                "simple": {
                  "logo": {
                    "uri": "https://betelgeuse.example.com/public/education-logo-de.png",
                    "uri#integrity": "sha256-LmXfh-9cLlJNXN-TsMk-PmKjZ5t0WRL5ca_xGgX3c1V",
                    "alt_text": "Logo des Betelgeusischen Bildungsministeriums"
                  },
                  "background_color": "#12107c",
                  "text_color": "#FFFFFF"
                },
                "svg_templates": [
                  {
                    "uri": "https://betelgeuse.example.com/public/credential-german.svg",
                    "uri#integrity": "sha256-8cLlJNXN-TsMk-PmKjZ5t0WRL5ca_xGgX3c1VLmXfh-9c",
                    "properties": {
                      "orientation": "landscape",
                      "color_scheme": "light",
                      "contrast": "high"
                    }
                  }
                ]
              }
            }
          ],
          "claims": [
            {
              "path": ["name"],
              "display": [
                {
                  "lang": "de-DE",
                  "label": "Vor- und Nachname",
                  "description": "Der Name des Studenten"
                },
                {
                  "lang": "en-US",
                  "label": "Name",
                  "description": "The name of the student"
                }
              ],
              "sd": "allowed"
            },
            {
              "path": ["address"],
              "display": [
                {
                  "lang": "de-DE",
                  "label": "Adresse",
                  "description": "Adresse zum Zeitpunkt des Abschlusses"
                },
                {
                  "lang": "en-US",
                  "label": "Address",
                  "description": "Address at the time of graduation"
                }
              ],
              "sd": "always"
            },
            {
              "path": ["address", "street_address"],
              "display": [
                {
                  "lang": "de-DE",
                  "label": "Straße"
                },
                {
                  "lang": "en-US",
                  "label": "Street Address"
                }
              ],
              "sd": "always",
              "svg_id": "address_street_address"
            },
            {
              "path": ["degrees", null],
              "display": [
                {
                  "lang": "de-DE",
                  "label": "Abschluss",
                  "description": "Der Abschluss des Studenten"
                },
                {
                  "lang": "en-US",
                  "label": "Degree",
                  "description": "Degree earned by the student"
                }
              ],
              "sd": "allowed"
            }
          ],
          "schema_url": "https://exampleuniversity.com/public/credential-schema-0.9",
          "schema_url#integrity": "sha256-o984vn819a48ui1llkwPmKjZ5t0WRL5ca_xGgX3c1VLmXfh"
        }
    """.trimIndent()
}
