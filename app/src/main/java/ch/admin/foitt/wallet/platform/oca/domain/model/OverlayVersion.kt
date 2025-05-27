package ch.admin.foitt.wallet.platform.oca.domain.model

data class OverlayVersion(val version: String) : Comparable<OverlayVersion> {
    override fun compareTo(other: OverlayVersion): Int {
        val thisVersionParts = this.version.split(".").mapNotNull { it.toIntOrNull() }
        val otherVersionParts = other.version.split(".").mapNotNull { it.toIntOrNull() }
        // make sure version has 3 parts (major, minor, patch) -> pad with zeros
        val thisPadded = thisVersionParts + List(VERSION_PARTS - thisVersionParts.size) { 0 }
        val otherPadded = otherVersionParts + List(VERSION_PARTS - otherVersionParts.size) { 0 }

        return thisPadded.zip(otherPadded)
            .map { it.first.compareTo(it.second) }
            .firstOrNull { it != 0 } ?: 0
    }

    private companion object {
        const val VERSION_PARTS = 3
    }
}
