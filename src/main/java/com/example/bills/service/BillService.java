package com.example.bills.service;

import com.example.bills.model.Bill;
import com.example.bills.model.BillType;
import com.example.bills.repository.BillRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public Bill save(Bill bill) {
        if (bill.getType() == BillType.CREDIT_CARD && bill.getCreditCardName() == null) {
            throw new IllegalArgumentException("Credit card bills must include card name");
        }
        return billRepository.save(bill);
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public List<Bill> findByPaidAndMonth(boolean paid, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return billRepository.findByPaidAndDueDateBetween(paid, start, end);
    }

    @Transactional
    public void delete(Long id) {
        if (!billRepository.existsById(id)) {
            throw new IllegalArgumentException("Bill not found");
        }
        billRepository.deleteById(id);
    }

    @Transactional
    public Bill markAsPaid(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));
        bill.setPaid(true);
        Bill savedBill = billRepository.save(bill);

        Bill nextMonthBill = new Bill();
        nextMonthBill.setName(savedBill.getName());
        nextMonthBill.setEmail(savedBill.getEmail());
        nextMonthBill.setType(savedBill.getType());
        nextMonthBill.setCreditCardName(savedBill.getCreditCardName());
        nextMonthBill.setDueDate(savedBill.getDueDate().plusMonths(1));
        nextMonthBill.setPaid(false);
        billRepository.save(nextMonthBill);

        return savedBill;
    }

    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void sendDueBillsReminders() {
        sendDueBillsReminders(LocalDate.now().minusDays(2l));
    }

    void sendDueBillsReminders(LocalDate today) {
        List<Bill> bills = billRepository.findByPaidFalse();

        for (Bill bill : bills) {
        	LocalDate dueDate = calculateNextDueDate(bill, today);
        	
            if (handleOverdue(bill, today, dueDate)) {
            	//
            }
            else if (handleDueTomorrow(bill, today, dueDate)) {
            	//
            }
            else if (handleDueToday(bill, today, dueDate)) {
            	//
            } else {
            	log.warn("There are no bills to be reminded");
            }
        }
    }

    private boolean handleOverdue(Bill bill, LocalDate today, LocalDate dueDate) {
        LocalDate adjustedDueDate = adjustForWeekend(dueDate);
        if (today.isAfter(adjustedDueDate)) {
            String subject = String.format("Bill overdue: %s", bill.getName());
            String body = String.format("Your bill %s was due on %s. Please pay(if not) and mark it as paid.",
                    bill.getName(), adjustedDueDate);
            sendEmail(bill, subject, body);
            log.info(subject);
            return true;
        }
        return false;
    }

    private boolean handleDueTomorrow(Bill bill, LocalDate today, LocalDate dueDate) {
        if (today.equals(dueDate.minusDays(1))) {
            String subject = String.format("Bill due tomorrow: %s", bill.getName());
            String body = String.format("Your bill %s is due on %s.", bill.getName(), dueDate);
            sendEmail(bill, subject, body);
            log.info(subject);
            return true;
        }
        return false;
    }

    private boolean handleDueToday(Bill bill, LocalDate today, LocalDate dueDate) {
        LocalDate adjustedDueDate = adjustForWeekend(dueDate);
        if (today.equals(adjustedDueDate)) {
            String subject = String.format("Bill due: %s", bill.getName());
            String body = String.format("Your bill %s is due today.", bill.getName());
            sendEmail(bill, subject, body);
            log.info(subject);
            return true;
        }
        return false;
    }

    private LocalDate calculateNextDueDate(Bill bill, LocalDate referenceDate) {
        int day = bill.getDueDate().getDayOfMonth();

        LocalDate dueDateThisMonth = LocalDate.of(referenceDate.getYear(), referenceDate.getMonth(),
                Math.min(day, referenceDate.lengthOfMonth()));
        LocalDate adjustedThisMonth = adjustForWeekend(dueDateThisMonth);

        if (!referenceDate.isAfter(adjustedThisMonth)) {
            return dueDateThisMonth;
        }

        LocalDate nextMonth = referenceDate.plusMonths(1);
        return LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(),
                Math.min(day, nextMonth.lengthOfMonth()));
    }

    private LocalDate adjustForWeekend(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return date;
    }

    private void sendEmail(Bill bill, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(bill.getEmail());
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send reminder for {}: {}", bill.getName(), e.getMessage());
        }
    }
}
