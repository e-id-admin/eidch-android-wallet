package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionEnforcement(
    @SerialName("id")
    val id: String,
    @SerialName("platform")
    val platform: Platform,
    @SerialName("priority")
    val priority: Priority,
    @SerialName("created")
    val created: String,
    @SerialName("criteria")
    val criteria: Criteria,
    @SerialName("displays")
    val displays: List<Display>,
) {
    enum class Platform {
        @SerialName("android")
        ANDROID,
        OTHER,
    }

    enum class Priority {
        @SerialName("low")
        LOW,

        @SerialName("medium")
        MEDIUM,

        @SerialName("high")
        HIGH,
    }

    @Serializable
    data class Criteria(
        @Serializable(with = AppVersionSerializer::class)
        @SerialName("minAppVersionIncluded")
        val minAppVersionIncluded: AppVersion? = null,
        @Serializable(with = AppVersionSerializer::class)
        @SerialName("maxAppVersionExcluded")
        val maxAppVersionExcluded: AppVersion,
    )

    @Serializable
    data class Display(
        @SerialName("title")
        val title: String,
        @SerialName("body")
        val text: String,
        @SerialName("locale")
        override val locale: String,
    ) : LocalizedDisplay
}
