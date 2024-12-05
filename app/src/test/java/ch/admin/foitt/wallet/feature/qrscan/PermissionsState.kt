package ch.admin.foitt.wallet.feature.qrscan

data class PermissionsState(
    val permissionWasTriggeredOnce: Boolean,
    val resultPermissionGranted: Boolean,
    val resultRationaleShouldBeShown: Boolean,
    val resultPromptWasTriggered: Boolean,
)
