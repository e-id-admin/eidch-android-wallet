package ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.implementation

import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.toPainter
import javax.inject.Inject

internal class GetActorUiStateImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val getDrawableFromUri: GetDrawableFromUri,
) : GetActorUiState {
    override suspend fun invoke(
        actorDisplayData: ActorDisplayData,
    ): ActorUiState {
        val actorName: String? = actorDisplayData.getLocalizedName()
        val actorDataLogo: Painter? = actorDisplayData.getLocalizedIcon()

        return ActorUiState(
            name = actorName,
            painter = actorDataLogo,
            trustStatus = actorDisplayData.trustStatus,
            actorType = actorDisplayData.actorType,
        )
    }

    private fun ActorDisplayData.getLocalizedName(): String? = name?.let {
        getLocalizedDisplay(
            displays = name,
            preferredLocale = preferredLanguage,
        )
    }?.let { actorField: ActorField<String> ->
        return if (actorField.locale == DisplayLanguage.FALLBACK) {
            null
        } else if (actorField.value == DisplayConst.ISSUER_FALLBACK_NAME) {
            null
        } else {
            actorField.value
        }
    }

    private suspend fun ActorDisplayData.getLocalizedIcon(): Painter? = image?.let {
        getLocalizedDisplay(
            displays = image,
            preferredLocale = preferredLanguage,
        )
    }?.let {
        getDrawableFromUri(it.value)?.toPainter()
    }
}
