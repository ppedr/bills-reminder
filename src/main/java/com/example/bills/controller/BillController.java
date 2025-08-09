package com.example.bills.controller;

import com.example.bills.model.Bill;
import com.example.bills.service.BillService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

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
    
    @GetMapping("/reminder")
    public void forceReminder() {
    	billService.sendDueBillsReminders();
    }
}
