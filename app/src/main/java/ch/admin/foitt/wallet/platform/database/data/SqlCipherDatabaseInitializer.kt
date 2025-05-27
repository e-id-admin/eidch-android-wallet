package ch.admin.foitt.wallet.platform.database.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.wallet.platform.database.domain.model.Converters
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqlCipherDatabaseInitializer @Inject constructor(
    @ApplicationContext private val appContext: Context
) : DatabaseInitializer {

    override fun create(password: ByteArray): Result<AppDatabase, DatabaseError.SetupFailed> =
        runSuspendCatching {
            Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(SupportOpenHelperFactory(password))
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }.mapError { throwable ->
            DatabaseError.SetupFailed(throwable)
        }

    companion object {
        private const val DATABASE_NAME = "app_database.db"

        // DB schema v3.1 to v3.3
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Credential ADD COLUMN validFrom INTEGER")
                db.execSQL("ALTER TABLE Credential ADD COLUMN validUntil INTEGER")


                val cursor = db.query("SELECT id, format, payload FROM CREDENTIAL")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val payload = cursor.getString(cursor.getColumnIndexOrThrow("payload"))
                    val formatString = cursor.getString(cursor.getColumnIndexOrThrow("format"))
                    val format = Converters().toCredentialFormat(formatString)

                    val (validFrom, validUntil) = when (format) {
                        CredentialFormat.VC_SD_JWT -> {
                            val sdJwt = SdJwt(payload)
                            Pair(sdJwt.nbfInstant?.epochSecond, sdJwt.expInstant?.epochSecond)
                        }
                        else -> error("invalid format")
                    }

                    db.execSQL("UPDATE Credential SET validFrom = $validFrom, validUntil = $validUntil WHERE id = $id")
                }

                cursor.close()
            }
        }

        // DB schema v3.3 to v3.4
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE EIdRequestState " +
                        "ADD COLUMN legalRepresentativeConsent TEXT NOT NULL " +
                        "DEFAULT '${LegalRepresentativeConsent.NOT_REQUIRED.name}'"
                )
            }
        }

        init {
            System.loadLibrary("sqlcipher")
        }
    }
}
