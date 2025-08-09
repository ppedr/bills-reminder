package com.example.bills.service;

import com.example.bills.model.Bill;
import com.example.bills.model.BillType;
import com.example.bills.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private JavaMailSender mailSender;
    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    private BillService billService;

    @BeforeEach
    void setUp() {
        billService = new BillService(billRepository, mailSender);
    }

    @Test
    void shouldSendReminderOneDayBeforeDueDate() {
        Bill bill = new Bill();
        bill.setName("Internet");
        bill.setEmail("test@example.com");
        bill.setType(BillType.INTERNET);
        bill.setDueDate(LocalDate.of(2024, 5, 10));
        when(billRepository.findAll()).thenReturn(List.of(bill));

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 9));

        verify(mailSender).send(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getSubject()).contains("Bill due tomorrow");
    }

    @Test
    void shouldSendReminderOnNextBusinessDayWhenDueDateIsWeekend() {
        Bill bill = new Bill();
        bill.setName("Electricity");
        bill.setEmail("test@example.com");
        bill.setType(BillType.ELECTRICITY);
        bill.setDueDate(LocalDate.of(2024, 5, 25)); // Saturday
        when(billRepository.findAll()).thenReturn(List.of(bill));

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 27)); // Monday

        verify(mailSender).send(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getSubject()).contains("Bill due:");
    }
}
