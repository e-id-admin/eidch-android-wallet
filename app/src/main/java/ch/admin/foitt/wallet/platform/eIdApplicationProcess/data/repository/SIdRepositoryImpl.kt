package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toFetchSIdCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toFetchSIdStateError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class SIdRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val environmentSetupRepo: EnvironmentSetupRepository,
) : SIdRepository {
    override suspend fun fetchSIdCase(applyRequest: ApplyRequest): Result<CaseResponse, ApplyRequestError> =
        runSuspendCatching<CaseResponse> {
            httpClient.post(environmentSetupRepo.sidBackendUrl + "/eid/apply") {
                contentType(ContentType.Application.Json)
                setBody(applyRequest)
            }.body()
        }.mapError(Throwable::toFetchSIdCaseError)

    override suspend fun fetchSIdState(caseId: String): Result<StateResponse, StateRequestError> =
        runSuspendCatching<StateResponse> {
            httpClient.get(environmentSetupRepo.sidBackendUrl + "/eid/$caseId/state") {
                contentType(ContentType.Application.Json)
            }.body()
        }.mapError(Throwable::toFetchSIdStateError)
}
