package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.util.useCursor
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getLongOrNullColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.IOException

class Migration28To29 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate28To29() {
        var db = helper.createDatabase(testDbName, 28)

        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedIssuer = "https://issuer-agent.domain.ch"
        val expectedSelectedConfigurationId = "configId"
        val expectedCreatedAt = 1000L

        db.execSQL(
            "INSERT INTO `Credential` (`id`,`format`,`issuerUrl`,`selectedConfigurationId`,`createdAt`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${Credential.Converters().fromCredentialFormat(expectedFormat)}'," +
                "'$expectedIssuer'," +
                "'$expectedSelectedConfigurationId'," +
                "$expectedCreatedAt" +
                ")"
        )

        val expectedProgressionState = "ACCEPTED"
        val expectedValidFrom = 100L
        val expectedValidUntil = 200L
        val expectedNextPresentableBundleItemId = 5L
        val updatedAt = 1500L

        db.execSQL(
            "INSERT INTO `VerifiableCredentialEntity` " +
                "(`credentialId`,`progressionState`,`issuer`,`validFrom`,`validUntil`,`createdAt`,`updatedAt`,`nextPresentableBundleItemId`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'$expectedProgressionState'," +
                "'$expectedIssuer'," +
                "$expectedValidFrom," +
                "$expectedValidUntil," +
                "$expectedCreatedAt," +
                "$updatedAt," +
                "$expectedNextPresentableBundleItemId" +
                ")"
        )

        db.close()

        db = helper.runMigrationsAndValidate(testDbName, 29, true)

        db.query("SELECT * FROM `VerifiableCredentialEntity`").useCursor { cursor ->
            cursor.moveToFirst()

            assertEquals(expectedCredentialId, cursor.getLongColumn("credentialId"))
            assertEquals(expectedProgressionState, cursor.getStringColumn("progressionState"))
            assertEquals(expectedIssuer, cursor.getStringColumn("issuer"))
            assertEquals(expectedValidFrom, cursor.getLongColumn("validFrom"))
            assertEquals(expectedValidUntil, cursor.getLongColumn("validUntil"))
            assertEquals(expectedCreatedAt, cursor.getLongColumn("createdAt"))
            assertEquals(expectedNextPresentableBundleItemId, cursor.getLongColumn("nextPresentableBundleItemId"))

            assertNull(cursor.getLongOrNullColumn("refreshedAt"))
            assertThrows(IllegalArgumentException::class.java) {
                cursor.getLongOrNullColumn("updatedAt")
            }
        }

        db.close()
    }
}
