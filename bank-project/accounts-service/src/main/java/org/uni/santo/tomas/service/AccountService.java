package org.uni.santo.tomas.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.helper.enums.AccountStatus;
import org.uni.santo.tomas.helper.exception.ASException;
import org.uni.santo.tomas.model.account.Account;
import org.uni.santo.tomas.model.account.AccountDTO;
import org.uni.santo.tomas.model.customer.Customer;
import org.uni.santo.tomas.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class AccountService {

    private static final Logger LOG = Logger.getLogger(AccountService.class.getName());

    @Inject
    AccountRepository accountRepository;

    @Inject
    CustomerService customerService;

    public List<Account> getAccountsByCustomer(String documentNumber) {

        LOG.infof("@getAccountsByCustomer SERV > Inicia servicio para obtener cuentas del cliente con " +
                "documento: %s", documentNumber);

        List<Account> accounts = accountRepository.findAccountsByCustomer(documentNumber);

        LOG.infof("@getAccountsByCustomer SERV > Cuentas obtenidas para el cliente: %s", accounts.size());

        return accounts;
    }

    public Account getAccountByNumber(String accountNumber) {

        LOG.infof("@getAccountByNumber SERV > Inicia consulta de la cuenta con el numero: %s", accountNumber);

        Account account = getAccount(accountNumber);

        LOG.infof("@getAccountByNumber SERV > La cuenta con el numero: %s se obtuvo correctamente", accountNumber);

        return account;
    }

    public BigDecimal getAccountBalance(String accountNumber) {

        LOG.infof("@getAccountBalance SERV > Inicia servicio para consultar el saldo de la cuenta: %s", accountNumber);

        Account account = getAccount(accountNumber);

        LOG.infof("@getAccountBalance SERV > Se obtuvo los datos de la cuenta: %s correctamente", accountNumber);

        return account.getBalance();
    }

    @Transactional
    public void registerAccount(AccountDTO account) {

        LOG.info("@registerAccount SERV > Inicia servicio para registrar una cuenta en la base de datos");

        checkAccountExist(account.getAccountNumber());

        Customer customer = customerService.getCustomer(account.getCustomerDocumentNumber());

        LOG.info("@registerAccount SERV > El cliente se obtuvo correctamente");

        Account accountDB = buildAccount(account, customer);

        LOG.infof("@registerAccount SERV > Inicia registro de la cuenta: %s en BD", account.getAccountNumber());

        accountRepository.persist(accountDB);

        LOG.info("@registerAccount SERV > Finaliza servicio, La cuenta se registro correctamente");
    }

    @Transactional
    public Account updateAccount(AccountDTO account) {

        LOG.infof("@updateAccount SERV > Inicia servicio para actualizar los datos de la cuenta con numero: " +
                "%s", account.getAccountNumber());

        Account accountDB = getAccount(account.getAccountNumber());

        LOG.info("@updateAccount SERV > La cuenta se consulto correctamente. Inicia validacion del estado de la cuenta");

        validateAccountStatus(accountDB);

        accountDB.setAccountType(account.getAccountType());
        accountDB.setCurrency(account.getCurrency());
        accountDB.setStatus(account.getStatus());
        accountDB.setCustomer(customerService.getCustomer(account.getCustomerDocumentNumber()));

        LOG.infof("@updateAccount SERV > Finaliza servicio, la cuenta con numero: %s fue actualizada " +
                "correctamente", account.getAccountNumber());

        return accountDB;
    }

    @Transactional
    public void updateAccountBalance(AccountDTO account) {

        LOG.infof("@updateAccountBalance SERV > Inicia servicio para actualizar el saldo de la cuenta con " +
                "el numero: %s", account.getAccountNumber());

        Account accountDB = getAccount(account.getAccountNumber());

        accountDB.setBalance(account.getBalance());

        LOG.infof("@updateAccountBalance SERV > Finaliza servicio, el saldo de la cuenta con numero: %s fue " +
                "actualizado correctamente", account.getAccountNumber());
    }

    @Transactional
    public void deleteAccountByNumber(String accountNumber) {

        LOG.infof("@deleteAccountByNumber SERV > Inicia servicio para cambiar el estado del registro de la " +
                "cuenta con numero: %s", accountNumber);

        Account account = getAccount(accountNumber);

        account.setStatus("CERRADA");

        LOG.infof("@deleteAccountByNumber SERV > El estado de la cuenta con numero: %s fue cambiado a " +
                "CERRADA exitosamente", accountNumber);
    }

    private void validateAccountStatus(Account account) {

        LOG.infof("@validateAccountStatus SERV > El estado de la cuenta es: %s", account.getStatus());

        if (AccountStatus.CLOSED.getStatus().equals(account.getStatus())) {

            LOG.errorf("@validateAccountStatus SERV > La cuenta con numero: %s está cerrada y no puede ser " +
                    "modificada", account.getAccountNumber());

            throw new ASException(Response.Status.BAD_REQUEST.getStatusCode(), "La cuenta se encuentra en estado " +
                    "CERRADA y no puede ser modificada");
        }
    }

    private Account buildAccount(AccountDTO account, Customer customer) {

        return Account.builder()
                .status(AccountStatus.ACTIVE.getStatus())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO)
                .currency(account.getCurrency())
                .openingDate(LocalDateTime.now())
                .customer(customer)
                .build();
    }

    private void checkAccountExist(String accountNumber) {

        LOG.infof("@checkAccountExist SERV > Inicia servicio para validar si la cuenta con el numero: %s ya " +
                "existe en base de datos", accountNumber);

        accountRepository.findByIdOptional(accountNumber).ifPresent(accountDB -> {

            LOG.errorf("@checkAccountExist SERV > La cuenta con el numero: %s ya se encuentra registrada",
                    accountDB.getAccountNumber());

            throw new ASException(Response.Status.BAD_REQUEST.getStatusCode(), "La cuenta con el número de " +
                    "cuenta ingresado ya esta registrada");
        });
    }

    private Account getAccount(String accountNumber) {

        LOG.infof("@getAccount SERV > Inicia consulta de la cuenta con numero: %s", accountNumber);

        return accountRepository.findAccountByNumber(accountNumber).orElseThrow(() -> {

            LOG.errorf("@getAccount SERV > La cuenta con el numero: %s no existe o esta en estado CERRADA " +
                    "o BLOQUEADA", accountNumber);

            return new ASException(Response.Status.NOT_FOUND.getStatusCode(), "La cuenta con el numero de " +
                    "cuenta ingresado no existe");
        });
    }
}
