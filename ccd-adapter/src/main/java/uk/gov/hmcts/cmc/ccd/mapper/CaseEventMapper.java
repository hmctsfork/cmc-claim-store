package uk.gov.hmcts.cmc.ccd.mapper;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_REQUEST_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENDANT_PIN_LETTER_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DEFENDANT_RESPONSE_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_SEALED_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLEMENT_AGREEMENT_UPLOAD;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

public class CaseEventMapper {

    private static final Map<ClaimDocumentType, CaseEvent> claimDocumentTypeMap = new HashMap<ClaimDocumentType, CaseEvent>(){{
        put(SEALED_CLAIM, SEALED_CLAIM_UPLOAD);
        put(CLAIM_ISSUE_RECEIPT, CLAIM_ISSUE_RECEIPT_UPLOAD);
        put(DEFENDANT_RESPONSE_RECEIPT, DEFENDANT_RESPONSE_UPLOAD);
        put(CCJ_REQUEST, CCJ_REQUEST_UPLOAD);
        put(SETTLEMENT_AGREEMENT, SETTLEMENT_AGREEMENT_UPLOAD);
        put(DEFENDANT_PIN_LETTER, DEFENDANT_PIN_LETTER_UPLOAD);
    }};

    public static CaseEvent map(ClaimDocumentType type) {

        return claimDocumentTypeMap.getOrDefault(type, LINK_SEALED_CLAIM);
    }
}
