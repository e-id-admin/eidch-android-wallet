package ch.admin.foitt.wallet.platform.utils

/**
 * Provide a finite number of traversal index to choose from for the semantics.
 * This semantic index is mainly used by the Talkback feature.
 * [HIGH1] is the highest priority, , [LOW3] the lowest. Overall priorities are HIGH > DEFAULT > LOW.
 */
enum class TraversalIndex(val value: Float) {
    FIRST(Float.NEGATIVE_INFINITY),
    HIGH1(-5f),
    HIGH2(-4f),
    HIGH3(-3f),
    HIGH4(-2f),
    HIGH5(-1f),
    DEFAULT(0f),
    LOW1(1f),
    LOW2(2f),
    LOW3(3f),
    LAST(Float.POSITIVE_INFINITY),
}
