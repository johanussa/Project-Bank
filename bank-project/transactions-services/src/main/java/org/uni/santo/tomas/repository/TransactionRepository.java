package org.uni.santo.tomas.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.helper.enums.TransactionStatus;
import org.uni.santo.tomas.model.Transaction;

import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepositoryBase<Transaction, String> {

    private static final Logger LOG = Logger.getLogger(TransactionRepository.class);

    public List<Transaction> getTransactionsByAccount(String accountId) {

        LOG.infof("@getTransactionsByAccount REPO > Inicia consulta de historial de transacciones para " +
                "la cuenta con numero: %s", accountId);

        return find("originAccountId = ?1 OR destinationAccountId = ?1 ORDER BY transactionDate DESC", accountId)
                .list();
    }

    public void updateTransactionStatus(String referenceNumber, TransactionStatus trxStatus) {

        LOG.infof("@updateTransactionStatus REPO > Inicia actualizacion de la transaccion con numero: %s al " +
                "estado: %s", referenceNumber, trxStatus);

        findByIdOptional(referenceNumber).ifPresent(transaction -> transaction.setStatus(trxStatus.getStatus()));
    }
}
