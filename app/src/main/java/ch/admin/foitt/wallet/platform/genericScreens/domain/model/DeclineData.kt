package ch.admin.foitt.wallet.platform.genericScreens.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody

data class DeclineData(
    val responseUri: String,
    val reason: AuthorizationResponseErrorBody.ErrorType,
    val state: String?,
)
