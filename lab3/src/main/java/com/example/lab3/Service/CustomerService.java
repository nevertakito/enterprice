package com.example.lab3.Service;

import com.example.lab3.Model.Customer;
import com.example.lab3.Repository.CustomerRepository;
import com.example.lab3.dto.CustomerCreateDto;
import com.example.lab3.dto.CustomerUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.*;
import org.springframework.util.StringUtils;
import com.example.lab3.jms.NotificationProducer;
import com.example.lab3.dto.CustomerDto ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final NotificationProducer notificationProducer;

    public CustomerService(CustomerRepository customerRepository, NotificationProducer notificationProducer) {
        this.customerRepository = customerRepository;
        this.notificationProducer = notificationProducer;
    }


    @CacheEvict(value = {"customers", "allCustomers"}, allEntries = true)
    @Transactional
    public CustomerDto createCustomer(CustomerCreateDto dto) {
        Logger log = null;
        log.info("Создание нового клиента: {}", dto.getEmail());

        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer = customerRepository.save(customer);

        // Асинхронная отправка уведомления
        notificationProducer.sendWelcomeEmail(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName()
        );

        log.info("Клиент создан с ID: {}", customer.getId());
        return convertToDto(customer);
    }


    @Cacheable(value = "customers", key = "#id")
    public CustomerDto getCustomerById(Long id) {
        Logger log = null;
        log.info("Поиск клиента по ID: {}", id);
        return customerRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Клиент не найден: " + id));
    }


    @Cacheable(
            value = "allCustomers",
            key = "#firstName + '_' + #lastName + '_' + #email + '_' + #pageable"
    )
    public Page<CustomerDto> findCustomers(
            String firstName,
            String lastName,
            String email,
            Pageable pageable) {

        Logger log = null;
        log.debug("Поиск клиентов с фильтрами: firstName={}, lastName={}, email={}",
                firstName, lastName, email);

        Specification<Customer> spec = Specification.where((Specification<Customer>) null);

        if (StringUtils.hasText(firstName)) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%")
            );
        }
        if (StringUtils.hasText(lastName)) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%")
            );
        }
        if (StringUtils.hasText(email)) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%")
            );
        }

        return customerRepository.findAll(spec, pageable).map(this::convertToDto);
    }


    @CacheEvict(value = {"customers", "allCustomers"}, allEntries = true)
    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerUpdateDto dto) {
        Logger log = null;
        log.info("Обновление клиента ID: {}", id);

        return customerRepository.findById(id)
                .map(existing -> {
                    if (dto.getFirstName() != null) {
                        existing.setFirstName(dto.getFirstName());
                    }
                    if (dto.getLastName() != null) {
                        existing.setLastName(dto.getLastName());
                    }
                    if (dto.getEmail() != null) {
                        existing.setEmail(dto.getEmail());
                    }
                    Customer updated = customerRepository.save(existing);
                    log.info("Клиент обновлен: ID={}", updated.getId());
                    return convertToDto(updated);
                })
                .orElseThrow(() -> new RuntimeException("Клиент не найден: " + id));
    }


    @CacheEvict(value = {"customers", "allCustomers"}, allEntries = true)
    @Transactional
    public void deleteCustomer(Long id) {
        Logger log = null;
        log.info("Удаление клиента по ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Клиент не найден: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Клиент удален: ID={}", id);
    }


    private CustomerDto convertToDto(Customer customer) {
        if (customer == null) return null;

        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }
}

