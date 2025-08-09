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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
