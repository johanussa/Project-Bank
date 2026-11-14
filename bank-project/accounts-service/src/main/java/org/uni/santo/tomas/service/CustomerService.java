package org.uni.santo.tomas.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.uni.santo.tomas.helper.exception.ASException;
import org.uni.santo.tomas.model.account.Account;
import org.uni.santo.tomas.model.customer.Customer;
import org.uni.santo.tomas.model.customer.CustomerDTO;
import org.uni.santo.tomas.helper.enums.CustomerStatus;
import org.uni.santo.tomas.repository.CustomerRepository;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CustomerService {

    private static final Logger LOG = Logger.getLogger(CustomerService.class.getName());

    @Inject
    CustomerRepository customerRepository;

    @Inject
    AccountService accountService;

    public List<Customer> getListCustomers() {

        LOG.info("@getListCustomers SERV > Inicia servicio para obtener listado de clientes");

        List<Customer> customers = customerRepository.listAll();

        LOG.infof("@getListCustomers SERV > Clientes obtenidos: %s", customers.size());

        return customers;
    }

    public Customer getCustomerByDocumentNumber(String documentNumber) {

        LOG.infof("@getCustomerByDocumentNumber SERV > Inicia consulta del cliente con el numero de " +
                "documento: %s", documentNumber);

        Customer customer = getCustomer(documentNumber);

        LOG.infof("@getCustomerByDocumentNumber SERV > El cliente con numero de documento: %s se obtuvo " +
                "correctamente", documentNumber);

        return customer;
    }

    @Transactional
    public void registerACustomer(CustomerDTO customerDTO) {

        LOG.info("@registerACustomer SERV > Inicia servicio para registrar un cliente en base de datos");

        checkCustomerExist(customerDTO.getDocumentNumber());

        Customer customerDB = buildCustomer(customerDTO);

        LOG.infof("@registerACustomer SERV > El cliente no existe, se registra con los datos: %s", customerDTO);

        customerRepository.persist(customerDB);

        LOG.info("@registerACustomer SERV > Finaliza servicio, el cliente se registro correctamente");
    }

    @Transactional
    public Customer updateACustomer(CustomerDTO customer, String documentNumber) {

        LOG.infof("@updateACustomer SERV > Inicia servicio para actualizar los datos del cliente con numero " +
                "de documento: %s", documentNumber);

        Customer customerDB = getCustomer(documentNumber);

        customerDB.setDocumentType(customer.getDocumentType());
        customerDB.setNames(customer.getNames());
        customerDB.setLastNames(customer.getLastNames());
        customerDB.setEmail(customer.getEmail());
        customerDB.setPhone(customer.getPhone());
        customerDB.setAddress(customer.getAddress());

        LOG.infof("@updateACustomer SERV > Finaliza servicio, el cliente con numero de documento: %s fue " +
                "actualizado correctamente", documentNumber);

        return customerDB;
    }

    @Transactional
    public void deleteCustomerByDocumentNumber(String documentNumber) {

        LOG.infof("@deleteCustomerByDocumentNumber SERV > Inicia servicio para cambiar el estado del registro " +
                "del cliente con numero de documento: %s", documentNumber);

        Customer customer = getCustomer(documentNumber);

        LOG.infof("@deleteCustomerByDocumentNumber SERV > El cliente se obtuvo correctamente. Inicia " +
                "validacion si el cliente con documento: %s tiene cuentas ACTIVAS", documentNumber);

        customerHaveAnyActiveAccounts(documentNumber);

        customer.setStatus(CustomerStatus.INACTIVE.getStatus());

        LOG.infof("@deleteCustomerByDocumentNumber SERV > El estado del cliente con numero de documento: " +
                "%s fue cambiado a INACTIVO exitosamente", documentNumber);
    }

    private void customerHaveAnyActiveAccounts(String documentNumber) {

        List<Account> accounts = accountService.getAccountsByCustomer(documentNumber);

        LOG.infof("@customerHaveAnyActiveAccounts SERV > El cliente tiene %s cuentas en estado ACTIVO", accounts.size());

        if (!accounts.isEmpty()) {

            LOG.errorf("@customerHaveAnyActiveAccounts SERV > No se permite desactivar la cuenta del " +
                    "cliente con documento: %s ya que tiene cuentas activas", documentNumber);

            throw new ASException(Response.Status.CONFLICT.getStatusCode(), "No se permite inactivar un cliente " +
                    "que tiene cuentas activas");
        }
    }

    private Customer buildCustomer(CustomerDTO customerDTO) {

        return Customer.builder()
                .documentNumber(customerDTO.getDocumentNumber())
                .documentType(customerDTO.getDocumentType())
                .names(customerDTO.getNames())
                .lastNames(customerDTO.getLastNames())
                .email(customerDTO.getEmail())
                .phone(customerDTO.getPhone())
                .address(customerDTO.getAddress())
                .status(CustomerStatus.ACTIVE.getStatus())
                .dateRegistration(LocalDateTime.now())
                .build();
    }

    private void checkCustomerExist(String documentNumber) {

        LOG.infof("@checkCustomerExist SERV > Inicia servicio para validar si el cliente con el numero de " +
                "documento: %s ya existe en base de datos", documentNumber);

        customerRepository.findByIdOptional(documentNumber).ifPresent(customDB -> {

            LOG.errorf("@checkCustomerExist SERV > El cliente con numero de documento: %s ya se encuentra " +
                    "registrado", customDB.getDocumentNumber());

            throw new ASException(Response.Status.BAD_REQUEST.getStatusCode(), "El cliente con el número de " +
                    "documento ingresado ya esta registrado");
        });
    }

    public Customer getCustomer(String documentNumber) {

        LOG.infof("@getCustomer SERV > Inicia consulta del cliente con documento: %s", documentNumber);

        return customerRepository.findCustomerByDocument(documentNumber).orElseThrow(() -> {

            LOG.errorf("@getCustomer SERV > El cliente con numero de documento: %s no existe o esta en " +
                    "estado INACTIVO", documentNumber);

            return new ASException(Response.Status.NOT_FOUND.getStatusCode(), "El cliente con el numero de " +
                    "documento ingresado no existe");
        });
    }
}
