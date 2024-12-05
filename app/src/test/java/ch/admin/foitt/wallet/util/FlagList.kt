package ch.admin.foitt.wallet.util

import kotlin.math.ceil
import kotlin.math.log2

/**
 * Get a list of list of Boolean, each sub-list being the binary representation of a number.
 * The list starts with the representation of the [start] value and ends with the [endInclusive] value.
 */
internal fun getFlagLists(start: Int, endInclusive: Int): List<List<Boolean>> = IntRange(start, endInclusive).map { intFlags ->
    val fixedLength = ceil(log2(endInclusive.toDouble())).toInt()
    intFlags.toBooleans(fixedLength)
}

private fun Int.toBooleans(fixedLength: Int): List<Boolean> {
    return Integer.toBinaryString(this).padStart(fixedLength, '0')
        .map { char ->
            when (char.digitToInt()) {
                0 -> false
                1 -> true
                else -> error("wrong digit")
            }
        }
}
