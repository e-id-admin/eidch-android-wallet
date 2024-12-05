package ch.admin.foitt.wallet.feature.qrscan.di

import ch.admin.foitt.wallet.feature.qrscan.data.repository.SharedPrefsCameraIntroRepository
import ch.admin.foitt.wallet.feature.qrscan.domain.repository.CameraIntroRepository
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.CheckQrScanPermission
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.ShouldAutoTriggerPermissionPrompt
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.implementation.CheckQrScanPermissionImpl
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.implementation.ShouldAutoTriggerPermissionPromptImpl
import ch.admin.foitt.wallet.feature.qrscan.infra.QrScanner
import ch.admin.foitt.wallet.feature.qrscan.infra.implementation.QrScannerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface QrScanModule {

    @Binds
    fun bindQrScanner(
        qrScanner: QrScannerImpl
    ): QrScanner

    @Binds
    fun bindCheckQrScanPermission(
        useCase: CheckQrScanPermissionImpl
    ): CheckQrScanPermission

    @Binds
    fun bindShouldTriggerPermissionPrompt(
        useCase: ShouldAutoTriggerPermissionPromptImpl
    ): ShouldAutoTriggerPermissionPrompt

    @Binds
    @ActivityRetainedScoped
    fun bindCameraIntroRepository(
        repo: SharedPrefsCameraIntroRepository
    ): CameraIntroRepository
}
