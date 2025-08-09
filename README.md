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

## API

- `POST /bills` – register a new bill.
- `GET /bills` – list all bills.
- `GET /bills/paid?year=YYYY&month=MM` – list paid bills for a given month.
- `GET /bills/unpaid?year=YYYY&month=MM` – list unpaid bills for a given month.
- `GET /bills/reminder` – manually trigger due bill reminders.
- `POST /bills/{id}/paid` – mark a bill as paid so no reminder is sent.

## Web Interface

Static pages are available under `src/main/resources/static`:

- `create.html` – form for creating a bill.
- `paid.html` – view paid bills by month.
- `unpaid.html` – view unpaid bills by month with an option to mark them as paid.
