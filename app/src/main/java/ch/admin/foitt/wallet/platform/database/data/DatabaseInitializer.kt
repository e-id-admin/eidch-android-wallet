package ch.admin.foitt.wallet.platform.database.data

import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import com.github.michaelbull.result.Result

internal interface DatabaseInitializer {
    fun create(password: ByteArray): Result<AppDatabase, DatabaseError.SetupFailed>
}
