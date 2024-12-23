package ch.admin.foitt.wallet.platform.passphraseInput.presentation

import android.view.inputmethod.EditorInfo
import androidx.annotation.DoNotInline
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.isScreenReaderOn
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTheme
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PassphraseInputComponent(
    modifier: Modifier = Modifier,
    passphraseInputFieldState: PassphraseInputFieldState = PassphraseInputFieldState.Typing,
    textFieldValue: TextFieldValue,
    passphraseInputAltText: String = "input your passphrase",
    enabled: Boolean = true,
    colors: TextFieldColors = WalletTextFieldColors.textFieldColors(),
    keyboardImeAction: ImeAction = ImeAction.Go,
    onKeyboardAction: () -> Unit,
    label: (@Composable () -> Unit)? = null,
    placeholder: @Composable () -> Unit = {},
    supportingText: @Composable () -> Unit = {},
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onAnimationFinished: (Boolean) -> Unit,
) {
    // Workaround to set https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_PERSONALIZED_LEARNING flag
    // Source: https://issuetracker.google.com/issues/359257538
    InterceptPlatformTextInput(
        interceptor = { request, nextHandler ->
            val modifiedRequest = PlatformTextInputMethodRequest { outAttributes ->
                request.createInputConnection(outAttributes).also {
                    NoPersonalizedLearningHelper.addNoPersonalizedLearning(outAttributes)
                }
            }
            nextHandler.startInputMethod(modifiedRequest)
        }
    ) {
        val focusRequester = remember { FocusRequester() }
        val windowInfo = LocalWindowInfo.current
        val keyboard = LocalSoftwareKeyboardController.current

        val context = LocalContext.current
        if (!context.isScreenReaderOn()) {
            LaunchedEffect(windowInfo) {
                snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
                    if (isWindowFocused) {
                        focusRequester.requestFocus()
                        keyboard?.show() ?: Timber.w("PinInputField: keyboard not controllable")
                    }
                }
            }
        }

        var showPassphrase by remember { mutableStateOf(false) }

        val errorAnimatable = createErrorAnimatable(
            passphraseInputFieldState = passphraseInputFieldState,
            onAnimationFinished = { onAnimationFinished(false) }
        )

        TextField(
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onTextFieldValueChange(
                            TextFieldValue(
                                text = textFieldValue.text,
                                selection = TextRange(textFieldValue.text.length, textFieldValue.text.length)
                            )
                        )
                    }
                }
                .offset {
                    createShakingOffset(amplitude = 10.dp.roundToPx(), errorAnimatable = errorAnimatable)
                }
                .semantics {
                    contentDescription = passphraseInputAltText
                }
                .testTag(TestTags.PIN_FIELD.name),
            value = textFieldValue,
            enabled = enabled,
            singleLine = true,
            isError = passphraseInputFieldState is PassphraseInputFieldState.Error,
            colors = colors,
            trailingIcon = {
                Icon(
                    modifier = Modifier
                        .clickable {
                            showPassphrase = !showPassphrase
                        }
                        .testTag(TestTags.SHOW_PASSPHRASE_ICON.name),
                    painter = if (showPassphrase) {
                        painterResource(R.drawable.wallet_ic_eye)
                    } else {
                        painterResource(R.drawable.wallet_ic_eye_crossed)
                    },
                    contentDescription = if (showPassphrase) {
                        stringResource(R.string.tk_global_visible_alt)
                    } else {
                        stringResource(R.string.tk_global_invisible_alt)
                    }
                )
            },
            visualTransformation = if (showPassphrase) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = keyboardImeAction,
            ),
            keyboardActions = when (keyboardImeAction) {
                ImeAction.Go -> KeyboardActions(onGo = { onKeyboardAction() })
                ImeAction.Next -> KeyboardActions(onNext = { onKeyboardAction() })
                else -> KeyboardActions()
            },
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            onValueChange = onTextFieldValueChange
        )
    }
}

@Composable
private fun createErrorAnimatable(
    passphraseInputFieldState: PassphraseInputFieldState,
    onAnimationFinished: () -> Unit
): Animatable<Float, AnimationVector1D> {
    val animation = remember { Animatable(0f) }
    LaunchedEffect(passphraseInputFieldState) {
        if (passphraseInputFieldState is PassphraseInputFieldState.Error) {
            animation.snapTo(0f)
            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000),
            )
            onAnimationFinished()
        }
    }
    return animation
}

private fun createShakingOffset(amplitude: Int, errorAnimatable: Animatable<Float, AnimationVector1D>) =
    IntOffset(
        x = (amplitude * sin(errorAnimatable.value * Math.PI * 3f).toFloat()).roundToInt(),
        y = 0
    )

internal object NoPersonalizedLearningHelper {
    @DoNotInline
    fun addNoPersonalizedLearning(info: EditorInfo) {
        info.imeOptions = info.imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
    }
}

@WalletComponentPreview
@Composable
private fun PassphraseInputComponentPreview() {
    WalletTheme {
        PassphraseInputComponent(
            textFieldValue = TextFieldValue("abc123"),
            onKeyboardAction = {},
            onTextFieldValueChange = {},
            onAnimationFinished = {},
        )
    }
}
