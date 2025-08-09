package com.example.bills.repository;

import com.example.bills.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByDueDate(LocalDate dueDate);
}
