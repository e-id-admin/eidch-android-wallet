package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetHasLegalGuardian
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetHasLegalGuardianImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : GetHasLegalGuardian {
    override operator fun invoke(): StateFlow<Boolean> {
        val eidApplicationProcessRepository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = EidApplicationProcessEntryPoint::class.java,
            componentScope = ComponentScope.EidApplicationProcess,
        ).eidApplicationProcessRepository()

        return eidApplicationProcessRepository.hasLegalGuardian
    }
}
