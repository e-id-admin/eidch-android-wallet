package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class VerifiableCredentialWithDisplaysAndClusters(
    @Embedded
    val verifiableCredential: VerifiableCredentialEntity,
    @Relation(
        entity = Credential::class,
        parentColumn = "credentialId",
        entityColumn = "id",
    )
    val credential: Credential,
    @Relation(
        entity = CredentialDisplay::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val credentialDisplays: List<CredentialDisplay>,
    @Relation(
        entity = CredentialClaimClusterEntity::class,
        parentColumn = "credentialId",
        entityColumn = "verifiableCredentialId",
    )
    val clusters: List<ClusterWithDisplaysAndClaims>,
    /**
     * Also makes the flows of this entity observe the bundle items, so that a credential status update is reflected
     * in the ui without waiting for the next flow collection.
     */
    @Relation(
        entity = BundleItemEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val bundleItems: List<BundleItemEntity>,
) {
    /**
     * Status of the copy that gets presented next.
     * Unknown while the bundle items are being replaced by a batch refresh.
     */
    val nextPresentableStatus: CredentialStatus
        get() = bundleItems.find {
            it.id == verifiableCredential.nextPresentableBundleItemId
        }?.status ?: CredentialStatus.UNKNOWN
}
