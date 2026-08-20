package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(
    tableName = "VerifiableCredentialEntity",
    columnName = "updatedAt",
)
class Migration28to29 : AutoMigrationSpec
