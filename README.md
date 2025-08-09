# Bills Reminder

This is a simple Spring Boot backend for registering monthly bills and sending email reminders on their due dates.

## Running

```
mvn spring-boot:run
```

Configure an SMTP server via the `spring.mail.*` properties. For local testing you can run a tool like [smtp4dev](https://github.com/rnwood/smtp4dev) and set `spring.mail.host` and `spring.mail.port` accordingly. When no mail server is available the application logs a warning instead of throwing an exception.

## Testing

```
mvn test
```
