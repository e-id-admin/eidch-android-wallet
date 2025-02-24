package ch.admin.foitt.wallet.platform.cameraPermissionHandler.di

import ch.admin.foitt.wallet.platform.cameraPermissionHandler.data.repository.SharedPrefsCameraIntroRepository
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.repository.CameraIntroRepository
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.ShouldAutoTriggerPermissionPrompt
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.implementation.CheckCameraPermissionImpl
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.implementation.ShouldAutoTriggerPermissionPromptImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface CameraPermissionHandlerModule {

    @Binds
    fun bindCheckQrScanPermission(
        useCase: CheckCameraPermissionImpl
    ): CheckCameraPermission

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
