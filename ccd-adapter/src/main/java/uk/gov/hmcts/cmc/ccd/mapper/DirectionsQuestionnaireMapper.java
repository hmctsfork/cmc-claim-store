package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDCourtLocationOption;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.CourtLocationType;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertRequest;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.HearingLocation;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.RequireSupport;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.Witness;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isAllEmpty;
import static uk.gov.hmcts.cmc.ccd.util.StreamUtil.asStream;

@Component
public class DirectionsQuestionnaireMapper implements Mapper<CCDDirectionsQuestionnaire, DirectionsQuestionnaire> {

    private final ExpertRowMapper expertRowMapper;
    private final UnavailableDateMapper unavailableDateMapper;
    private final AddressMapper addressMapper;
    private final YesNoMapper yesNoMapper;

    @Autowired
    public DirectionsQuestionnaireMapper(
        ExpertRowMapper expertRowMapper,
        UnavailableDateMapper unavailableDateMapper,
        AddressMapper addressMapper,
        YesNoMapper yesNoMapper
    ) {
        this.expertRowMapper = expertRowMapper;
        this.unavailableDateMapper = unavailableDateMapper;
        this.addressMapper = addressMapper;
        this.yesNoMapper = yesNoMapper;
    }

    @Override
    public CCDDirectionsQuestionnaire to(DirectionsQuestionnaire directionsQuestionnaire) {
        if (directionsQuestionnaire == null) {
            return null;
        }

        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder = CCDDirectionsQuestionnaire.builder();
        directionsQuestionnaire.getRequireSupport().ifPresent(toRequireSupport(builder));

        toHearingLocation(directionsQuestionnaire.getHearingLocation(), builder);

        directionsQuestionnaire.getWitness().ifPresent(toWitness(builder));

        directionsQuestionnaire.getExpertRequest().ifPresent(toExpertRequest(builder));

        builder.expertReports(directionsQuestionnaire.getExpertReports()
            .stream()
            .map(expertRowMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        builder.unavailableDates(directionsQuestionnaire.getUnavailableDates()
            .stream()
            .map(unavailableDateMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        );

        return builder.build();
    }

    private Consumer<RequireSupport> toRequireSupport(
        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder
    ) {
        return requireSupport -> {
            requireSupport.getLanguageInterpreter().ifPresent(builder::languageInterpreted);
            requireSupport.getSignLanguageInterpreter().ifPresent(builder::signLanguageInterpreted);
            requireSupport.getOtherSupport().ifPresent(builder::otherSupportRequired);

            requireSupport.getHearingLoop()
                .map(YesNoOption::name)
                .map(CCDYesNoOption::valueOf)
                .ifPresent(builder::hearingLoop);

            requireSupport.getDisabledAccess()
                .map(YesNoOption::name)
                .map(CCDYesNoOption::valueOf)
                .ifPresent(builder::disabledAccess);
        };
    }

    private Consumer<ExpertRequest> toExpertRequest(
        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder
    ) {
        return expertRequest -> {
            builder.expertEvidenceToExamine(expertRequest.getExpertEvidenceToExamine());
            builder.reasonForExpertAdvice(expertRequest.getReasonForExpertAdvice());
        };
    }

    private Consumer<Witness> toWitness(CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder) {
        return witness -> {
            builder.selfWitness(yesNoMapper.to(witness.getSelfWitness()));
            witness.getNoOfOtherWitness().ifPresent(builder::howManyOtherWitness);
        };
    }

    private void toHearingLocation(
        HearingLocation hearingLocation,
        CCDDirectionsQuestionnaire.CCDDirectionsQuestionnaireBuilder builder
    ) {
        builder.hearingLocation(hearingLocation.getCourtName());
        builder.hearingLocationSlug(hearingLocation.getHearingLocationSlug());
        hearingLocation.getCourtAddress()
            .ifPresent(address -> builder.hearingCourtAddress(addressMapper.to(address)));
        builder.hearingLocationOption(CCDCourtLocationOption.valueOf(hearingLocation.getLocationOption().name()));
        hearingLocation.getExceptionalCircumstancesReason().ifPresent(builder::exceptionalCircumstancesReason);
    }

    @Override
    public DirectionsQuestionnaire from(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        if (ccdDirectionsQuestionnaire == null) {
            return null;
        }

        DirectionsQuestionnaire.DirectionsQuestionnaireBuilder builder = DirectionsQuestionnaire.builder();

        builder.hearingLocation(extractHearingLocation(ccdDirectionsQuestionnaire));
        builder.witness(extractWitness(ccdDirectionsQuestionnaire));
        builder.expertRequest(extractExpertRequest(ccdDirectionsQuestionnaire));
        builder.requireSupport(extractRequireSupport(ccdDirectionsQuestionnaire));

        List<ExpertReport> expertReports = asStream(ccdDirectionsQuestionnaire.getExpertReports())
            .map(expertRowMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        builder.expertReports(expertReports);

        List<UnavailableDate> unavailableDates = asStream(ccdDirectionsQuestionnaire.getUnavailableDates())
            .map(unavailableDateMapper::from)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        builder.unavailableDates(unavailableDates);

        return builder.build();
    }

    private RequireSupport extractRequireSupport(
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire
    ) {
        CCDYesNoOption hearingLoop = ccdDirectionsQuestionnaire.getHearingLoop();
        CCDYesNoOption disabledAccess = ccdDirectionsQuestionnaire.getDisabledAccess();
        if (isAllEmpty(
            ccdDirectionsQuestionnaire.getLanguageInterpreted(),
            ccdDirectionsQuestionnaire.getSignLanguageInterpreted(),
            ccdDirectionsQuestionnaire.getOtherSupportRequired())
            && isNull(hearingLoop)
            && isNull(disabledAccess
        )) {
            return null;
        }

        return RequireSupport.builder()
            .languageInterpreter(ccdDirectionsQuestionnaire.getLanguageInterpreted())
            .signLanguageInterpreter(ccdDirectionsQuestionnaire.getSignLanguageInterpreted())
            .hearingLoop(yesNoMapper.from(hearingLoop))
            .disabledAccess(yesNoMapper.from(disabledAccess))
            .otherSupport(ccdDirectionsQuestionnaire.getOtherSupportRequired())
            .build();
    }

    private ExpertRequest extractExpertRequest(
        CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire
    ) {
        if (isAllEmpty(
            ccdDirectionsQuestionnaire.getExpertEvidenceToExamine(),
            ccdDirectionsQuestionnaire.getReasonForExpertAdvice()
        )) {
            return null;
        }
        return ExpertRequest.builder()
            .expertEvidenceToExamine(ccdDirectionsQuestionnaire.getExpertEvidenceToExamine())
            .reasonForExpertAdvice(ccdDirectionsQuestionnaire.getReasonForExpertAdvice())
            .build();
    }

    private Witness extractWitness(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        CCDYesNoOption selfWitness = ccdDirectionsQuestionnaire.getSelfWitness();
        if (isNull(selfWitness)
            && isNull(ccdDirectionsQuestionnaire.getHowManyOtherWitness())) {
            return null;
        }

        return Witness.builder()
            .selfWitness(yesNoMapper.from(selfWitness))
            .noOfOtherWitness(ccdDirectionsQuestionnaire.getHowManyOtherWitness())
            .build();
    }

    private HearingLocation extractHearingLocation(CCDDirectionsQuestionnaire ccdDirectionsQuestionnaire) {
        HearingLocation.HearingLocationBuilder hearingLocation = HearingLocation.builder();

        hearingLocation.courtName(ccdDirectionsQuestionnaire.getHearingLocation());
        hearingLocation.courtAddress(addressMapper.from(ccdDirectionsQuestionnaire.getHearingCourtAddress()));
        hearingLocation.hearingLocationSlug(ccdDirectionsQuestionnaire.getHearingLocationSlug());
        hearingLocation.exceptionalCircumstancesReason(ccdDirectionsQuestionnaire.getExceptionalCircumstancesReason());

        hearingLocation.locationOption(
            CourtLocationType.valueOf(ccdDirectionsQuestionnaire.getHearingLocationOption().name()));

        return hearingLocation.build();
    }
}