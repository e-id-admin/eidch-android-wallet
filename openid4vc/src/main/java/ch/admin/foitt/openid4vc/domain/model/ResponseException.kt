package ch.admin.foitt.openid4vc.domain.model

interface ResponseException {
    data class InvalidGrant(val description: String) : Exception(description)
}
