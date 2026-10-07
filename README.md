# Distributed Hotel Management System using Java RMI and JDBC

3rd-year CSE Advanced Java mini-project.

## Objective

To build a hotel management application in which several client programs
(customer, receptionist, admin) share one central server. The clients call the
server's methods remotely with **Java RMI**, and only the server talks to the
**MySQL** database through **JDBC**.

## Features

**Customer**
- Register and log in
- Search available rooms by dates, number of guests and room type
- View room details and book a room
- View own bookings and their status, cancel a booking
- View the bill of a booking

**Receptionist**
- Log in
- Register walk-in customers, search customers by name or phone
- Search available rooms and create bookings for any customer
- View all bookings, check guests in and out
- Add additional services (Room Service, Laundry, Breakfast, Airport Pickup)
- Generate the final bill and record the payment (CASH / CARD / UPI)
- Put a room under maintenance and back in service

**Admin**
- Log in
- Add, update and delete rooms, change room status
- View customers, bookings and payments
- View statistics: total / available / booked / occupied / maintenance rooms,
  number of bookings, total revenue

**Rules enforced by the server**
- A room cannot be booked twice for overlapping dates, even if two clients try at the same moment
- Check-in date cannot be in the past, check-out must be after check-in
- Guests cannot exceed the room capacity, maintenance rooms cannot be booked
- Bill = Room Charges (nights x price) + Service Charges + 12% GST

## Architecture

```
Customer / Receptionist / Admin Client (Swing)
                |
             Java RMI   (port 1099)
                |
           RMI Server   (service implementations)
                |
              JDBC
                |
             MySQL      (database hotel_management)
```

The client contains no SQL and no database password. It only knows the five
remote interfaces in `hotelmanagement.remote`.

## Technologies

| Technology | Used for |
|---|---|
| Java (JDK 21) | whole application |
| Java RMI | client-server communication |
| JDBC + MySQL Connector/J 8.4.0 | database access on the server |
| MySQL 8.4 | data storage |
| Java Swing | client user interface |

No Maven, no frameworks.

## Folder structure

```
.vscode/                  VS Code Java settings and run configurations
lib/mysql-connector-j.jar MySQL JDBC driver
database/
    hotel_management.sql  creates the database, tables and demo data
    db.properties         database URL, user and password (server only)
src/hotelmanagement/
    model/                Serializable classes sent over RMI (Room, Booking, Bill, ...)
    remote/               RMI remote interfaces, HotelException, ServiceNames
    database/             DBConnection and the DAO classes (all SQL is here)
    server/               HotelServer and the five service implementations
    client/               Swing screens and the RMI test client
out/                      compiled .class files
docs/                     viva material and screenshots
compile.bat, run-server.bat, run-client.bat
start-mysql.bat, stop-mysql.bat, reset-database.bat
```

## Database setup

**On this laptop** MySQL 8.4.3 is already set up in
`C:\Users\RUPSHA\mysql-8.4.3-winx64` and the database is created. It is not a
Windows service, so start it once after every restart of the computer:

```
.\start-mysql.bat
```

**On another computer**
1. Install MySQL Server 8.x.
2. Run the script as root (it also creates the `hotel_user` account the server uses):
   ```
   mysql -u root -p < database/hotel_management.sql
   ```
3. If MySQL is not on `localhost:3306`, edit `database/db.properties`.

To go back to the clean demo data at any time: `.\reset-database.bat`
(asks for the MySQL root password).

## JDBC setup

- The driver jar is `lib/mysql-connector-j.jar`. It is on the classpath through
  `-cp "out;lib/*"` and through `java.project.referencedLibraries` in `.vscode/settings.json`.
- Connection settings are in `database/db.properties` and are read only by
  `hotelmanagement.database.DBConnection`.
- Test the connection: `java -cp "out;lib/*" hotelmanagement.database.DBConnectionTest`

## RMI setup

- The server creates the RMI registry itself on port **1099**
  (`LocateRegistry.createRegistry`), so the `rmiregistry` tool is not needed.
- It binds five remote objects: `AuthService`, `RoomService`, `BookingService`,
  `CustomerService`, `PaymentService`.
- The client looks them up once in `hotelmanagement.client.RemoteServices`.

## How to compile

Open the project folder in VS Code and use the terminal. `java` and `javac`
come from the JDK at `C:\Program Files\BlueJ\jdk` (configured in `.vscode/settings.json`).

```
.\compile.bat
```

This is the same as compiling every file under `src` with:

```
javac -cp "lib/*" -d out <all .java files under src>
```

## How to run

Use three terminals, all in the project folder.

```
1.  .\start-mysql.bat          (only if MySQL is not running yet)
2.  .\run-server.bat           (keep this terminal open)
3.  .\run-client.bat           (open as many clients as you like)
```

The same without the scripts:

```
java -cp "out;lib/*" hotelmanagement.server.HotelServer
java -cp "out;lib/*" hotelmanagement.client.HotelClient
```

In VS Code you can also use **Run and Debug** and pick "1. RMI Server" and then "2. Swing Client".

To test all server functions without the GUI (server must be running):

```
java -cp "out;lib/*" hotelmanagement.client.RmiTestClient
```

It prints PASS/FAIL for each check. It adds some test data; run `reset-database.bat` afterwards.

## Demo credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Receptionist | `reception` | `reception123` |
| Customer | `rahul` | `rahul123` |
| Customer | `priya` | `priya123` |

MySQL accounts on this laptop: `root` / `root123` (administration) and
`hotel_user` / `hotel123` (used by the server).

## Suggested demo flow

1. Start the server and two clients.
2. Client 1: register a new customer, log in, search rooms, book one, open "My Bookings".
3. Client 2: log in as `reception`. The new booking is in the "Bookings" tab.
   Select it and use Check-In, Add Service, then Check-Out & Bill.
4. Client 1: press Refresh in "My Bookings" and open View Bill. The bill is now PAID.
5. Log in as `admin`: Statistics, Rooms (add/update/delete), Payments.
6. Double-booking: in both clients search the same dates, book the same room in
   client 1, then try to book it in client 2. The server rejects the second one.

## Troubleshooting

| Problem | Fix |
|---|---|
| `'java' is not recognized` | Use the VS Code terminal (reopen it once), or use the `.bat` files, which find the JDK themselves. |
| Server prints `Cannot connect to MySQL` | Run `.\start-mysql.bat`, then check `database/db.properties`. |
| Server prints `Cannot read database/db.properties` | Start the server from the project folder, not from `src` or `out`. |
| Server prints `Port already in use: 1099` | Another server is still running. Close it (Ctrl+C in its terminal). |
| Client says `Cannot connect to the hotel server` | Start the server first. |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | The classpath must include `lib/*`: `-cp "out;lib/*"`. |
| Client on another computer cannot connect | Run the client with the server's IP (`run-client.bat 192.168.x.x`), start the server with `-Djava.rmi.server.hostname=<server IP>`, and allow Java through the Windows firewall on the server. |
| Data looks messy after testing | Run `.\reset-database.bat`. |
