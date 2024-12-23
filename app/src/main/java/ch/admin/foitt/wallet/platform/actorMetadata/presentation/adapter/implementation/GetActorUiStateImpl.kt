package ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.implementation

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.toPainter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class GetActorUiStateImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val getDrawableFromUri: GetDrawableFromUri,
    @ApplicationContext private val appContext: Context,
) : GetActorUiState {
    override suspend fun invoke(
        actorDisplayData: ActorDisplayData,
        @StringRes defaultName: Int,
    ): ActorUiState {
        val actorName: String = actorDisplayData.name?.let {
            getLocalizedDisplay(
                displays = actorDisplayData.name,
                preferredLocale = actorDisplayData.preferredLanguage,
            )
        }?.value ?: appContext.getString(defaultName)

        val actorLogo: Painter? = actorDisplayData.image?.let {
            getLocalizedDisplay(
                displays = actorDisplayData.image,
                preferredLocale = actorDisplayData.preferredLanguage,
            )
        }?.let {
            getDrawableFromUri(it.value)?.toPainter()
        }

        return ActorUiState(
            name = actorName,
            painter = actorLogo,
            trustStatus = actorDisplayData.trustStatus,
        )
    }
}
