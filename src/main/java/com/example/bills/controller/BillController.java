package com.example.bills.controller;

import com.example.bills.model.Bill;
import com.example.bills.service.BillService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return billService.save(bill);
    }

    @GetMapping
    public List<Bill> all() {
        return billService.findAll();
    }
}
