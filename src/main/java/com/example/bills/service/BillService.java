package com.example.bills.service;

import com.example.bills.model.Bill;
import com.example.bills.repository.BillRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    public BillService(BillRepository billRepository, JavaMailSender mailSender) {
        this.billRepository = billRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void sendDueBillsReminders() {
        List<Bill> dueBills = billRepository.findByDueDate(LocalDate.now());
        for (Bill bill : dueBills) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(bill.getEmail());
            message.setSubject("Bill due: " + bill.getName());
            message.setText("Your bill " + bill.getName() + " is due today.");
            try {
                mailSender.send(message);
            } catch (MailException e) {
                logger.warn("Failed to send reminder for {}: {}", bill.getName(), e.getMessage());
            }
            mailSender.send(message);
        }
    }
}
