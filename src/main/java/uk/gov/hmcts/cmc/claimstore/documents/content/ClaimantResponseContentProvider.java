package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class ClaimantResponseContentProvider {

    private final PartyDetailsContentProvider partyDetailsContentProvider;
    private final ClaimDataContentProvider claimDataContentProvider;
    private final NotificationsProperties notificationsProperties;
    private final ResponseAcceptationContentProvider responseAcceptationContentProvider;
    private final ResponseRejectionContentProvider responseRejectionContentProvider;

    public ClaimantResponseContentProvider(
        PartyDetailsContentProvider partyDetailsContentProvider,
        ClaimDataContentProvider claimDataContentProvider,
        NotificationsProperties notificationsProperties,
        ResponseAcceptationContentProvider responseAcceptationContentProvider,
        ResponseRejectionContentProvider responseRejectionContentProvider
    ) {
        this.partyDetailsContentProvider = partyDetailsContentProvider;
        this.claimDataContentProvider = claimDataContentProvider;
        this.notificationsProperties = notificationsProperties;
        this.responseAcceptationContentProvider = responseAcceptationContentProvider;
        this.responseRejectionContentProvider = responseRejectionContentProvider;
    }

    public Map<String, Object> createContent(Claim claim) {
        requireNonNull(claim);

        Map<String, Object> content = new HashMap<>();

        content.put("claim", claimDataContentProvider.createContent(claim));
        claim.getClaimantRespondedAt().ifPresent(respondedAt -> {
            content.put("claimantSubmittedOn", formatDateTime(respondedAt));
            content.put("claimantSubmittedDate", formatDate(respondedAt));
        });

        ClaimantResponse claimantResponse = claim.getClaimantResponse().orElseThrow(IllegalStateException::new);
        content.put("amountPaid", claimantResponse.getAmountPaid());
        content.put("responseDashboardUrl", notificationsProperties.getFrontendBaseUrl());

        Response defendantResponse = claim.getResponse().orElseThrow(IllegalStateException::new);
        content.put("defendant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getDefendant(),
            defendantResponse.getDefendant(),
            claim.getDefendantEmail(),
            null,
            null
        ));
        content.put("claimant", partyDetailsContentProvider.createContent(
            claim.getClaimData().getClaimant(),
            claim.getSubmitterEmail()
        ));

        content.put("responseType", claimantResponse.getType().name());

        switch (claimantResponse.getType()) {
            case ACCEPTATION: {
                content.put("defendantAdmissionAccepted",
                    String.format("I accept %s", getDefendantAdmissionStatus(claim, defendantResponse)));
                ResponseAcceptation responseAcceptation = (ResponseAcceptation) claimantResponse;
                content.putAll(responseAcceptationContentProvider.createContent(claim));
                responseAcceptation.getFormaliseOption()
                    .map(FormaliseOption::getDescription)
                    .ifPresent(
                        selectedOption -> {
                            if (PartyUtils.isCompanyOrOrganisation(defendantResponse.getDefendant())
                                && selectedOption == FormaliseOption.REFER_TO_JUDGE.getDescription()) {
                                content.put("formaliseOption", "Please enter judgment by determination");
                            } else {
                                content.put("formaliseOption", selectedOption);
                            }
                        }
                    );
                claim.getTotalAmountTillDateOfIssue()
                    .map(totalAmount ->
                        totalAmount.subtract(claimantResponse.getAmountPaid().orElse(BigDecimal.ZERO))
                    )
                    .map(Formatting::formatMoney)
                    .ifPresent(formattedAmount -> content.put("totalAmount", formattedAmount));
                addFormalisedOption(claim, content, responseAcceptation);
            }
            break;
            case REJECTION:
                content.put("defendantAdmissionAccepted",
                    String.format("I reject %s", getDefendantAdmissionStatus(claim, defendantResponse)));
                content.putAll(responseRejectionContentProvider.createContent((ResponseRejection) claimantResponse));
                break;
            default:
                throw new MappingException("Invalid responseType " + claimantResponse.getType());

        }

        return content;
    }

    private void addFormalisedOption(
        Claim claim,
        Map<String, Object> content,
        ResponseAcceptation responseAcceptation
    ) {
        switch (responseAcceptation.getFormaliseOption().orElseThrow(IllegalArgumentException::new)) {
            case CCJ:
                content.put("ccj", claim.getCountyCourtJudgment());
                break;
            case SETTLEMENT:
            case REFER_TO_JUDGE:
                //No Action
                break;
            default:
                throw new MappingException("Invalid formalization type " + responseAcceptation.getFormaliseOption());
        }
    }

    private String getDefendantAdmissionStatus(Claim claim, Response response) {
        if (!claim.getReDeterminationRequestedAt().isPresent()) {
            return "this amount";
        }

        switch (response.getResponseType()) {
            case PART_ADMISSION:
                return formatMoney(((PartAdmissionResponse) response).getAmount());
            case FULL_ADMISSION:
                return "full admission";
            default:
                return "this amount";
        }
    }
}
