package com.example.bills;

import com.example.bills.model.Bill;
import com.example.bills.model.BillType;
import com.example.bills.service.BillService;
import com.example.bills.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private BillRepository billRepository;

    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    void clean() {
        billRepository.deleteAll();
    }

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

    @Test
    void findsBillsByMonthAndStatus() {
        Bill paidBill = new Bill();
        paidBill.setName("Electricity");
        paidBill.setDueDate(LocalDate.of(2024, 5, 20));
        paidBill.setEmail("test@example.com");
        paidBill.setType(BillType.ELECTRICITY);
        paidBill.setPaid(true);
        billService.save(paidBill);

        Bill unpaidBill = new Bill();
        unpaidBill.setName("Gas");
        unpaidBill.setDueDate(LocalDate.of(2024, 5, 21));
        unpaidBill.setEmail("test@example.com");
        unpaidBill.setType(BillType.GAS);
        billService.save(unpaidBill);

        List<Bill> paid = billService.findByPaidAndMonth(true, 2024, 5);
        List<Bill> unpaid = billService.findByPaidAndMonth(false, 2024, 5);

        assertEquals(1, paid.size());
        assertEquals("Electricity", paid.get(0).getName());
        assertEquals(1, unpaid.size());
        assertEquals("Gas", unpaid.get(0).getName());
    }
}
