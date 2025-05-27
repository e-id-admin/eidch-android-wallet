package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema

data class VcMetadata(
    val vcSchema: VcSchema?,
    val rawOcaBundle: RawOcaBundle?,
)
