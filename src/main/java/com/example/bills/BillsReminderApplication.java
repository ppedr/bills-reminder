package com.example.bills;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillsReminderApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillsReminderApplication.class, args);
    }
}
