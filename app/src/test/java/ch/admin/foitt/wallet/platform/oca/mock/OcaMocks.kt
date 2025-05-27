package ch.admin.foitt.wallet.platform.oca.mock

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.BrandingOverlay1x1
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.ClusterOrderingOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.MetaOverlay1x0

object OcaMocks {
    val ocaResponse = """
        {
            "oca": "displayData"
        }
    """.trimIndent()

    val elfaCaptureBase = """
    {
        "type": "spec/capture_base/1.0",
        "digest": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
        "attributes": {
            "lastName": "Text",
            "firstName": "Text",
            "dateOfBirth": "DateTime",
            "hometown": "Text",
            "dateOfExpiration": "DateTime",
            "issuerEntity": "Text",
            "issuerEntityDate": "DateTime",
            "signatureImage": "Text",
            "photoImage": "Text",
            "policeQRCode": "Text",
            "categoryCode": "Text",
            "categoryIcon": "Text",
            "categoryRestrictions": "Text",
            "restrictionsA": "Text",
            "restrictionsB": "Text",
            "faberPin": "Numeric",
            "licenceNumber": "Numeric"
        }
    }
    """.trimIndent()

    val elfaExample = """
    {
        "capture_bases": [
            $elfaCaptureBase
        ],
        "overlays": [
            {
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "type": "spec/overlays/character_encoding/1.0",
                "default_character_encoding": "utf-8"
            },
            {
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "type": "spec/overlays/format/1.0",
                "attribute_formats": {
                    "dateOfBirth": "YYYY-MM-DD",
                    "dateOfExpiration": "YYYY-MM-DD",
                    "issuerEntityDate": "YYYY-MM-DD"
                }
            },
            {
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "type": "spec/overlays/standard/1.0",
                "attr_standards": {
                    "date": "urn:iso:std:iso:8601",
                    "dateOfExpiration": "urn:iso:std:iso:8601",
                    "issuerEntityDate": "urn:iso:std:iso:8601",
                    "signatureImage": "urn:ietf:rfc:2397",
                    "photoImage": "urn:ietf:rfc:2397",
                    "policeQRCode": "urn:ietf:rfc:2397",
                    "categoryIcon": "urn:ietf:rfc:2397"
                }
            },
            {
                "type": "extend/overlays/data_source/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "format": "vc+sd-jwt",
                "attribute_sources": {
                    "lastName": "${'$'}.lastName",
                    "firstName": "${'$'}.firstName",
                    "dateOfBirth": "${'$'}.dateOfBirth",
                    "hometown": "${'$'}.hometown",
                    "dateOfExpiration": "${'$'}.dateOfExpiration",
                    "issuerEntity": "${'$'}.issuerEntity",
                    "issuerEntityDate": "${'$'}.issuerEntityDate",
                    "signatureImage": "${'$'}.signatureImage",
                    "photoImage": "${'$'}.photoImage",
                    "policeQRCode": "${'$'}.policeQRCode",
                    "categoryCode": "${'$'}.categoryCode",
                    "categoryIcon": "${'$'}.categoryIcon",
                    "categoryRestrictions": "${'$'}.categoryRestrictions",
                    "restrictionsA": "${'$'}.restrictionsA",
                    "restrictionsB": "${'$'}.restrictionsB",
                    "faberPin": "${'$'}.faberPin",
                    "licenceNumber": "${'$'}.licenceNumber"
                }
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "de",
                "name": "REF: Lernfahrausweis",
                "description": "Elektronischer Lernfahrausweis"
            },
            {
                "type": "spec/overlays/meta/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "en",
                "name": "REF: Learner-driver permit",
                "description": "Electronic learner-driver permit"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "de",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AFF",
                "primary_field": "Kategorie {{categoryCode}}"
            },
            {
                "type": "aries/overlays/branding/1.1",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "en",
                "logo": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg==",
                "primary_background_color": "#007AEF",
                "primary_field": "Category {{categoryCode}}"
            },
            {
                "type": "spec/overlays/label/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "de",
                "attribute_labels": {
                    "lastName": "Name",
                    "firstName": "Vorname",
                    "dateOfBirth": "Geburtsdatum",
                    "hometown": "Heimatort",
                    "dateOfExpiration": "Ablaufdatum",
                    "issuerEntity": "Ausstellende Behörde",
                    "issuerEntityDate": "Ausstelldatum",
                    "signatureImage": "Unterschrift",
                    "photoImage": "Foto",
                    "policeQRCode": "Polizei QR Code",
                    "categoryCode": "Kategorie",
                    "categoryIcon": "Kategorie Icon",
                    "categoryRestrictions": "Zusatzangaben",
                    "restrictionsA": "Zusatzangaben (A)",
                    "restrictionsB": "Zusatzangaben (B)",
                    "faberPin": "FABER-PIN",
                    "licenceNumber": "Nummer des Lernfahrausweises"
                }
            },
            {
                "type": "spec/overlays/label/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "en",
                "attribute_labels": {
                    "lastName": "Family name",
                    "firstName": "First name",
                    "dateOfBirth": "Date of birth",
                    "hometown": "Place of origin",
                    "dateOfExpiration": "Expiry date",
                    "issuerEntity": "Issuing authority",
                    "issuerEntityDate": "Date of issue",
                    "signatureImage": "Signature",
                    "photoImage": "Photo",
                    "policeQRCode": "Police QR Code",
                    "categoryCode": "Category",
                    "categoryIcon": "Category icon",
                    "categoryRestrictions": "Additional information",
                    "restrictionsA": "Additional information (A)",
                    "restrictionsB": "Additional information (B)",
                    "faberPin": "FABER PIN",
                    "licenceNumber": "Learner-driver permit number"
                }
            },
            {
                "type": "extend/overlays/cluster_ordering/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "cluster_order": {
                    "main": 1
                },
                "cluster_labels": {
                    "main": "Inhalt"
                },
                "language": "de",
                "attribute_cluster_order": {
                    "main": {
                        "lastName": 3,
                        "firstName": 4,
                        "dateOfBirth": 5,
                        "hometown": 6,
                        "dateOfExpiration": 8,
                        "issuerEntity": 9,
                        "issuerEntityDate": 7,
                        "signatureImage": 17,
                        "policeQRCode": 1,
                        "photoImage": 2,
                        "categoryCode": 10,
                        "categoryIcon": 11,
                        "categoryRestrictions": 12,
                        "restrictionsA": 13,
                        "restrictionsB": 14,
                        "faberPin": 16,
                        "licenceNumber": 15
                    }
                }
            },
            {
                "type": "extend/overlays/cluster_ordering/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "cluster_order": {
                    "main": 1
                },
                "cluster_labels": {
                    "main": "Details"
                },
                "language": "en",
                "attribute_cluster_order": {
                    "main": {
                        "lastName": 3,
                        "firstName": 4,
                        "dateOfBirth": 5,
                        "hometown": 6,
                        "dateOfExpiration": 8,
                        "issuerEntity": 9,
                        "issuerEntityDate": 7,
                        "signatureImage": 17,
                        "policeQRCode": 1,
                        "photoImage": 2,
                        "categoryCode": 10,
                        "categoryIcon": 11,
                        "categoryRestrictions": 12,
                        "restrictionsA": 13,
                        "restrictionsB": 14,
                        "faberPin": 16,
                        "licenceNumber": 15
                    }
                }
            },
            {
                "type": "spec/overlays/entry/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "de",
                "attribute_entries": {
                    "categoryCode": {
                        "A": "Motorräder mit einer Motorleistung von mehr als 35 kW",
                        "B": "Motorwagen und dreirädrige Motorfahrzeuge mit einem Gesamtgewicht von nicht mehr als 3500 kg"
                    }
                }
            },
            {
                "type": "spec/overlays/entry/1.0",
                "capture_base": "IDOF_3koVquS-oVf5GQSDFNCEpWcKVQ5ylKLy40Ky49W",
                "language": "en",
                "attribute_entries": {
                    "categoryCode": {
                        "A": "Motorcycles with an engine power of more than 35 kW",
                        "B": "Motor vehicles and three-wheeled motor vehicles with a maximum weight not exceeding 3500 kg"
                    }
                }
            }
        ]
    }
    """.trimIndent()

    const val DIGEST = "digest"
    const val LANGUAGE_EN = "en"
    const val LANGUAGE_DE = "de"
    const val ATTRIBUTE_KEY_FIRSTNAME = "firstname"
    const val ATTRIBUTE_LABEL_FIRSTNAME_EN = "Firstname"
    const val ATTRIBUTE_LABEL_FIRSTNAME_DE = "Vorname"
    const val ATTRIBUTE_KEY_AGE = "age"
    const val ATTRIBUTE_LABEL_AGE_EN = "Age"
    const val ATTRIBUTE_LABEL_AGE_DE = "Alter"
    const val FORMAT = "vc+sd-jwt"
    const val JSON_PATH_FIRSTNAME = "$.firstname"
    const val JSON_PATH_AGE = "$.age"

    private const val VALID_DIGEST_HUMAN = "IDif6Jd863C_YYjp1cHFCTAUr1_TzZSS1l-pv21Q56qs"
    private const val ATTRIBUTE_KEY_LASTNAME = "lastname"
    const val ATTRIBUTE_KEY_ADDRESS_STREET = "address_street"
    private const val ATTRIBUTE_KEY_ADDRESS_CITY = "address_city"
    private const val ATTRIBUTE_KEY_ADDRESS_COUNTRY = "address_country"
    private const val ATTRIBUTE_KEY_PETS = "pets"
    private const val VALID_DIGEST_PET = "IKLvtGx1NU0007DUTTmI_6Zw-hnGRFicZ5R4vAxg4j2j"
    const val ATTRIBUTE_KEY_NAME = "name"
    private const val ATTRIBUTE_KEY_RACE = "race"
    private const val JSON_PATH_LASTNAME = "$.lastname"
    private const val JSON_PATH_ADDRESS_STREET = "$.address.street"
    private const val JSON_PATH_ADDRESS_CITY = "$.address.city"
    private const val JSON_PATH_ADDRESS_COUNTRY = "$.address.country"
    private const val JSON_PATH_PETS = "$.pets"
    private const val JSON_PATH_NANE = "$.pets[*].name"
    private const val JSON_PATH_RACE = "$.pets[*].race"
    private const val LOGO = "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAaCAYAAACpSkzOAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAGbSURBVHgBvVaBUcMwDFQ4BjAbmA06QjYgGzQbkA1aJmg3yHWCwgSGCVImcJkg3UDYjVMUI6VxW/g7X+701kuWZTsAI0DEwo0GO9RuKMKpYGvdsH4uXALnqPE3DOFrhs8hFc5pizxyN2YCZyS9+5FYuWCfgYzZJYFUon2UuwMZ+xG7xO3gXKBQ95xwGyHIuxuvbhwY/oPo+WbSQAyKtDCGVtWM3aMifmXENcGnb3uqp6Q2NZHgEpnWDQl5rsJwxgS9NTBZ98ghEdgdcA6t3yOpJQtGSIUVekHN+DwJWsfSWSGLmsm2xWHti2iOEbQav6I3IYtPIqDdZwvDc3K0OY5W5EvQ2vUb2jJZaBLIogxL5pUcf9LC7gzZQPigJXFe4HmsyPw1sRvk9jKsjj4Fc5yOOfFTyDcLcEGfMR0VpACnlUvC4j+C9FjFulkURLuPhdvgIcuy08UbPxMabofBjRMHOsAfIS6db21fOgXXYe/K9kgNgxWFmr7A9dhMmoXD001h8OcvSPpLWkIKsLu3THC2yBxG7B48S5IoJb1vHubbPPxs2qsAAAAASUVORK5CYII="

    val ocaSimple = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = DIGEST,
                attributes = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_AGE to AttributeType.Numeric,
                )
            ),
        ),
        overlays = listOf(
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_EN,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_EN,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = DIGEST,
                language = LANGUAGE_DE,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to ATTRIBUTE_LABEL_FIRSTNAME_DE,
                    ATTRIBUTE_KEY_AGE to ATTRIBUTE_LABEL_AGE_DE,
                )
            ),
            DataSourceOverlay1x0(
                captureBaseDigest = DIGEST,
                format = FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to JSON_PATH_FIRSTNAME,
                    ATTRIBUTE_KEY_AGE to JSON_PATH_AGE,
                )
            )
        )
    )

    val ocaNested = OcaBundle(
        captureBases = listOf(
            CaptureBase1x0(
                digest = VALID_DIGEST_HUMAN,
                attributes = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_LASTNAME to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_STREET to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_CITY to AttributeType.Text,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to AttributeType.Text,
                    ATTRIBUTE_KEY_PETS to AttributeType.Array(AttributeType.Reference(VALID_DIGEST_PET)),
                ),
            ),
            CaptureBase1x0(
                digest = VALID_DIGEST_PET,
                attributes = mapOf(
                    ATTRIBUTE_KEY_NAME to AttributeType.Text,
                    ATTRIBUTE_KEY_RACE to AttributeType.Text,
                )
            )
        ),
        overlays = listOf(
            DataSourceOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                format = FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to JSON_PATH_FIRSTNAME,
                    ATTRIBUTE_KEY_LASTNAME to JSON_PATH_LASTNAME,
                    ATTRIBUTE_KEY_ADDRESS_STREET to JSON_PATH_ADDRESS_STREET,
                    ATTRIBUTE_KEY_ADDRESS_CITY to JSON_PATH_ADDRESS_CITY,
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to JSON_PATH_ADDRESS_COUNTRY,
                    ATTRIBUTE_KEY_PETS to JSON_PATH_PETS,
                )
            ),
            DataSourceOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                format = FORMAT,
                attributeSources = mapOf(
                    ATTRIBUTE_KEY_NAME to JSON_PATH_NANE,
                    ATTRIBUTE_KEY_RACE to JSON_PATH_RACE,
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_FIRSTNAME to "Firstname",
                    ATTRIBUTE_KEY_LASTNAME to "Lastname",
                    ATTRIBUTE_KEY_ADDRESS_STREET to "street name",
                    ATTRIBUTE_KEY_ADDRESS_CITY to "city name",
                    ATTRIBUTE_KEY_ADDRESS_COUNTRY to "country name",
                    ATTRIBUTE_KEY_PETS to "pets"
                )
            ),
            LabelOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                language = LANGUAGE_EN,
                attributeLabels = mapOf(
                    ATTRIBUTE_KEY_NAME to "name",
                    ATTRIBUTE_KEY_RACE to "race",
                )
            ),
            BrandingOverlay1x1(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                logo = LOGO,
                primaryBackgroundColor = "#2C75E3",
                primaryField = "{{firstname}} {{lastname}} from {{address_country}}",
            ),
            MetaOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                name = "Pet permit",
            ),
            ClusterOrderingOverlay1x0(
                captureBaseDigest = VALID_DIGEST_HUMAN,
                language = LANGUAGE_EN,
                clusterOrder = mapOf(
                    "pets" to 1,
                    "owner" to 2,
                ),
                clusterLabels = mapOf(
                    "pets" to "Pets",
                    "owner" to "Owner"
                ),
                attributeClusterOrder = mapOf(
                    "pets" to mapOf(
                        "pets" to 1
                    ),
                    "owner" to mapOf(
                        ATTRIBUTE_KEY_FIRSTNAME to 1,
                        ATTRIBUTE_KEY_LASTNAME to 2,
                        ATTRIBUTE_KEY_ADDRESS_STREET to 3,
                        ATTRIBUTE_KEY_ADDRESS_CITY to 4,
                        ATTRIBUTE_KEY_ADDRESS_COUNTRY to 5,
                    )
                )
            ),
            ClusterOrderingOverlay1x0(
                captureBaseDigest = VALID_DIGEST_PET,
                language = LANGUAGE_EN,
                clusterOrder = mapOf(
                    "default" to 1,
                ),
                clusterLabels = emptyMap(),
                attributeClusterOrder = mapOf(
                    "default" to mapOf(
                        ATTRIBUTE_KEY_RACE to 1,
                        ATTRIBUTE_KEY_NAME to 2,
                    )
                )
            )
        )
    )
}
