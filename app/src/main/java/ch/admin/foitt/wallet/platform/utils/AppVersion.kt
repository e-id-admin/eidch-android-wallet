package ch.admin.foitt.wallet.platform.utils

import kotlin.math.max

data class AppVersion(val rawVersion: String) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int {
        val version1Splits = rawVersion.split(".")
        val version2Splits = other.rawVersion.split(".")
        val maxLengthOfVersionSplits = max(version1Splits.size, version2Splits.size)

        for (i in 0 until maxLengthOfVersionSplits) {
            val v1 = if (i < version1Splits.size) version1Splits[i].toInt() else 0
            val v2 = if (i < version2Splits.size) version2Splits[i].toInt() else 0
            val compare = v1.compareTo(v2)
            if (compare != 0) {
                return compare
            }
        }
        return 0
    }
}
