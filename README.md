# Hotel Management System

A Java Servlet/JSP Maven web application for Whispering Pines Lodge, managing hotel rooms,
bookings, check-ins, and invoices.

## Requirements

- JDK 25 or later
- Maven 3.8 or later
- Apache Tomcat 9 (Servlet 4 / `javax.servlet`)
- No separate database installation required for local development

## Build

```powershell
mvn clean package
```

Deploy `target/hotel-management-system.war` to Tomcat 9. The room-management
screen lets you list, add, and edit rooms.

## Database setup

The app uses an embedded H2 database for local development. It creates the
`users`, `rooms`, and setup-state tables automatically on the first database
connection and persists data under `hotel-management-system/hotel_management`
in the home directory of the account running Tomcat. The database configuration is in
`src/main/resources/db.properties`. No MySQL setup or credentials are needed.

Open the app's `/setup` page and create the first administrator in the browser.
The one-time setup page is disabled after the first account is created.
Passwords are stored as salted PBKDF2 hashes.

The web app is available at `/hotel-management-system/`. Sign in with that
administrator account to manage rooms and approve customer reservation
requests. Customers can use `/hotel-management-system/booking` to search room
availability and submit booking requests. Requests are pending until approved
by the hotel; no online payment is processed. Set `hotel.contact.phone` in
`src/main/resources/db.properties` to display the hotel's public phone number.
When an administrator approves a request, the app sends a confirmation email
to the address provided by the guest. Configure SMTP using these environment
variables on the Tomcat process before starting it:

- `HOTEL_SMTP_HOST` (required)
- `HOTEL_SMTP_PORT` (optional; defaults to `587`)
- `HOTEL_SMTP_USERNAME` and `HOTEL_SMTP_PASSWORD` (both optional, but must be set together)
- `HOTEL_MAIL_FROM` (required; sender email address authorized by the SMTP provider)
- `HOTEL_MAIL_FROM_NAME` (optional; defaults to `Whispering Pines Lodge`)
- `HOTEL_SMTP_STARTTLS` (optional; defaults to `true`; set to `false` only when the SMTP provider requires it)

Keep the SMTP password in a protected environment variable or secret store;
do not commit it to the source code or `db.properties`. Restart Tomcat after
changing its environment. The reservation remains approved if mail delivery
fails, and the reservation inbox displays that the email was not sent.
Room management lets the administrator mark each room AC or Non-AC, and guests
can filter available rooms by that preference. Reservation requests can include
Indian menu selections; reception confirms food availability and pricing.
Existing rooms are migrated as Non-AC by default and should be checked in room
management if their actual facilities differ.
Check-in and invoice workflows have not been implemented yet.
