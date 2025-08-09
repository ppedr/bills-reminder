package com.example.bills;

import com.example.bills.model.Bill;
import com.example.bills.repository.BillRepository;
import com.example.bills.service.BillService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

@SpringBootTest
class BillServiceTests {

    @Autowired
    private BillService billService;

    @Autowired
    private BillRepository billRepository;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void sendsReminderForDueBills() {
        Bill bill = new Bill();
        bill.setName("Internet");
        bill.setDueDate(LocalDate.now());
        bill.setEmail("test@example.com");
        billRepository.save(bill);

        billService.sendDueBillsReminders();

        Mockito.verify(mailSender).send(Mockito.any());
    }
}
