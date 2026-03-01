package ru.mrsu.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mrsu.api.entity.Customer;

@org.springframework.stereotype.Repository
public interface Repository extends JpaRepository<Customer, Long> {
}
