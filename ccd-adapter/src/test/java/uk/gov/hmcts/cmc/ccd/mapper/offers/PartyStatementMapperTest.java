package uk.gov.hmcts.cmc.ccd.mapper.offers;

import com.sun.javafx.UnmodifiableArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.multipart.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SamplePartyStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PartyStatementMapperTest {

    @Autowired
    private PartyStatementMapper partyStatementMapper;

    @Test
    public void shouldMapPartyStatementWithoutPaymentIntentionToCCD() {
        //given
        final PartyStatement partyStatement = SamplePartyStatement.builder().build();

        //when
        CCDPartyStatement ccdPartyStatement = partyStatementMapper.to(partyStatement);

        //then
        assertThat(partyStatement).isEqualTo(ccdPartyStatement);
    }

    @Test
    public void shouldMapWithoutOfferToCCD() {
        //given
        final List<PartyStatement> partyStatements = Arrays.asList(
            SamplePartyStatement.acceptPartyStatement,
            SamplePartyStatement.rejectPartyStatement,
            SamplePartyStatement.counterSignPartyStatement,
            SamplePartyStatement.offerPartyStatement
        );

        //when
        List<CCDPartyStatement> ccdPartyStatements = partyStatements.stream()
            .map(partyStatementMapper::to)
            .collect(Collectors.toList());


        //then
        ListIterator<PartyStatement> partyStatementIterator = partyStatements.listIterator();
        ListIterator<CCDPartyStatement> ccdPartyStatementIterator = ccdPartyStatements.listIterator();

        while(partyStatementIterator.hasNext()){
            assertThat(partyStatementIterator.next()).isEqualTo(ccdPartyStatementIterator.next());
        }

    }

    @Test
    public void shouldMapCCDPartyStatementToDomain() {
        //given
        List<CCDPartyStatement> ccdPartyStatements = Stream.of(
                    SamplePartyStatement.acceptPartyStatement,
                    SamplePartyStatement.rejectPartyStatement,
                    SamplePartyStatement.counterSignPartyStatement,
                    SamplePartyStatement.offerPartyStatement,
                    SamplePartyStatement.builder().build())
                .map(partyStatementMapper::to)
                .collect(Collectors.toList());

        //when
        List<PartyStatement> partyStatements = ccdPartyStatements.stream()
            .map(partyStatementMapper::from)
            .collect(Collectors.toList());

        //then
        ListIterator<PartyStatement> partyStatementIterator = partyStatements.listIterator();
        ListIterator<CCDPartyStatement> ccdPartyStatementIterator = ccdPartyStatements.listIterator();

        while(partyStatementIterator.hasNext()){
            assertThat(partyStatementIterator.next()).isEqualTo(ccdPartyStatementIterator.next());
        }
    }

    @Test
    public void shouldMapEmptyCCDPartyStatementToDomain() {
        //given
        final CCDPartyStatement ccdPartyStatement = CCDPartyStatement.builder().build();

        //when
        PartyStatement partyStatement = partyStatementMapper.from(ccdPartyStatement);

        //then
        assertThat(partyStatement).isEqualTo(ccdPartyStatement);
    }


}
