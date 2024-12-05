package ch.admin.foitt.openid4vc.domain.model

interface KeyPairError {
    data object NotFound : GetKeyPairError
    data object DeleteFailed : DeleteKeyPairError
    data class Unexpected(val throwable: Throwable) : CreateJWSKeyPairError, GetKeyPairError, DeleteKeyPairError
}

sealed interface CreateJWSKeyPairError
sealed interface GetKeyPairError

sealed interface DeleteKeyPairError
