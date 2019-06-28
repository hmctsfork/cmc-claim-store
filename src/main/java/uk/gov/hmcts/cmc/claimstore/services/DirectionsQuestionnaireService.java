package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ASSIGN_FOR_DIRECTIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFERRED_TO_MEDIATION;
import static uk.gov.hmcts.cmc.claimstore.utils.ClaimantResponseHelper.isOptedForMediation;
import static uk.gov.hmcts.cmc.claimstore.utils.TheirDetailsHelper.isDefendantBusiness;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;
import static uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt.isPilotCourt;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.cmc.domain.models.response.ResponseType.PART_ADMISSION;

@Service
public class DirectionsQuestionnaireService {

    public Optional<CaseEvent> prepareCaseEvent(ResponseRejection responseRejection) {
        DirectionsQuestionnaire directionsQuestionnaire = responseRejection.getDirectionsQuestionnaire()
            .orElseThrow(IllegalStateException::new);

        String courtName = directionsQuestionnaire.getHearingLocation().getCourtName();
        if (isOptedForMediation(responseRejection)) {
            return Optional.of(REFERRED_TO_MEDIATION);
        }

        if (isPilotCourt(courtName)) {
            return Optional.of(ASSIGN_FOR_DIRECTIONS);
        }

        return Optional.empty();
    }

    public String getPreferredCourt(Claim claim) {
        if (isDefendantBusiness(claim.getClaimData().getDefendant())) {
            ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
            return getClaimantHearingCourt(claimantResponse);
        } else {
            Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);
            return getDefendantHearingCourt(defendantResponse);
        }
    }

    public String getDefendantHearingCourt(Response defendantResponse) {
        if (defendantResponse.getResponseType() == FULL_DEFENCE) {
            return ((FullDefenceResponse) defendantResponse).getDirectionsQuestionnaire()
                .orElseThrow(IllegalStateException::new)
                .getHearingLocation().getCourtName();
        } else if (defendantResponse.getResponseType() == PART_ADMISSION) {
            return ((PartAdmissionResponse) defendantResponse).getDirectionsQuestionnaire()
                .orElseThrow(IllegalStateException::new)
                .getHearingLocation().getCourtName();
        } else {
            throw new IllegalStateException("No preferred court as defendant response is full admission");
        }
    }

    public String getClaimantHearingCourt(ClaimantResponse claimantResponse) {
        if (claimantResponse.getType() == REJECTION) {
            return ((ResponseRejection) claimantResponse).getDirectionsQuestionnaire()
                .orElseThrow(IllegalStateException::new)
                .getHearingLocation().getCourtName();
        } else {
            throw new IllegalStateException("No preferred court as claimant response is not rejection.");
        }
    }
}
