package ch.admin.foitt.wallet.platform.oca

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OverlayBundleAttribute
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x0
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_KEY_ADDRESS_STREET
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_KEY_AGE
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_KEY_FIRSTNAME
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_KEY_NAME
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_LABEL_AGE_DE
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_LABEL_AGE_EN
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_LABEL_FIRSTNAME_DE
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ATTRIBUTE_LABEL_FIRSTNAME_EN
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.DIGEST
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.FORMAT
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.JSON_PATH_AGE
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.JSON_PATH_FIRSTNAME
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.LANGUAGE_DE
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.LANGUAGE_EN
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ocaNested
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ocaSimple
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class OcaBundleTest {

    @Test
    fun `OcaBundle correctly gets all attributes`() = runTest {
        val result = ocaSimple.attributes

        val expectedLabelsFirstname = mapOf(
            LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN,
            LANGUAGE_DE to ATTRIBUTE_LABEL_FIRSTNAME_DE,
        )

        val expectedDataSourcesFirstname = mapOf(
            FORMAT to JSON_PATH_FIRSTNAME
        )

        val expectedLabelsAge = mapOf(
            LANGUAGE_EN to ATTRIBUTE_LABEL_AGE_EN,
            LANGUAGE_DE to ATTRIBUTE_LABEL_AGE_DE,
        )

        val expectedDataSourcesAge = mapOf(
            FORMAT to JSON_PATH_AGE
        )

        assertEquals(2, result.size)

        assertEquals(DIGEST, result[0].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, result[0].name)
        assertEquals(AttributeType.Text, result[0].attributeType)
        assertEquals(expectedLabelsFirstname, result[0].labels)
        assertEquals(expectedDataSourcesFirstname, result[0].dataSources)

        assertEquals(DIGEST, result[1].captureBaseDigest)
        assertEquals(ATTRIBUTE_KEY_AGE, result[1].name)
        assertEquals(AttributeType.Numeric, result[1].attributeType)
        assertEquals(expectedLabelsAge, result[1].labels)
        assertEquals(expectedDataSourcesAge, result[1].dataSources)
    }

    @Test
    fun `OcaBundle correctly gets attributes for data source format`() = runTest {
        val result = ocaSimple.getAttributesForDataSourceFormat(FORMAT, null)

        val expectedLabelsFirstname = mapOf(
            LANGUAGE_EN to ATTRIBUTE_LABEL_FIRSTNAME_EN,
            LANGUAGE_DE to ATTRIBUTE_LABEL_FIRSTNAME_DE,
        )

        val expectedDataSourcesFirstname = mapOf(
            FORMAT to JSON_PATH_FIRSTNAME
        )

        val expectedLabelsAge = mapOf(
            LANGUAGE_EN to ATTRIBUTE_LABEL_AGE_EN,
            LANGUAGE_DE to ATTRIBUTE_LABEL_AGE_DE,
        )

        val expectedDataSourcesAge = mapOf(
            FORMAT to JSON_PATH_AGE
        )

        val expected = mapOf(
            JSON_PATH_FIRSTNAME to OverlayBundleAttribute(
                captureBaseDigest = DIGEST,
                name = ATTRIBUTE_KEY_FIRSTNAME,
                attributeType = AttributeType.Text,
                labels = expectedLabelsFirstname,
                dataSources = expectedDataSourcesFirstname,
            ),
            JSON_PATH_AGE to OverlayBundleAttribute(
                captureBaseDigest = DIGEST,
                name = ATTRIBUTE_KEY_AGE,
                attributeType = AttributeType.Numeric,
                labels = expectedLabelsAge,
                dataSources = expectedDataSourcesAge,
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `OcaBundle correctly gets latest overlays of specific overlay type`() = runTest {
        val bundle = OcaBundle(captureBases = captureBasesGetLatestOverlays, overlays = overlaysGetLatestOverlays)
        val result = bundle.getLatestOverlaysOfType<LabelOverlay>()

        assertEquals(2, result.size)
        assertEquals(overlaysGetLatestOverlays[0], result[0])
        assertEquals(overlaysGetLatestOverlays[1], result[1])
    }

    @Test
    fun `OcaBundle correctly gets latest overlays of specific overlay type version`() = runTest {
        val bundle = OcaBundle(captureBases = captureBasesGetLatestOverlays, overlays = overlaysGetLatestOverlays)
        val result = bundle.getLatestOverlaysOfType<LabelOverlay1x0>()

        assertEquals(2, result.size)
        assertEquals(overlaysGetLatestOverlays[0], result[0])
        assertEquals(overlaysGetLatestOverlays[1], result[1])
    }

    @Test
    fun `OcaBundle correctly gets latest overlays for specific digest`() = runTest {
        val bundle = OcaBundle(captureBases = captureBasesGetLatestOverlays, overlays = overlaysGetLatestOverlays)
        val result = bundle.getLatestOverlaysOfType<LabelOverlay>("digest1")

        assertEquals(1, result.size)
        assertEquals(overlaysGetLatestOverlays[0], result[0])
    }

    @Test
    fun `OcaBundle correctly gets latest overlays of specific type version for specific digest`() = runTest {
        val bundle = OcaBundle(captureBases = captureBasesGetLatestOverlays, overlays = overlaysGetLatestOverlays)
        val result = bundle.getLatestOverlaysOfType<LabelOverlay1x0>("digest2")

        assertEquals(1, result.size)
        assertEquals(overlaysGetLatestOverlays[1], result[0])
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["$.firstname", """$["firstname"]""", "$['firstname']"]
    )
    fun `OcaBundle getAttributeForJsonPath with one level path returns attributes`(jsonPath: String) = runTest {
        val result = ocaSimple.getAttributeForJsonPath(jsonPath = jsonPath)

        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, result?.name)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["$.address.street", """$["address"].street""", "$['address'].street", """$["address"]['street']"""]
    )
    fun `OcaBundle getAttributeForJsonPath with multi level path returns attributes`(jsonPath: String) = runTest {
        val result = ocaNested.getAttributeForJsonPath(jsonPath = jsonPath)

        assertEquals(ATTRIBUTE_KEY_ADDRESS_STREET, result?.name)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["$.pets[0].name", """$["pets"][0].name""", "$['pets'][0]['name']"]
    )
    fun `OcaBundle getAttributeForJsonPath with array index path returns attributes`(jsonPath: String) = runTest {
        val result = ocaNested.getAttributeForJsonPath(jsonPath = jsonPath)

        assertEquals(ATTRIBUTE_KEY_NAME, result?.name)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["$.foobar.firstname", """$.foobar["firstname"]""", "$['foobar']['firstname']", """$["foobar"]['firstname']"""]
    )
    fun `OcaBundle getAttributeForJsonPath with different jsonPath notations in DataSource Overlay returns correct attributes`(
        jsonPath: String
    ) = runTest {
        val dataSourceOverlay = DataSourceOverlay1x0(
            captureBaseDigest = DIGEST,
            format = FORMAT,
            attributeSources = mapOf(
                ATTRIBUTE_KEY_FIRSTNAME to jsonPath,
            )
        )
        val oca = ocaSimple.copy(overlays = ocaSimple.overlays + dataSourceOverlay)
        val result = oca.getAttributeForJsonPath(jsonPath = "$.foobar.firstname")

        assertEquals(ATTRIBUTE_KEY_FIRSTNAME, result?.name)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["${'$'}foobar.firstname", "$..firstname", "$..['firstname']", """"$.foobar[*]""", "$[*]['firstname']", "$.*.firstname"]
    )
    fun `OcaBundle getAttributeForJsonPath with unsupported jsonPath in DataSource Overlay returns null`(
        jsonPath: String
    ) = runTest {
        val dataSourceOverlay = DataSourceOverlay1x0(
            captureBaseDigest = DIGEST,
            format = FORMAT,
            attributeSources = mapOf(
                ATTRIBUTE_KEY_FIRSTNAME to jsonPath,
            )
        )
        val oca = ocaSimple.copy(overlays = ocaSimple.overlays + dataSourceOverlay)
        assertNull(oca.getAttributeForJsonPath(jsonPath = "$.foobar.firstname"))
    }

    @Test
    fun `OcaBundle getAttributeForJsonPath with array wildcard path returns attributes`() = runTest {
        val result = ocaNested.getAttributeForJsonPath("$.pets[*].name")

        assertEquals(ATTRIBUTE_KEY_NAME, result?.name)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["", "$", "$.", "invalid", "$.invalid", "$..", "$.*", "$[*]", "$..*"]
    )
    fun `OcaBundle getAttributeForJsonPath with invalid path returns null`(path: String) = runTest {
        assertNull(ocaNested.getAttributeForJsonPath(path))
    }

    private val captureBasesGetLatestOverlays = listOf(
        CaptureBase1x0(
            digest = "digest1",
            attributes = emptyMap()
        ),
        CaptureBase1x0(
            digest = "digest2",
            attributes = emptyMap(),
        )
    )

    private val overlaysGetLatestOverlays = listOf(
        LabelOverlay1x0(
            captureBaseDigest = "digest1",
            language = "en",
            attributeLabels = emptyMap(),
        ),
        LabelOverlay1x0(
            captureBaseDigest = "digest2",
            language = "en",
            attributeLabels = emptyMap(),
        ),
        DataSourceOverlay1x0(
            captureBaseDigest = "digest1",
            format = "format1",
            attributeSources = emptyMap()
        ),
        DataSourceOverlay1x0(
            captureBaseDigest = "digest2",
            format = "format2",
            attributeSources = emptyMap()
        ),
    )
}
