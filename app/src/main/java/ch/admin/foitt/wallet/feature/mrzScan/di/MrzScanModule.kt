package ch.admin.foitt.wallet.feature.mrzScan.di

import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.FetchSIdCase
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.SaveEIdRequestCase
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.SaveEIdRequestState
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.implementation.FetchSIdCaseImpl
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.implementation.SaveEIdRequestCaseImpl
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.implementation.SaveEIdRequestStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface MrzScanModule {
    @Binds
    fun bindSaveEIdRequestCase(
        useCase: SaveEIdRequestCaseImpl
    ): SaveEIdRequestCase

    @Binds
    fun bindSaveEIdRequestState(
        useCase: SaveEIdRequestStateImpl
    ): SaveEIdRequestState

    @Binds
    fun bindFetchSIdCase(
        useCase: FetchSIdCaseImpl
    ): FetchSIdCase
}
