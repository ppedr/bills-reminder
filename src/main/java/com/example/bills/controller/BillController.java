package com.example.bills.controller;

import com.example.bills.model.Bill;
import com.example.bills.service.BillService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bill create(@RequestBody Bill bill) {
        log.info("Creating bill: {}", bill.getName());
        return billService.save(bill);
    }

    @GetMapping
    public List<Bill> all() {
        log.info("Fetching all bills");
        return billService.findAll();
    }

    @GetMapping("/paid")
    public List<Bill> paid(@RequestParam int year, @RequestParam int month) {
        log.info("Fetching paid bills for {}/{}", year, month);
        return billService.findByPaidAndMonth(true, year, month);
    }

    @GetMapping("/unpaid")
    public List<Bill> unpaid(@RequestParam int year, @RequestParam int month) {
        log.info("Fetching unpaid bills for {}/{}", year, month);
        return billService.findByPaidAndMonth(false, year, month);
    }
    
    @GetMapping("/reminder")
    public void forceReminder() {
        billService.sendDueBillsReminders();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Deleting bill {}", id);
        billService.delete(id);
    }

    @PostMapping("/{id}/paid")
    public Bill markAsPaid(@PathVariable Long id) {
        log.info("Marking bill {} as paid", id);
        return billService.markAsPaid(id);
    }
}
