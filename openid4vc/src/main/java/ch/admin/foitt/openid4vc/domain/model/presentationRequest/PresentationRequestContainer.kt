package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.Invitation
import kotlinx.serialization.json.JsonElement

sealed interface PresentationRequestContainer : Invitation {
    class Json(
        val json: JsonElement,
    ) : PresentationRequestContainer

    class Jwt(
        val jwt: ch.admin.foitt.openid4vc.domain.model.jwt.Jwt,
    ) : PresentationRequestContainer
}
