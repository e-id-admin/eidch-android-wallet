package ch.admin.foitt.wallet.feature.qrscan.di

import ch.admin.foitt.wallet.feature.qrscan.infra.QrScanner
import ch.admin.foitt.wallet.feature.qrscan.infra.implementation.FakeQrScannerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.testing.TestInstallIn


@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [QrScanModule::class]
)
internal interface FakeQrScannerModule {
    @Binds
    fun bindFakeQrScanner(
        qrScanner: FakeQrScannerImpl
    ): QrScanner
}
