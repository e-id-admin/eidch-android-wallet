package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetHasLegalGuardian
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import javax.inject.Inject

class SetHasLegalGuardianImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : SetHasLegalGuardian {
    override fun invoke(
        hasLegalGuardian: Boolean,
    ) {
        val eidApplicationProcessRepository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = EidApplicationRepositoryEntryPoint::class.java,
            componentScope = ComponentScope.EidApplicationProcess,
        ).eidApplicationProcessRepository()
        eidApplicationProcessRepository.setHasLegalGuardian(hasLegalGuardian)
    }
}
