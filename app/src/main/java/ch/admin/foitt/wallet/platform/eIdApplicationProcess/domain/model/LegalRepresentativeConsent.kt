package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

enum class LegalRepresentativeConsent {
    VERIFIED,
    NOT_VERIFIED,
    NOT_REQUIRED,
}

fun StateResponse.toLegalRepresentativeConsent(): LegalRepresentativeConsent = when (legalRepresentant?.verified) {
    true -> LegalRepresentativeConsent.VERIFIED
    false -> LegalRepresentativeConsent.NOT_VERIFIED
    null -> LegalRepresentativeConsent.NOT_REQUIRED
}
