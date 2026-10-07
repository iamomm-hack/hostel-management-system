# Distributed Hotel Management System using Java RMI and JDBC

**Advanced Java Mini-Project Report - 3rd Year, Computer Science and Engineering**

---

## 1. Abstract

This project is a distributed hotel management system written in Java. The
application is divided into a server and several clients. The server holds all
business logic and is the only program that accesses the MySQL database, using
JDBC. The clients are Java Swing desktop programs for three kinds of users:
customers, receptionists and administrators. Clients communicate with the
server through Java Remote Method Invocation (RMI). The system supports
customer registration, room search, booking with protection against double
booking, check-in and check-out, additional services, bill generation with GST,
payment recording and basic hotel statistics.

## 2. Introduction

A hotel is used by several people at the same time: guests who want to book
rooms, front-desk staff who check guests in and out, and managers who maintain
rooms and review income. All of them must see the same, current data. A single
standalone program on one computer cannot do this, so the application has to be
distributed: many client programs sharing one server and one database.

Java RMI allows a Java program to call a method of an object that lives in
another Java Virtual Machine as if it were a local object. JDBC is the standard
Java API for relational databases. This project combines the two in a
three-tier architecture.

## 3. Problem Statement

Design and implement a hotel management system in which multiple clients on a
network can register customers, search and book rooms, manage stays and
generate bills through a central server, such that:

- clients never access the database directly,
- the same room can never be booked by two customers for overlapping dates,
- each type of user sees only the functions meant for that role.

## 4. Objectives

1. Apply Java RMI to build a working client-server application.
2. Apply JDBC with `PreparedStatement`, transactions and proper resource handling.
3. Design a normalised relational database with primary keys, foreign keys and constraints.
4. Build a simple, usable Swing interface for three user roles.
5. Enforce booking rules on the server and handle errors with clear messages.

## 5. Existing System

Small hotels often keep bookings in paper registers or spreadsheets, or use a
single-computer program.

- Only one person can work with the data at a time.
- Double booking happens easily because availability is checked by eye.
- Bills are calculated by hand, so mistakes in service charges and tax are common.
- There is no quick way to see how many rooms are free or how much revenue was collected.

## 6. Proposed System

A central RMI server with a MySQL database, and Swing clients for each role.

- All clients work on the same live data.
- The server checks availability inside a database transaction before every booking.
- The bill is calculated by the server from the stored room price, the services used and GST.
- Room status changes automatically with booking, check-in, check-out and cancellation.
- The admin can see room counts, the number of bookings and total revenue at any time.

## 7. Methodology

The project was built in small phases, and each phase was compiled and tested
before the next one started:

1. Project structure and VS Code configuration
2. Database schema and demo data
3. Model classes
4. JDBC connection and data-access classes
5. RMI remote interfaces
6. Server implementations and the RMI server
7. Console RMI test client
8. Swing screens: login and registration, customer, receptionist, admin
9. Integration testing, clean-up and documentation

## 8. System Architecture

```
+---------------------------+        +---------------------------+        +-----------+
|  Swing Client             |  RMI   |  RMI Server               |  JDBC  |  MySQL    |
|  LoginFrame               | -----> |  AuthServiceImpl          | -----> |  hotel_   |
|  CustomerFrame            |  port  |  RoomServiceImpl          |        |  manage-  |
|  ReceptionistFrame        |  1099  |  BookingServiceImpl       |        |  ment     |
|  AdminFrame               | <----- |  CustomerServiceImpl      | <----- |           |
|  (uses remote interfaces) |        |  PaymentServiceImpl       |        |           |
+---------------------------+        |  DAO classes, DBConnection|        +-----------+
                                     +---------------------------+
```

**Flow of one request (booking a room)**

1. The customer selects a room and presses *Book Selected Room*.
2. The client calls `BookingService.createBooking(...)` on its RMI stub.
3. RMI serialises the arguments and sends them to the server.
4. `BookingServiceImpl` validates the dates, opens a JDBC connection and starts a transaction.
5. It locks the room row, checks for overlapping bookings, inserts the booking, updates the room status and commits.
6. The `Booking` object is serialised back to the client, which shows the booking ID.
7. If a rule is broken the server throws `HotelException`, and the client shows its message in a dialog.

**Packages**

| Package | Contents |
|---|---|
| `hotelmanagement.model` | Serializable data classes and enums |
| `hotelmanagement.remote` | Remote interfaces, `HotelException`, `ServiceNames` |
| `hotelmanagement.database` | `DBConnection` and DAO classes containing all SQL |
| `hotelmanagement.server` | `HotelServer`, service implementations, `BillCalculator` |
| `hotelmanagement.client` | Swing screens, `RemoteServices`, `RmiTestClient` |

## 9. Modules

| Module | Description | Remote interface |
|---|---|---|
| Authentication | Login for all roles, customer self-registration, SHA-256 password hashing | `AuthService` |
| Room management | List, search available rooms, add / update / delete, maintenance status | `RoomService` |
| Customer management | Walk-in registration, list, search by name or phone | `CustomerService` |
| Booking | Create and cancel bookings with overlap check | `BookingService` |
| Check-in / check-out | Status changes of booking and room | `BookingService` |
| Billing and payment | Additional services, bill calculation, payment recording | `PaymentService` |
| Admin statistics | Room counts, booking count, total revenue | `PaymentService` |

**User roles**

| Role | Screens |
|---|---|
| Customer | Search & Book Rooms, My Bookings |
| Receptionist | Bookings, Customers, Book a Room, Rooms |
| Admin | Statistics, Rooms, Customers, Bookings, Payments |

## 10. Database Design

Database: `hotel_management`

| Table | Main columns | Purpose |
|---|---|---|
| `users` | user_id (PK), username (unique), password (SHA-256), role | login accounts |
| `customers` | customer_id (PK), user_id (FK, unique, nullable), full_name, phone, email, address, id_proof | guests; user_id is NULL for walk-ins |
| `rooms` | room_id (PK), room_number (unique), room_type, price, capacity, status | hotel rooms |
| `bookings` | booking_id (PK), customer_id (FK), room_id (FK), check_in, check_out, number_of_guests, booking_status, booking_date | reservations |
| `services` | service_id (PK), service_name (unique), price | additional services |
| `service_usage` | usage_id (PK), booking_id (FK), service_id (FK), quantity, usage_date | services used during a stay |
| `payments` | payment_id (PK), booking_id (FK, unique), room_charges, service_charges, gst_amount, total_amount, payment_method, payment_date | final payments |

**Relationships**

```
users      1 --- 0..1 customers
customers  1 --- N    bookings
rooms      1 --- N    bookings
bookings   1 --- N    service_usage
services   1 --- N    service_usage
bookings   1 --- 0..1 payments
```

**Constraints and indexes**

- `CHECK (check_out > check_in)`, `CHECK (number_of_guests > 0)`, `CHECK (price > 0)`, `CHECK (capacity > 0)`, `CHECK (quantity > 0)`
- ENUM columns for role, room type, room status, booking status and payment method
- Index on `bookings (room_id, check_in, check_out)` for the availability query, on `bookings (booking_status)`, and on customer name and phone for search

**Status values**

- Room: `AVAILABLE -> BOOKED -> OCCUPIED -> AVAILABLE`, plus `MAINTENANCE`
- Booking: `CONFIRMED -> CHECKED_IN -> CHECKED_OUT`, or `CONFIRMED -> CANCELLED`

## 11. Implementation

**RMI.** Each remote interface extends `java.rmi.Remote` and every method
declares `RemoteException`. Each implementation extends `UnicastRemoteObject`.
`HotelServer` starts the registry with `LocateRegistry.createRegistry(1099)`
and binds the five objects with `registry.rebind(name, object)`. The client
obtains stubs with `registry.lookup(name)`. All model classes implement
`Serializable` so they can travel between the JVMs.

**JDBC.** `DBConnection` reads `database/db.properties` and returns
connections from `DriverManager`. All SQL is in the DAO classes and uses
`PreparedStatement` with `?` parameters. Connections, statements and result
sets are closed with try-with-resources.

**Preventing double booking.** RMI serves each client call in its own thread,
so two bookings for the same room can arrive together. `createBooking` is a
`synchronized` method, and inside a transaction it locks the room row with
`SELECT ... FOR UPDATE`, checks for an overlapping booking and only then
inserts. Two stays overlap when

```
existing.check_in < new.check_out  AND  existing.check_out > new.check_in
```

Only `CONFIRMED` and `CHECKED_IN` bookings are counted, so a cancelled booking
frees its dates.

**Transactions.** Registration (user + customer), booking, cancellation,
check-in and check-out each change more than one row. They run with
`setAutoCommit(false)` and end with `commit()`, or `rollback()` if anything fails.

**Billing.** `BillCalculator` computes on the server:

```
Room Charges    = number of nights x room price
Service Charges = sum of (service price x quantity)
GST             = 12% of (Room Charges + Service Charges)
Final Amount    = Room Charges + Service Charges + GST
```

At check-out the amounts are stored in `payments` together with the payment
method. No real payment gateway is used.

**Error handling.** Business errors are thrown as `HotelException` with a
message for the user. `SQLException` is logged on the server console and
converted to a general message, so SQL details never reach the client. On the
client, `ClientUtil.showError` shows a dialog; a `RemoteException` becomes
"Cannot reach the hotel server".

**User interface.** Swing with `JFrame`, `JTabbedPane`, `JTable`,
`JTextField`, `JComboBox` and `JOptionPane`. One common `DashboardFrame` holds
the header and tabs; each role adds its own tabs. A tab reloads its data from
the server every time it is opened.

## 12. Testing

| Test | How | Result |
|---|---|---|
| JDBC connection | `DBConnectionTest` connects and counts the rows of all 7 tables | Passed |
| Server functions through RMI | `RmiTestClient` runs 67 checks: login, registration, search, booking rules, check-in, services, bill arithmetic, check-out, customers, room administration, statistics | 67 passed, 0 failed |
| Concurrent booking | 5 threads book the same room for the same dates at once (part of `RmiTestClient`) | Exactly 1 succeeded, 4 rejected |
| GUI | Automated click-through of the screens of all three roles (login, registration, booking, cancel, check-in, add service, check-out, room add / update / delete, status change), 45 checks | 45 passed, 0 failed |

**Sample test cases**

| Case | Input | Expected and actual result |
|---|---|---|
| Invalid login | admin / wrong | "Invalid username or password." |
| Duplicate username | register an existing username | "Username '...' is already taken." |
| Invalid dates | check-out equal to check-in | "Check-out date must be after the check-in date." |
| Past date | check-in yesterday | "Check-in date cannot be in the past." |
| Overlapping booking | same room, overlapping dates | "Room 201 is already booked for the selected dates." |
| Back-to-back booking | new check-in equals old check-out | Booking accepted |
| Too many guests | 9 guests in a double room | "Room 201 can hold only 2 guest(s)." |
| Early check-in | check-in before the booked date | "Check-in is allowed only from <date>." |
| Bill | 2 nights x 1500, Breakfast x2, Laundry x1 | 3000 + 800 + 456 GST = 4256 |
| Delete room with bookings | delete room 201 | Refused, advises MAINTENANCE |

Screenshots of the running application are in `docs/screenshots/`.

## 13. Advantages

- Several users can work at the same time on the same data.
- The database is reachable only from the server, so credentials and SQL are in one place.
- Double booking is prevented by the server, not left to the user.
- Bills are calculated automatically and consistently.
- Plain Java only: easy to install, run and understand.

## 14. Limitations

- Role restrictions are applied in the client screens. The server does not
  verify the identity of the caller on each remote call (there is no session token).
- RMI communication is not encrypted, and passwords are hashed with plain SHA-256 without a salt.
- A new database connection is opened for each request; there is no connection pool.
- Dates are typed as text (`YYYY-MM-DD`); there is no calendar picker.
- Payment is only recorded. There is no payment gateway, partial payment or refund.
- The GST rate is fixed at 12% in the code.
- Receptionist and admin accounts can be created only with SQL.
- If a guest stays beyond the booked check-out date, the system does not detect it automatically.

## 15. Future Scope

- Session tokens so that the server checks the role on every call
- Salted password hashing (for example PBKDF2) and RMI over SSL
- Connection pooling
- Date picker, printable or PDF invoice
- Admin screens for staff accounts and for editing service prices
- Reports by date range and by room type
- Web or mobile client using the same server logic

## 16. Conclusion

The project shows how Java RMI and JDBC can be combined to build a working
distributed application. The three-tier design keeps the user interface,
business logic and data storage separate. The server enforces the important
rules of a hotel, in particular that a room cannot be double booked, and
produces correct bills. All planned features for the three roles were
implemented and tested.
