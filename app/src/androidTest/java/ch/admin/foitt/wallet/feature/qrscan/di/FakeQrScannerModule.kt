package ch.admin.foitt.wallet.feature.qrscan.di

import ch.admin.foitt.wallet.feature.qrscan.infra.QrScanner
import ch.admin.foitt.wallet.feature.qrscan.infra.implementation.FakeQrScannerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [QrScanModule::class]
)
internal interface FakeQrScannerModule {
    @Binds
    @Singleton
    fun bindFakeQrScanner(
        qrScanner: FakeQrScannerImpl
    ): QrScanner
}
