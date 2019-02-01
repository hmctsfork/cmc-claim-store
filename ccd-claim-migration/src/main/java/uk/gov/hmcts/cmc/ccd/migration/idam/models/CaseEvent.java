package uk.gov.hmcts.cmc.ccd.migration.idam.models;

public enum CaseEvent {

    SUBMIT_PRE_PAYMENT("SubmitPrePayment"),
    SUBMIT_POST_PAYMENT("SubmitPostPayment"),
    LINK_DEFENDANT("LinkDefendant"),
    MORE_TIME_REQUESTED_ONLINE("MoreTimeRequestedOnline"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DISPUTE("DisputesAll"),
    ALREADY_PAID("StatesPaid"),
    FULL_ADMISSION("AdmitAll"),
    PART_ADMISSION("AdmitPart"),
    DIRECTIONS_QUESTIONNAIRE_DEADLINE("DirectionsQuestionnaireDeadline"),
    CLAIMANT_RESPONSE_ACCEPTATION("ClaimantAccepts"),
    CLAIMANT_RESPONSE_REJECTION("ClaimantRejects"),
    OFFER_MADE_BY_CLAIMANT("OfferMadeByClaimant"),
    OFFER_MADE_BY_DEFENDANT("OfferMadeByDefendant"),
    OFFER_REJECTED_BY_CLAIMANT("OfferRejectedByClaimant"),
    OFFER_REJECTED_BY_DEFENDANT("OfferRejectedByDefendant"),
    OFFER_SIGNED_BY_CLAIMANT("OfferSignedByClaimant"),
    OFFER_COUNTER_SIGNED_BY_DEFENDANT("OfferCounterSignedByDefendant"),
    AGREEMENT_SIGNED_BY_CLAIMANT("AgreementSignedByClaimant"),
    AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT("AgreementCounterSignedByDefendant"),
    AGREEMENT_REJECTED_BY_DEFENDANT("AgreementRejectedByDefendant"),
    SETTLED_PRE_JUDGMENT("SettledPreJudgment"),
    CCJ_REQUESTED("CCJRequested"),
    INTERLOCATORY_JUDGEMENT("InterlocatoryJudgement"),
    REJECT_ORGANISATION_PAYMENT_PLAN("RejectOrganisationPaymentPlan"),
    REFER_TO_JUDGE_BY_CLAIMANT("ReferToJudgeByClaimant"),
    REFER_TO_JUDGE_BY_DEFENDANT("ReferToJudgeByDefendant");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}