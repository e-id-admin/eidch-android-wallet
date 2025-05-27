package ch.admin.foitt.wallet.platform.oca.mock

object OcaCaptureBaseMocks {
    val validInput01 = """
        {
            "type":"spec/capture_base/1.0",
            "digest":"IBYzBHEN4moeVO_aQtW_DbDoQd-30BgeJQMyfsRzoUFI",
            "attributes":{
                "name":"Text"
            }
        }
    """.trimIndent()

    val validInput02 = """
        {
            "type": "spec/capture_base/1.0",
            "digest": "IEaoDFlT--ZaM8F_Y8sJosPaPwEBu06BZWAjZu1mVE2o",
            "attributes": {
                "firstname": "Text",
                "lastname": "Text",
                "address_street": "Text",
                "address_city": "Text",
                "address_country": "Text"
            }
        }
    """.trimIndent()

    val validInput03 = """
        {
            "attributes":{
                "name":"Text",
                "race":"Text"
            },
            "type":"spec/capture_base/1.0",
            "digest":"IKLvtGx1NU0007DUTTmI_6Zw-hnGRFicZ5R4vAxg4j2j"
        }
    """.trimIndent()

    val validInput04 = """
        {
            "type":"spec/capture_base/1.0",
            "digest":"IFM8RfatBApjAWtUuoPdHBw7u-poW49aGCMSgoK1pwu5",
            "attributes":{
                "picture":"Text"
            }
        }
    """.trimIndent()

    val validInput02alt = """
        {"attributes":{"address_city":"Text","address_country":"Text","address_street":"Text","firstname":"Text","lastname":"Text"},"digest":"IEaoDFlT--ZaM8F_Y8sJosPaPwEBu06BZWAjZu1mVE2o","type":"spec/capture_base/1.0"}
    """.trimIndent()

    val validInput05 = """
        {
            "type": "spec/capture_base/1.0",
            "digest": "IH9w8JN_ZE4maSfcs27R33JdV_ClH7jilM9mnlS9j_0j",
            "attributes": {
                "firstname": "Text",
                "lastname": "Text",
                "address_street": "Text",
                "address_city": "Text",
                "address_country": "Text",
                "pets": "Array[refs:IKLvtGx1NU0007DUTTmI_6Zw-hnGRFicZ5R4vAxg4j2j]"
            }
        }
    """.trimIndent()

    val wrongAlgorithm = """
        {
            "type": "spec/capture_base/1.0",
            "digest": "HEaoDFlT--ZaM8F_Y8sJosPaPwEBu06BZWAjZu1mVE2o",
            "attributes": {
                "firstname": "Text",
                "lastname": "Text",
                "address_street": "Text",
                "address_city": "Text",
                "address_country": "Text"
            }
        }
    """.trimIndent()

    val wrongDigest = """
        {
            "type": "spec/capture_base/1.0",
            "digest": "ThisIsInvalid",
            "attributes": {
                "firstname": "Text"
            }
        }
    """.trimIndent()

    val noDigest = """
        {
            "type": "spec/capture_base/1.0",
            "attributes": {
                "firstname": "Text",
                "lastname": "Text"
            }
        }
    """.trimIndent()

    val emptyDigest = """
        {
            "type": "spec/capture_base/1.0",
            "digest": "",
            "attributes": {
                "firstname": "Text"
            }
        }
    """.trimIndent()

    val wrongJson = """
        {
            "type":"spec/capture_base/1.0",
            "digest":"IBYzBHEN4moeVO_aQtW_DbDoQd-30BgeJQMyfsRzoUFI",
            "attributes":{
                "name":"Text",
            }
    """.trimIndent()
}
