package com.example.bills.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate dueDate;
    private String email;
    @Enumerated(EnumType.STRING)
    private BillType type;
    @Enumerated(EnumType.STRING)
    private CreditCardName creditCardName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BillType getType() {
        return type;
    }

    public void setType(BillType type) {
        this.type = type;
    }

    public CreditCardName getCreditCardName() {
        return creditCardName;
    }

    public void setCreditCardName(CreditCardName creditCardName) {
        this.creditCardName = creditCardName;
    }
}
