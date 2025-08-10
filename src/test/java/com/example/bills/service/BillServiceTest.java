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

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private EmailService emailService;
    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    private BillService billService;

    @BeforeEach
    void setUp() {
        billService = new BillService(billRepository, emailService);
    }

    @Test
    void shouldSendReminderOneDayBeforeDueDate() {
        Bill bill = new Bill();
        bill.setName("Internet");
        bill.setEmail("test@example.com");
        bill.setType(BillType.INTERNET);
        bill.setDueDate(LocalDate.of(2024, 5, 10));
        when(billRepository.findByPaidFalse()).thenReturn(List.of(bill));

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 9));

        verify(emailService).sendEmail(eq(bill.getEmail()), subjectCaptor.capture(), anyString());
        assertThat(subjectCaptor.getValue()).contains("Bill due tomorrow");
    }

    @Test
    void shouldSendReminderOnNextBusinessDayWhenDueDateIsWeekend() {
        Bill bill = new Bill();
        bill.setName("Electricity");
        bill.setEmail("test@example.com");
        bill.setType(BillType.ELECTRICITY);
        bill.setDueDate(LocalDate.of(2024, 5, 25)); // Saturday
        when(billRepository.findByPaidFalse()).thenReturn(List.of(bill));

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 27)); // Monday

        verify(emailService).sendEmail(eq(bill.getEmail()), subjectCaptor.capture(), anyString());
        assertThat(subjectCaptor.getValue()).contains("Bill due:");
    }

    @Test
    void shouldSendReminderWhenBillIsOverdue() {
        Bill bill = new Bill();
        bill.setName("Water");
        bill.setEmail("test@example.com");
        bill.setType(BillType.WATER);
        bill.setDueDate(LocalDate.of(2024, 5, 10));
        when(billRepository.findByPaidFalse()).thenReturn(List.of(bill));

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 11));

        verify(emailService).sendEmail(eq(bill.getEmail()), subjectCaptor.capture(), anyString());
        assertThat(subjectCaptor.getValue()).contains("Bill overdue");
    }

    @Test
    void shouldNotConsiderNextMonthBillAsOverdue() {
        Bill bill = new Bill();
        bill.setName("Internet");
        bill.setEmail("test@example.com");
        bill.setType(BillType.INTERNET);
        bill.setDueDate(LocalDate.of(2024, 6, 10));
        when(billRepository.findByPaidFalse()).thenReturn(List.of(bill));

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 11));

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void shouldKeepDueDateWhenBillIsInFutureMonth() throws Exception {
        Bill bill = new Bill();
        bill.setDueDate(LocalDate.of(2024, 8, 10));

        Method method = BillService.class.getDeclaredMethod("calculateNextDueDate", Bill.class, LocalDate.class);
        method.setAccessible(true);

        LocalDate result = (LocalDate) method.invoke(billService, bill, LocalDate.of(2024, 5, 11));

        assertThat(result).isEqualTo(bill.getDueDate());
    }

    @Test
    void shouldNotSendReminderForPaidBills() {
        when(billRepository.findByPaidFalse()).thenReturn(List.of());

        billService.sendDueBillsReminders(LocalDate.of(2024, 5, 10));

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void markingBillAsPaidCreatesNextMonthEntry() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setName("Internet");
        bill.setEmail("test@example.com");
        bill.setType(BillType.INTERNET);
        bill.setDueDate(LocalDate.of(2024, 5, 10));

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        billService.markAsPaid(1L);

        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(billRepository, times(2)).save(billCaptor.capture());

        List<Bill> savedBills = billCaptor.getAllValues();
        Bill original = savedBills.get(0);
        Bill nextMonth = savedBills.get(1);

        assertThat(original.isPaid()).isTrue();
        assertThat(nextMonth.isPaid()).isFalse();
        assertThat(nextMonth.getDueDate()).isEqualTo(LocalDate.of(2024, 6, 10));
        assertThat(nextMonth.getName()).isEqualTo(bill.getName());
    }
}
