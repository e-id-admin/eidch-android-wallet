package ch.admin.foitt.wallet.platform.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AnimatedButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    isEnabled: Boolean = true,
    label: String,
    onButtonClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = isVisible,
        ) {
            Button(
                onClick = onButtonClick,
                shape = MaterialTheme.shapes.small,
                enabled = isEnabled
            ) {
                Text(label)
            }
        }
    }
}
