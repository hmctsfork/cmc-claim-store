package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "core_case_data.api.url=false",
        "send-letter.url=false"
    }
)
public class DummyBulkPrintRequestTest extends BaseSaveTest {

    @MockBean
    private BulkPrintStaffNotificationService bulkPrintNotificationService;

    @Test
    public void shouldNotSendNotificationWhenEverythingIsOk() throws Exception {

        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService, never())
            .notifyFailedBulkPrint(any(Document.class), any(Document.class),
                eq(deserializeObjectFrom(result, Claim.class)));
    }

}