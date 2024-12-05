package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ParseTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toParseTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ParseTokenStatusList
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject
import kotlin.experimental.and

class ParseTokenStatusListImpl @Inject constructor() : ParseTokenStatusList {

    override suspend fun invoke(
        statusList: TokenStatusList,
        index: Int
    ): Result<Int, ParseTokenStatusStatusListError> = runSuspendCatching {
        val list = statusList.decodeAndDeflate()
        list.getStatus(
            bits = statusList.bits,
            index = index
        )
    }.mapError(Throwable::toParseTokenStatusStatusListError)

    /**
     * @param index Index of the status list entry
     *
     * @return the status bits as an integer
     */
    private fun ByteArray.getStatus(bits: Int, index: Int): Int {
        val entryByte = this[index * bits / 8]
        // The starting position of the status in the Byte
        val bitIndex = (index * bits) % 8
        // Mask to remove all bits larger that the status
        val mask = ((1 shl bitIndex shl bits) - 1).toByte()
        // Drop all bits larger that our status
        val maskedByte = java.lang.Byte.toUnsignedInt(entryByte and mask)
        // Shift the status to the start of the byte so 1 = revoked, 2 = suspended, etc, also removed all bits smaller than our status
        return maskedByte shr bitIndex
    }
}
