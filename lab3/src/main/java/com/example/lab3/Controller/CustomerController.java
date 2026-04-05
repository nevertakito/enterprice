package com.example.lab3.Controller;

import com.example.lab3.dto.CustomerCreateDto;
import com.example.lab3.dto.CustomerDto;
import com.example.lab3.dto.CustomerUpdateDto;
import com.example.lab3.Service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor // Инъекция через конструктор (вместо @Autowired)
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email
    ) {
        try {
            // 1. Парсинг сортировки
            String[] sortParams = sort.split(",");
            String sortBy = sortParams[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;

            if (sortParams.length > 1) {
                String dir = sortParams[1].trim().toLowerCase();
                if ("desc".equals(dir)) {
                    direction = Sort.Direction.DESC;
                }
            }

            // 2. Создание Pageable
            var pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // 3. Вызов сервиса (теперь возвращает Page<CustomerDto>)
            Page<CustomerDto> result = customerService.findCustomers(firstName, lastName, email, pageable);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Logger log = null;
            log.warn("Некорректный параметр сортировки: {}", sort);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            Logger log = null;
            log.error("Ошибка при получении списка клиентов", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========================================
    // GET BY ID
    // GET /api/v1/customers/1
    // ========================================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        try {
            CustomerDto dto = customerService.getCustomerById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            // Ловим исключение из сервиса и возвращаем 404
            Logger log = null;
            log.warn("Клиент не найден: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // CREATE
    // POST /api/v1/customers
    // ========================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody CustomerCreateDto dto) {
        Logger log = null;
        log.info("Запрос на создание клиента: {}", dto.getEmail());
        CustomerDto created = customerService.createCustomer(dto);
        return ResponseEntity.status(201).body(created);
    }

    // ========================================
    // UPDATE
    // PUT /api/v1/customers/1
    // ========================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerUpdateDto dto
    ) {
        try {
            CustomerDto updated = customerService.updateCustomer(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Logger log = null;
            log.warn("Не удалось обновить клиента {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // DELETE
    // DELETE /api/v1/customers/1
    // ========================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Logger log = null;
            log.warn("Не удалось удалить клиента {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}