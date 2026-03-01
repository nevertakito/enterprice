package ru.mrsu.api.service;

import ru.mrsu.api.entity.Customer;
import ru.mrsu.api.repository.Repository;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
public class Service {
    public final Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
    }

    public Customer getById(Long id){
        return repository.findById(id).orElseThrow();
    }

    public List<Customer> getAll(){
        return repository.findAll();
    }

    public Customer create(Customer customer) {
        if(customer.getCreatedAt() == null){
            customer.setCreatedAt(LocalDateTime.now());
        }
        return repository.save(customer);
    }

    public Customer update(Long id, Customer customer){
        Customer updateCustomer = repository.findById(id).orElseThrow();
        updateCustomer.setFirstName(customer.getFirstName());
        updateCustomer.setLastName(customer.getLastName());
        updateCustomer.setEmail(customer.getEmail());
        return repository.save(updateCustomer);
    }

    public void delete(Long id){
        Customer deleteCustomer = repository.findById(id).orElseThrow();
        repository.delete(deleteCustomer);
    }
}
