package org.uni.santo.tomas.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.model.account.Account;
import org.uni.santo.tomas.helper.enums.AccountStatus;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AccountRepository implements PanacheRepositoryBase<Account, String> {

    private static final Logger LOG = Logger.getLogger(AccountRepository.class);

    public List<Account> findAccountsByCustomer(String customerDocumentNumber) {

        LOG.infof("@findAccountsByCustomer REPO > Inicia consulta de cuentas para el cliente con " +
                "documento: %s", customerDocumentNumber);

        return find("customer.documentNumber = ?1 AND status = ?2",
                customerDocumentNumber, AccountStatus.ACTIVE.getStatus())
                .list();
    }

    public Optional<Account> findAccountByNumber(String accountNumber) {

        LOG.infof("@findAccountByNumber REPO > Inicia consulta de la cuenta con numero: %s y estado ACTIVA",
                accountNumber);

        return find("accountNumber = ?1 AND status = ?2",
                accountNumber, AccountStatus.ACTIVE.getStatus())
                .firstResultOptional();
    }
}
