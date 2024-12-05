package ch.admin.foitt.wallet.platform.database.data

import android.content.Context
import androidx.room.Room
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
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
                .build()
        }.mapError { throwable ->
            DatabaseError.SetupFailed(throwable)
        }

    companion object {
        private const val DATABASE_NAME = "app_database.db"

        init {
            System.loadLibrary("sqlcipher")
        }
    }
}
