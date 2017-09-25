package uk.gov.hmcts.cmc.claimstore.documents.content.models;

import uk.gov.hmcts.cmc.claimstore.models.Address;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.utils.PartyUtils;

import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class DefendantDetailsContent {

    private final String type;
    private final String fullName;
    private final Boolean nameAmended;
    private final String businessName;
    private final String contactPerson;
    private final Address address;
    private final Address correspondenceAddress;
    private final Boolean addressAmended;
    private final String dateOfBirth;
    private final String email;
    private final String signerName;
    private final String signerRole;

    public DefendantDetailsContent(
        final TheirDetails providedByClaimant,
        final DefendantResponse defendantResponse,
        final Party defendant
    ) {
        this.nameAmended  = !providedByClaimant.getName().equals(defendant.getName());
        this.addressAmended = !providedByClaimant.getAddress().equals(defendant.getAddress());
        this.type = PartyUtils.getType(providedByClaimant);
        this.fullName = nameAmended ? defendant.getName() : providedByClaimant.getName();
        this.businessName = PartyUtils.getBusinessName(defendantResponse.getResponse().getDefendant()).orElse(null);
        this.contactPerson = PartyUtils.getContactPerson(defendantResponse.getResponse().getDefendant()).orElse(null);
        this.address = addressAmended ? defendant.getAddress() : providedByClaimant.getAddress();
        this.correspondenceAddress = correspondenceAddress(defendantResponse);
        this.dateOfBirth = defendantDateOfBirth(defendant).orElse(null);
        this.email = defendantResponse.getDefendantEmail();

        Optional<StatementOfTruth> optionalStatementOfTruth = defendantResponse.getResponse().getStatementOfTruth();
        this.signerName = optionalStatementOfTruth.map((StatementOfTruth::getSignerName)).orElse(null);
        this.signerRole = optionalStatementOfTruth.map((StatementOfTruth::getSignerRole)).orElse(null);
    }

    private Address correspondenceAddress(final DefendantResponse defendantResponse) {
        return defendantResponse.getResponse().getDefendant().getCorrespondenceAddress().orElse(null);
    }

    private Optional<String> defendantDateOfBirth(final Party party) {
        if (party instanceof Individual) {
            return Optional.of(formatDate(((Individual) party).getDateOfBirth()));
        }
        return Optional.empty();
    }

    public String getType() {
        return type;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public Boolean getNameAmended() {
        return nameAmended;
    }

    public Address getAddress() {
        return address;
    }

    public Address getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public Boolean getAddressAmended() {
        return addressAmended;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getSignerName() {
        return signerName;
    }

    public String getSignerRole() {
        return signerRole;
    }
}
