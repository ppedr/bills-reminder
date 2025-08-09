package com.example.bills;

import com.example.bills.model.Bill;
import com.example.bills.model.BillType;
import com.example.bills.service.BillService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BillServiceTests {

    @Autowired
    private BillService billService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void sendsReminderForDueBills() {
        Bill bill = new Bill();
        bill.setName("Internet");
        bill.setDueDate(LocalDate.now());
        bill.setEmail("test@example.com");
        bill.setType(BillType.INTERNET);
        billService.save(bill);

        billService.sendDueBillsReminders();

        Mockito.verify(mailSender).send(Mockito.any(SimpleMailMessage.class));
    }

    @Test
    void creditCardRequiresCardName() {
        Bill bill = new Bill();
        bill.setName("Credit Card");
        bill.setDueDate(LocalDate.now());
        bill.setEmail("test@example.com");
        bill.setType(BillType.CREDIT_CARD);

        assertThrows(IllegalArgumentException.class, () -> billService.save(bill));
    }

    @Test
    void markingAsPaidCreatesNewBill() {
        Bill bill = new Bill();
        bill.setName("Water");
        bill.setDueDate(LocalDate.of(2024, 5, 10));
        bill.setEmail("test@example.com");
        bill.setType(BillType.WATER);
        billService.save(bill);

        billService.markAsPaid(bill.getId());

        List<Bill> bills = billService.findAll();
        assertEquals(2, bills.size());

        Bill original = bills.stream().filter(b -> b.getId().equals(bill.getId())).findFirst().get();
        assertTrue(original.isPaid());

        Bill next = bills.stream().filter(b -> !b.getId().equals(bill.getId())).findFirst().get();
        assertFalse(next.isPaid());
        assertEquals(LocalDate.of(2024, 6, 10), next.getDueDate());
    }
}
