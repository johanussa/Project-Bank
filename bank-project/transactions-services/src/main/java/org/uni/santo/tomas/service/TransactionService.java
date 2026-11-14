package org.uni.santo.tomas.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.helper.enums.TransactionStatus;
import org.uni.santo.tomas.helper.enums.TransactionType;
import org.uni.santo.tomas.helper.exception.TSException;
import org.uni.santo.tomas.model.Account;
import org.uni.santo.tomas.model.Transaction;
import org.uni.santo.tomas.model.TransactionDTO;
import org.uni.santo.tomas.repository.TransactionRepository;
import org.uni.santo.tomas.restClient.AccountProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    private static final Logger LOG = Logger.getLogger(TransactionService.class.getName());

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    @RestClient
    AccountProxy accountProxy;

    public List<Transaction> getTransactionsByAccount(String accountId) {

        LOG.infof("@getTransactionsByAccount SERV > Inicia servicio para obtener historial de la cuenta: %s", accountId);

        List<Transaction> transactions = transactionRepository.getTransactionsByAccount(accountId);

        LOG.infof("@getTransactionsByAccount SERV > Se encontraron: %d transacciones para la cuenta: %s",
                transactions.size(), accountId);

        return transactions;
    }

    public Transaction getTransactionById(String referenceNumber) {

        LOG.infof("@getTransactionById SERV > Inicia servicio para obtener transaccion con numero de " +
                "referencia: %s", referenceNumber);

        Transaction transaction = findTransactionByReference(referenceNumber);

        LOG.info("@getTransactionById SERV > La transaccion se obtuvo correctamente");

        return transaction;
    }

    @Transactional
    public void depositAmount(TransactionDTO transaction) {

        Transaction transactionDB = null;
        String accountNumber = transaction.getDestinationAccountId();

        LOG.infof("@depositAmount SERV > Inicia servicio para realizar deposito, cuenta numero: %s.", accountNumber);

        try {

            LOG.infof("@depositAmount SERV > Inicia servicio rest-client hacia account-proxy");

            Account account = accountProxy.getAccountByNumber(accountNumber);

            LOG.infof("@depositAmount SERV > La cuenta se obtuvo correctamente: %s", account);

            String referenceNumber = String.valueOf(LocalDate.now()).replace("-", "");
            String consecutive = String.format("%03d", transactionRepository.count() + 1);

            transactionDB = buildTransaction(referenceNumber, consecutive, transaction, TransactionType.DEPOSIT);

            LOG.info("@depositAmount SERV > Inicia servicio de guardado de registro de transaccion");

            transactionRepository.persistAndFlush(transactionDB);

            LOG.info("@depositAmount SERV > La transaccion se registro correctamente. Inicia actualizacion del saldo");

            Account accountRequest = Account.builder()
                    .accountNumber(accountNumber)
                    .balance(account.getBalance().add(transaction.getAmount()))
                    .build();

            LOG.infof("@depositAmount SERV > Inicia rest-client hacia account-proxy. Request: %s", accountRequest);

            accountProxy.updateAccountBalance(accountRequest);

            LOG.info("@depositAmount SERV > El monto de la cuenta se actualizo correctamente");

            transactionRepository.updateTransactionStatus(transactionDB.getReferenceNumber(), TransactionStatus.COMPLETED);

            LOG.infof("@depositAmount SERV > Finaliza servicio de depositar dinero a la cuenta: %s", accountNumber);

        } catch (Exception ex) {

            LOG.errorf(ex, "@depositAmount SERV > La cuenta numero: %s no existe o se encuentra inactiva", accountNumber);

            updateTransactionStatus(transactionDB);

            throw new TSException(Response.Status.BAD_REQUEST.getStatusCode(), "Se presento un error al realizar el " +
                    "deposito en la cuenta");
        }
    }

    @Transactional
    public void withdrawAmount(TransactionDTO transaction) {

        Transaction transactionDB = null;
        String accountNumber = transaction.getOriginAccountId();

        LOG.infof("@withdrawAmount SERV > Inicia servicio para realizar retiro, cuenta numero: %s.", accountNumber);

        try {

            LOG.infof("@withdrawAmount SERV > Inicia servicio rest-client hacia account-proxy");

            Account account = accountProxy.getAccountByNumber(accountNumber);

            LOG.infof("@withdrawAmount SERV > La cuenta se obtuvo correctamente: %s", account);

            validateAccountBalances(account, transaction.getAmount(), accountNumber);

            String referenceNumber = String.valueOf(LocalDate.now()).replace("-", "");
            String consecutive = String.format("%03d", transactionRepository.count() + 1);

            transactionDB = buildTransaction(referenceNumber, consecutive, transaction, TransactionType.WITHDRAWAL);

            LOG.info("@withdrawAmount SERV > Inicia servicio de guardado de registro de transaccion");

            transactionRepository.persistAndFlush(transactionDB);

            LOG.info("@withdrawAmount SERV > La transaccion se registro correctamente. Inicia actualizacion del saldo");

            Account accountRequest = Account.builder()
                    .accountNumber(accountNumber)
                    .balance(account.getBalance().subtract(transaction.getAmount()))
                    .build();

            LOG.infof("@withdrawAmount SERV > Inicia rest-client hacia account-proxy. Request: %s", accountRequest);

            accountProxy.updateAccountBalance(accountRequest);

            LOG.info("@withdrawAmount SERV > El monto de la cuenta se actualizo correctamente");

            transactionRepository.updateTransactionStatus(transactionDB.getReferenceNumber(), TransactionStatus.COMPLETED);

            LOG.infof("@withdrawAmount SERV > Finaliza servicio de retiro de dinero de la cuenta: %s", accountNumber);

        } catch (TSException ex) {

            LOG.errorf(ex, "@withdrawAmount SERV > Error de negocio al retirar de la cuenta: %s", accountNumber);

            updateTransactionStatus(transactionDB);

            throw ex;

        } catch (Exception ex) {

            LOG.errorf(ex, "@withdrawAmount SERV > Error inesperado al retirar de la cuenta: %s", accountNumber);

            updateTransactionStatus(transactionDB);

            throw new TSException(Response.Status.BAD_REQUEST.getStatusCode(), "Se presentó un error al realizar " +
                    "el retiro en la cuenta");
        }
    }

    @Transactional
    public void transferAmount(TransactionDTO transaction) {

        Transaction transactionDB = null;
        String originAccountNumber = transaction.getOriginAccountId();
        String destinationAccountNumber = transaction.getDestinationAccountId();

        LOG.infof("@transferAmount SERV > Inicia servicio para realizar transferencia. Origen: %s, Destino: %s.",
                originAccountNumber, destinationAccountNumber);

        try {

            validateOriginAndDestinationAccounts(originAccountNumber, destinationAccountNumber);

            LOG.info("@transferAmount SERV > Inicia servicio rest-client hacia account-proxy para obtener cuentas");

            Account originAccount = accountProxy.getAccountByNumber(originAccountNumber);
            Account destinationAccount = accountProxy.getAccountByNumber(destinationAccountNumber);

            verifyBalanceOriginAccount(originAccount.getBalance(), transaction.getAmount(), originAccountNumber);

            String referenceNumber = String.valueOf(LocalDate.now()).replace("-", "");
            String consecutive = String.format("%03d", transactionRepository.count() + 1);

            transactionDB = buildTransaction(referenceNumber, consecutive, transaction, TransactionType.TRANSFER);

            LOG.info("@transferAmount SERV > Inicia servicio de guardado de registro de transaccion");

            transactionRepository.persistAndFlush(transactionDB);

            LOG.info("@transferAmount SERV > Inicia resta del saldo a la cuenta de origen");

            Account accountRequest = Account.builder()
                    .accountNumber(originAccountNumber)
                    .balance(originAccount.getBalance().subtract(transaction.getAmount()))
                    .build();

            LOG.infof("@transferAmount SERV > Inicia rest-client hacia account-proxy. Request: %s", accountRequest);

            accountProxy.updateAccountBalance(accountRequest);

            LOG.info("@transferAmount SERV > Saldo restado a la cuenta origen. Sumando saldo a la cuenta destino");

            updateDestinationAccountBalance(destinationAccount, originAccount, transaction.getAmount());

            transactionRepository.updateTransactionStatus(transactionDB.getReferenceNumber(), TransactionStatus.COMPLETED);

            LOG.infof("@transferAmount SERV > Finaliza servicio de transferencia exitosa de %s a %s",
                    originAccountNumber, destinationAccountNumber);

        } catch (TSException ex) {

            LOG.errorf(ex, "@transferAmount SERV > Error de negocio en transferencia. Origen: %s, Destino: %s",
                    originAccountNumber, destinationAccountNumber);

            updateTransactionStatus(transactionDB);

            throw ex;

        } catch (Exception ex) {

            LOG.errorf(ex, "@transferAmount SERV > Error inesperado en transferencia. Origen: %s, Destino: %s",
                    originAccountNumber, destinationAccountNumber);

            updateTransactionStatus(transactionDB);

            throw new TSException(Response.Status.BAD_REQUEST.getStatusCode(), "Se presentó un error inesperado " +
                    "al realizar la transferencia");
        }
    }

    private void updateDestinationAccountBalance(Account destinationAccount, Account originAccount, BigDecimal amount) {

        Account accountRequest;

        try {
            accountRequest = Account.builder()
                    .accountNumber(destinationAccount.getAccountNumber())
                    .balance(destinationAccount.getBalance().add(amount))
                    .build();

            LOG.infof("@updateDestinationAccountBalance SERV > Inicia rest-client hacia account-proxy " +
                    "para actualizar saldo. Request: %s", accountRequest);

            accountProxy.updateAccountBalance(accountRequest);

            LOG.info("@updateDestinationAccountBalance SERV > Saldo sumado a la cuenta destino exitosamente");

        } catch (Exception ex) {

            LOG.errorf(ex, "@updateDestinationAccountBalance SERV > Falló la actualización de la cuenta " +
                    "destino. Iniciando compensación a cuenta origen");

            accountRequest = Account.builder()
                    .accountNumber(originAccount.getAccountNumber())
                    .balance(originAccount.getBalance().add(amount))
                    .build();

            LOG.infof("@updateDestinationAccountBalance SERV > Inicia rest-client hacia account-proxy " +
                    "para actualizar saldo. Request: %s", accountRequest);

            accountProxy.updateAccountBalance(accountRequest);

            throw new TSException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Fallo al depositar en " +
                    "la cuenta destino. El dinero ha sido devuelto a su cuenta origen");
        }
    }

    private void verifyBalanceOriginAccount(BigDecimal originBalance, BigDecimal transactionBalance, String originAccount) {

        if (originBalance.compareTo(transactionBalance) < 0) {

            LOG.warnf("@verifyBalanceOriginAccount SERV > Saldo insuficiente en la cuenta origen %s. Saldo " +
                    "actual: %s, Monto solicitado: %s", originAccount, originBalance, transactionBalance);

            throw new TSException(Response.Status.BAD_REQUEST.getStatusCode(), "Fondos insuficientes para " +
                    "realizar la transferencia");
        }
    }

    private void validateOriginAndDestinationAccounts(String originAccountNumber, String destinationAccountNumber) {

        if (originAccountNumber.equals(destinationAccountNumber)) {

            LOG.errorf("@validateOriginAndDestinationAccounts SERV > Error en la transferencia. La cuenta origen" +
                    ": %s y destino: %s son las mismas", originAccountNumber, destinationAccountNumber);

            throw new TSException(Response.Status.BAD_REQUEST.getStatusCode(), "La cuenta origen y destino no " +
                    "pueden ser la misma");
        }
    }

    private void validateAccountBalances(Account account, BigDecimal amount, String accountNumber) {

        if (account.getBalance().compareTo(amount) < 0) {

            LOG.warnf("@validateAccountBalances SERV > Saldo insuficiente en la cuenta %s. Saldo actual: %s, " +
                    "Monto solicitado: %s", accountNumber, account.getBalance(), amount);

            throw new TSException(Response.Status.BAD_REQUEST.getStatusCode(), "Fondos insuficientes para realizar " +
                    "el retiro");
        }
    }

    private void updateTransactionStatus(Transaction transactionDB) {

        if (transactionDB != null) {
            transactionRepository.updateTransactionStatus(transactionDB.getReferenceNumber(), TransactionStatus.FAILED);
        }
    }

    private Transaction findTransactionByReference(String referenceNumber) {

        return transactionRepository.findByIdOptional(referenceNumber).orElseThrow(() -> {

            LOG.errorf("@findTransactionByReference SERV > La Transaccion con el numero de referencia: " +
                    "%s no existe", referenceNumber);

            return new TSException(Response.Status.NOT_FOUND.getStatusCode(), "La transacción con referencia " +
                    "numero: " + referenceNumber + " no existe");
        });
    }

    private Transaction buildTransaction(String referenceNumber, String consecutive, TransactionDTO transaction, TransactionType type) {

        return Transaction.builder()
                .referenceNumber("TX-" + referenceNumber + "-" + consecutive)
                .type(type.getType())
                .amount(transaction.getAmount())
                .destinationAccountId(transaction.getDestinationAccountId())
                .transactionDate(LocalDateTime.now())
                .status(TransactionStatus.PENDING.getStatus())
                .description(type.getType())
                .build();
    }
}
