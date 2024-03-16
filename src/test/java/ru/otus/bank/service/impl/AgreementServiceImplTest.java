package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import ru.otus.bank.dao.AgreementDao;
import ru.otus.bank.entity.Agreement;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class AgreementServiceImplTest {

    private AgreementDao dao = Mockito.mock(AgreementDao.class);

    AgreementServiceImpl agreementServiceImpl;

    @BeforeEach
    public void init() {
        agreementServiceImpl = new AgreementServiceImpl(dao);
    }


    @ParameterizedTest
    @CsvSource({"test","empty", "null"})
    public void testAddAgreementByMatcher(String name) {
        if(name == "empty") name = "";
        if(name == "null") name = null;
        Agreement agreement = new Agreement();
        agreement.setName(name);
        String finalName = name;
        ArgumentMatcher<Agreement> matcher = new ArgumentMatcher<Agreement>() {
            @Override
            public boolean matches(Agreement argument) {
                return argument != null &&  argument.getName().equals(finalName);
            }
        };
        when(dao.save(argThat(matcher))).thenReturn(agreement);
        Agreement  result = agreementServiceImpl.addAgreement(name);
        assertEquals(name,result.getName());
    }
    @ParameterizedTest
    @MethodSource("provideParameters")
    public void testAddAgreementByCaptor(String name) {
        if(name == "null") name = null;
        Agreement agreement = new Agreement();
        agreement.setName(name);

        ArgumentCaptor<Agreement> captor = ArgumentCaptor.forClass(Agreement.class);
        when(dao.save(captor.capture())).thenReturn(null);

        agreementServiceImpl.addAgreement(name);

        Assertions.assertNotNull(captor);
        assertEquals(name, captor.getValue().getName());
    }
    public static Stream<? extends Arguments> provideParameters() {
        return Stream.of(Arguments.of("test"),Arguments.of(""),null);
    }

    @Test
    public void testFindByName() {
        String name = "test";
        Agreement agreement = new Agreement();
        agreement.setId(10L);
        agreement.setName(name);

        when(dao.findByName(name)).thenReturn(
                Optional.of(agreement));

        Optional<Agreement> result = agreementServiceImpl.findByName(name);

        Assertions.assertTrue(result.isPresent());
        assertEquals(10, agreement.getId());
    }

    @Test
    public void testFindByNameWithCaptor() {
        String name = "test";
        Agreement agreement = new Agreement();
        agreement.setId(10L);
        agreement.setName(name);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        when(dao.findByName(captor.capture())).thenReturn(
                Optional.of(agreement));

        Optional<Agreement> result = agreementServiceImpl.findByName(name);

        assertEquals("test", captor.getValue());
        Assertions.assertTrue(result.isPresent());
        assertEquals(10, agreement.getId());
    }

}
