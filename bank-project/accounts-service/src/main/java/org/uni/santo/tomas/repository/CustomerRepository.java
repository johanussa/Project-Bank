package org.uni.santo.tomas.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.model.customer.Customer;
import org.uni.santo.tomas.helper.enums.CustomerStatus;

import java.util.Optional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, String> {

    private static final Logger LOG = Logger.getLogger(CustomerRepository.class.getName());

    public Optional<Customer> findCustomerByDocument(String documentNumber) {

        LOG.infof("@findCustomerByDocument REPO > Inicia consulta del cliente con numero de documento: %s y " +
                "estado ACTIVO", documentNumber);

        return find("documentNumber = ?1 AND status = ?2",
                documentNumber, CustomerStatus.ACTIVE.getStatus())
                .firstResultOptional();
    }
}
