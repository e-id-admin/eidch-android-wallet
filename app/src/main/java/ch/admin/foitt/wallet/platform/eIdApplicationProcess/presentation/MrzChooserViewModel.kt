package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.mock.ApplyResponseBody
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.mock.StateResponseBody
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.model.MrzData
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.getOr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class MrzChooserViewModel @Inject constructor(
    private val safeJson: SafeJson,
    private val saveEIdRequestCase: SaveEIdRequestCase,
    private val saveEIdRequestState: SaveEIdRequestState,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.Details(navManager::navigateUp, null)
    override val fullscreenState = FullscreenState.Insets

    private val mockUnderAge = safeJson.safeDecodeStringTo<List<MrzData>>(MockMRZData.underAgeMock)
    private val mockAdult = safeJson.safeDecodeStringTo<List<MrzData>>(MockMRZData.adultMock)
    private val mockOther = safeJson.safeDecodeStringTo<List<MrzData>>(MockMRZData.otherMock)

    val mrzData = mockUnderAge.getOr(emptyList()) + mockAdult.getOr(emptyList()) + mockOther.getOr(emptyList())

    fun onBack() = navManager.navigateUpOrToRoot()

    fun onMrzItemClick(index: Int) {
        val mrzData = mrzData[index]
        val rawMrz = mrzData.payload.mrz.joinToString(";")
        saveData(rawMrz)
    }

    private fun saveData(rawMrz: String) {
        val applyResponseBody = safeJson.safeDecodeStringTo<ApplyResponseBody>(APPLY_RESPONSE).value
        val stateResponseBody = safeJson.safeDecodeStringTo<StateResponseBody>(STATE_RESPONSE).value

        val eIdRequestCase = EIdRequestCase(
            id = applyResponseBody.caseId,
            rawMrz = rawMrz,
            documentNumber = applyResponseBody.identityNumber,
            firstName = applyResponseBody.givenNames,
            lastName = applyResponseBody.surname,
        )

        val eIdRequestState = EIdRequestState(
            eIdRequestCaseId = applyResponseBody.caseId,
            state = stateResponseBody.state,
            lastPolled = Instant.now().epochSecond,
            onlineSessionStartOpenAt = Instant.parse(stateResponseBody.queueInformation?.expectedOnlineSessionStart).epochSecond,
            onlineSessionStartTimeoutAt = Instant.parse(stateResponseBody.onlineSessionStartTimeout).epochSecond
        )

        viewModelScope.launch {
            saveEIdRequestCase(eIdRequestCase)
            saveEIdRequestState(eIdRequestState)
        }
    }

    private companion object {
        const val APPLY_RESPONSE = """{
          "caseId": "1234-5678-abcd-1234567890ABCDE",
          "surname": "Muster",
          "givenNames": "Max Felix",
          "dateOfBirth": "1989-08-24T00:00:00Z",
          "identityType": "SWISS_PASS",
          "identityNumber": "A123456789",
          "validUntil": "2028-12-23T00:00:00Z",
          "legalRepresentant": false,
          "email": "user@examle.com"
        }"""

        const val STATE_RESPONSE = """{
          "state": "BereitFuerOnlineSession",
          "queueInformation": {
            "positionInQueue": 3978,
            "totalInQueue": 5789,
            "expectedOnlineSessionStart": "2024-03-24T00:00:00Z"
          },
          "legalRepresentant": {
            "verified": false,
            "verificationLink": "https://sid-test.ejpd.intra.admin.ch/sid-web/verify-legal-representant/1234567890"
          },
          "onlineSessionStartTimeout": "2024-03-27T00:00:00Z"
        }"""
    }

    private object MockMRZData {
        const val underAgeMock = """
[
  {
    "displayName":"UNDERAGE I7A (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7A<<<<<<7<<<<<<<<<<<<<<<","1001015X3012316<<<<<<<<<<<<<<2","MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE I7G (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7G<<<<<<3<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<0", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"],
      "legalRepresentant": true
    }
  },
  {
    "displayName":"UNDERAGE I7C (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7C<<<<<<9<<<<<<<<<<<<<<<", "1001015X2312318<<<<<<<<<<<<<<0", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE I7D (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7D<<<<<<0<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<6", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE I7E (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7E<<<<<<1<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<4", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE I7F (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7F<<<<<<2<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<2", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"],
      "legalRepresentant": true
    }
  },
  {
    "displayName":"UNDERAGE I7G (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7G<<<<<<3<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<0", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"],
      "legalRepresentant": true
    }
  },
  {
    "displayName":"UNDERAGE P7B (PASSPORT)",
    "payload": {
      "mrz": ["PM<<<MINDERJAEHRIGE<<ANNETTE<<<<<<<<<<<<<<<<", "P7B<<<<<<7<<<1001015X3012316<<<<<<<<<<<<<<02"]
    }
  },
  {
    "displayName":"UNDERAGE A (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<M101A<<<<4<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<8", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE B (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<M101B<<<<7<<<<<<<<<<<<<<<", "1001015X2312318<<<<<<<<<<<<<<4", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE C (FOREIGN) DEAD",
    "payload": {
      "mrz": ["AR<<<M101C<<<<0<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<6", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
    }
  },
  {
    "displayName":"UNDERAGE D (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<M101D<<<<3<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<0", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
    }
  }
]
"""
        const val adultMock = """
[
  {
    "displayName":"ADULT A (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E101A<<<<8<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<6", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT B (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E101B<<<<1<<<<<<<<<<<<<<<", "0001018X2312318<<<<<<<<<<<<<<2", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT C (FOREIGN) DEAD",
    "payload": {
      "mrz": ["AR<<<E101C<<<<4<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<4", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT D (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E101D<<<<7<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<8", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT A (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I1A<<<<<<9<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<4", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT C (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I1C<<<<<<1<<<<<<<<<<<<<<<", "0001018X2312318<<<<<<<<<<<<<<2", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT D (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I1D<<<<<<2<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<8", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT B (PASSPORT)",
    "payload": {
      "mrz": ["PM<<<MUSTER1<<HANS1<<<<<<<<<<<<<<<<<<<<<<<<<", "P1B<<<<<<9<<<0001018X3012316<<<<<<<<<<<<<<04"]
    }
  },
  {
    "displayName":"ADULT2 (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E102<<<<<5<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<2", "MUSTER2<<HANS2<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT2 (PASSPORT)",
    "payload": {
      "mrz": ["PM<<<MUSTER2<<HANS2<<<<<<<<<<<<<<<<<<<<<<<<<", "P2<<<<<<<1<<<0001018X3012316<<<<<<<<<<<<<<00"]
    }
  },
  {
    "displayName":"ADULT3 (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E103<<<<<2<<<<<<<<<<<<<<<", "0001018X2212315<<<<<<<<<<<<<<0", "MUSTER3<<HANS3<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT3 (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I3<<<<<<<5<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<2", "MUSTER3<<HANS3<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT4 (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E104<<<<<9<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<4", "MUSTER4<<HANS4<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT4 (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I4<<<<<<<8<<<<<<<<<<<<<<<", "0001018X2212315<<<<<<<<<<<<<<8", "MUSTER4<<HANS4<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT5 (FOREIGN) DEAD",
    "payload": {
      "mrz": ["AR<<<E105<<<<<6<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<0", "MUSTER5<<HANS5<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT5 (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I5<<<<<<<1<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<0", "MUSTER5<<HANS5<<<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"ADULT6 (PASSPORT) DEAD",
    "payload": {
      "mrz": ["PM<<<MUSTER6<<HANS6<<<<<<<<<<<<<<<<<<<<<<<<<", "P6<<<<<<<3<<<0001018X3012316<<<<<<<<<<<<<<06"]
    }
  }
]
"""
        const val otherMock = """
[
  {
    "displayName":"MUSTERMANN (FOREIGN)",
    "payload": {
      "mrz": ["AR<<<E012345676<<<<<<<<<<<<<<<", "7001017X4012313<<<<<<<<<<<<<<6", "MUSTERMANN<<FRANZ<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"MUSTERMANN (PASSPORT)",
    "payload": {
      "mrz": ["PM<<<MUSTERMANN<<MAX<<<<<<<<<<<<<<<<<<<<<<<<", "X012345679<<<7001017X4012313<<<<<<<<<<<<<<00"]
    }
  },
  {
    "displayName":"BEVORMUNDET (PASSPORT)",
    "payload": {
      "mrz": ["PM<<<BEVORMUNDET<<FRITZ<<<<<<<<<<<<<<<<<<<<<", "P9<<<<<<<2<<<0001018X3012316<<<<<<<<<<<<<<08"],
      "legalRepresentant": true
    }
  },
  {
    "displayName":"LORENZ (ID CARD)",
    "payload": {
      "mrz": ["ID<<<I7<<<<<<<7<<<<<<<<<<<<<<<", "6704034X3012316<<<<<<<<<<<<<<4", "LORENZ<<PHILIPPE<<<<<<<<<<<<<<"]
    }
  },
  {
    "displayName":"INFOSTAR (PASSPORT)",
    "payload": {
      "mrz": ["PM<<<MEIER<<NICHT<IM<INFOSTAR<<<<<<<<<<<<<<<", "P999<<<<<4<<<1001015X3012316<<<<<<<<<<<<<<08"]
    }
  }
]
"""
    }
}
