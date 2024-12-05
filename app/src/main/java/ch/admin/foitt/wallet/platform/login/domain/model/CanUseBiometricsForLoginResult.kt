package ch.admin.foitt.wallet.platform.login.domain.model

enum class CanUseBiometricsForLoginResult {
    DeactivatedInDeviceSettings,
    RemovedInDeviceSettings,
    Changed,
    Usable,
    NotSetUpInApp,
    NoHardwareAvailable
}
