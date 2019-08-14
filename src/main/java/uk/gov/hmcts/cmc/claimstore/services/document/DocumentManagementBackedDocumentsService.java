package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.Optional;

@Service("documentsService")
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final CCDEventProducer ccdEventProducer;

    @Autowired
    @SuppressWarnings("squid:S00107")
    // Content providers are formatted values and aren't worth splitting into multiple models.
    public DocumentManagementBackedDocumentsService(
        ClaimService claimService,
        DocumentManagementService documentManagementService,
        SealedClaimPdfService sealedClaimPdfService,
        ClaimIssueReceiptService claimIssueReceiptService,
        DefendantResponseReceiptService defendantResponseReceiptService,
        SettlementAgreementCopyService settlementAgreementCopyService,
        CCDEventProducer ccdEventProducer
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.ccdEventProducer = ccdEventProducer;
    }

    private PdfService getService(ClaimDocumentType claimDocumentType) {
        switch (claimDocumentType) {
            case CLAIM_ISSUE_RECEIPT:
                return claimIssueReceiptService;
            case SEALED_CLAIM:
                return sealedClaimPdfService;
            case DEFENDANT_RESPONSE_RECEIPT:
                return defendantResponseReceiptService;
            case SETTLEMENT_AGREEMENT:
                return settlementAgreementCopyService;
            default:
                throw new IllegalArgumentException(
                    "Unknown document service for document of type " + claimDocumentType.name());
        }
    }

    @Override
    public byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return processRequest(claim,
            authorisation,
            claimDocumentType,
            getService(claimDocumentType));
    }

    private byte[] processRequest(
        Claim claim,
        String authorisation,
        ClaimDocumentType claimDocumentType,
        PdfService pdfService
    ) {
        Optional<ClaimDocument> claimDocument = claim.getClaimDocument(claimDocumentType);
        try {
            if (claimDocument.isPresent()) {
                return documentManagementService.downloadDocument(authorisation, claimDocument.get());
            } else {
                PDF document = pdfService.createPdf(claim);
                uploadToDocumentManagement(document, authorisation, claim);
                return document.getBytes();
            }
        } catch (Exception ex) {
            return pdfService.createPdf(claim).getBytes();
        }
    }

    public Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim) {
        ClaimDocument claimDocument = documentManagementService.uploadDocument(authorisation, document);
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection(claim, claimDocument);

        Claim newClaim = claimService.saveClaimDocuments(authorisation,
            claim.getId(),
            claimDocumentCollection,
            document.getClaimDocumentType());

        ccdEventProducer.saveClaimDocumentCCDEvent(authorisation,
            claim,
            claimDocumentCollection,
            document.getClaimDocumentType());

        return newClaim;
    }

    private ClaimDocumentCollection getClaimDocumentCollection(Claim claim, ClaimDocument claimDocument) {
        ClaimDocumentCollection claimDocumentCollection = claim.getClaimDocumentCollection()
            .orElse(new ClaimDocumentCollection());
        claimDocumentCollection.addClaimDocument(claimDocument);
        return claimDocumentCollection;
    }
}
