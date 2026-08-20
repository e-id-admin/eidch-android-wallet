package ch.admin.foitt.wallet.platform.permission.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@SuppressLint("ComposeModifierReused")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScaffold(
    permissions: List<String>,
    modifier: Modifier = Modifier,
    requestImmediately: Boolean = false,
    permissionRationaleContent: @Composable (onHandled: () -> Unit) -> Unit = { _ -> },
    permissionNotGrantedContent: @Composable (handler: PermissionHandler) -> Unit = {},
    permissionGrantedContent: @Composable () -> Unit = {},
    onPermissionGranted: () -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        // Treat permission as granted for previews
        Box(modifier = modifier) {
            permissionGrantedContent()
        }
        return
    }

    var showPermissionRationale by rememberSaveable { mutableStateOf(false) }
    var shouldRequestPermission by rememberSaveable { mutableStateOf(false) }
    var wasDenied by rememberSaveable { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(permissions) { grants ->
        val isGranted = grants.all { (_, isGranted) -> isGranted }
        if (isGranted) {
            wasDenied = false
            onPermissionGranted()
        } else {
            wasDenied = true
        }

        showPermissionRationale = false
        shouldRequestPermission = false
    }

    Box(modifier = modifier) {
        // Display the appropriate content based on the permission state
        if (permissionState.allPermissionsGranted) {
            permissionGrantedContent()
        } else if (showPermissionRationale) {
            permissionRationaleContent {
                wasDenied = false
            }
        } else {
            val callback = PermissionHandler {
                shouldRequestPermission = true
            }
            permissionNotGrantedContent(callback)
        }
    }

    LaunchedEffect(wasDenied) {
        if (wasDenied) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(requestImmediately) {
        // If the permission should be requested immediately (as opposed to a user action in the [permissionNotGrantedContent]), set the flag to request the permission
        if (requestImmediately) {
            shouldRequestPermission = true
        }
    }

    LaunchedEffect(shouldRequestPermission) {
        // Actively request the permission or trigger the rationale if the flag is set
        if (shouldRequestPermission) {
            when {
                permissionState.allPermissionsGranted -> {
                    // Permission is granted, immediately invoke the callback
                    onPermissionGranted()
                }

                else -> {
                    // Request the permission
                    wasDenied = false
                    permissionState.launchMultiplePermissionRequest()
                }
            }

            shouldRequestPermission = false
        }
    }
}
