package ru.otus.bank.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.bank.dao.AccountDao;
import ru.otus.bank.entity.Account;
import ru.otus.bank.entity.Agreement;
import ru.otus.bank.service.exception.AccountException;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    AccountDao accountDao;

    @InjectMocks
    AccountServiceImpl accountServiceImpl;

    @Test
    public void testTransfer() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        assertEquals(new BigDecimal(90), sourceAccount.getAmount());
        assertEquals(new BigDecimal(20), destinationAccount.getAmount());
    }

    @Test
    public void testSourceNotFound() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());

        AccountException result = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));
            }
        });
        assertEquals("No source account", result.getLocalizedMessage());
    }


    @Test
    public void testTransferWithVerify() {
        Account sourceAccount = new Account();
        sourceAccount.setAmount(new BigDecimal(100));
        sourceAccount.setId(1L);

        Account destinationAccount = new Account();
        destinationAccount.setAmount(new BigDecimal(10));
        destinationAccount.setId(2L);

        when(accountDao.findById(eq(1L))).thenReturn(Optional.of(sourceAccount));
        when(accountDao.findById(eq(2L))).thenReturn(Optional.of(destinationAccount));

        ArgumentMatcher<Account> sourceMatcher =
                argument -> argument.getId().equals(1L) && argument.getAmount().equals(new BigDecimal(90));

        ArgumentMatcher<Account> destinationMatcher =
                argument -> argument.getId().equals(2L) && argument.getAmount().equals(new BigDecimal(20));

        accountServiceImpl.makeTransfer(1L, 2L, new BigDecimal(10));

        verify(accountDao).save(argThat(sourceMatcher));
        verify(accountDao).save(argThat(destinationMatcher));
        }
    @ParameterizedTest
    @CsvSource({"_acc1,1,1000",
                "null,2,100",
                "empty,3,1000000,"})
    public void testAddAccountByMatcher(String number,String typeStr, String amtStr) {
        Agreement agreement = new Agreement();
        agreement.setName("Client1");
        agreement.setId(123L);
        Account account = new Account();
        if(number == "empty") number = "";
        if(number == "null") number = null;
        number = agreement.getName() + number;
        Integer type = Integer.getInteger(typeStr);
        BigDecimal amt = new BigDecimal(amtStr);
        String finalNumber = number;
        ArgumentMatcher<Account> matcher = new ArgumentMatcher<Account>() {
            @Override
            public boolean matches(Account argument) {
                return  argument != null &&
                        argument.getAgreementId() == agreement.getId() &&
                        argument.getNumber().equals(finalNumber) &&
                        argument.getType() == type &&
                        argument.getAmount() == amt;
            }
        };
        when(accountDao.save(argThat(matcher))).thenReturn(account);
        accountServiceImpl.addAccount(agreement,number, type,amt);
    }
    @Test
    public void testGetAccounts() {
        Long id = 123L;
        Agreement agreement = new Agreement();
        agreement.setId(id);
        List<Account> accounts =  new ArrayList<>();

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        when(accountDao.findByAgreementId(captor.capture())).thenReturn(accounts);

        accountServiceImpl.getAccounts(agreement);

        Assertions.assertNotNull(captor);
        assertEquals(id, captor.getValue());
    }
    @Test
    public void testChargeException() {
        when(accountDao.findById(any())).thenReturn(Optional.empty());
        AccountException result = assertThrows(AccountException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                accountServiceImpl.charge(1L, new BigDecimal(10));
            }
        });
        assertEquals("No source account", result.getLocalizedMessage());
    }
    @Test
    public void testChargeSucces() {
        String number = "Client_acc";
        Integer type = 1;
        BigDecimal amt = new BigDecimal(100);
        BigDecimal sum = new BigDecimal(1000);
        Account account = new Account();
        account.setNumber(number);
        account.setType(type);
        account.setAmount(amt);

        when(accountDao.findById(any())).thenReturn(Optional.of(account));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        when(accountDao.save(captor.capture())).thenReturn(account);

        accountServiceImpl.charge(1L,sum);

        Assertions.assertNotNull(captor);
        assertEquals(number, captor.getValue().getNumber());
        assertEquals(type, captor.getValue().getType());
        assertEquals(amt.subtract(sum), captor.getValue().getAmount());
    }
}
