# Viva Preparation

Distributed Hotel Management System using Java RMI and JDBC

## 1. Explain the project in one minute

"It is a hotel management system with a client-server design. The clients are
Swing programs for customers, receptionists and the admin. They do not touch
the database. They call methods on the server using Java RMI. The server has
five remote objects: AuthService, RoomService, BookingService, CustomerService
and PaymentService. The server uses JDBC to read and write a MySQL database
with seven tables. The server makes sure a room cannot be double booked and
calculates the bill with services and GST."

## 2. Where to find things in the code

| Topic | File |
|---|---|
| Starting the registry and binding | `server/HotelServer.java` |
| A remote interface | `remote/BookingService.java` |
| A remote implementation | `server/BookingServiceImpl.java` |
| Client lookup of stubs | `client/RemoteServices.java` |
| JDBC connection | `database/DBConnection.java` |
| SQL with PreparedStatement | `database/BookingDAO.java`, `database/RoomDAO.java` |
| Overlap check | `BookingDAO.hasOverlappingBooking` |
| Transaction + row lock | `BookingServiceImpl.createBooking` |
| Bill formula | `server/BillCalculator.java` |
| Password hashing | `ServerUtil.hashPassword` |
| Error dialogs | `client/ClientUtil.showError` |
| Role-based screens | `client/CustomerFrame`, `ReceptionistFrame`, `AdminFrame` |

## 3. RMI questions

**What is RMI?**
Remote Method Invocation. It lets an object in one JVM call methods of an
object in another JVM, possibly on another computer, using normal method call syntax.

**What are stub and skeleton?**
The stub is the client-side proxy that implements the remote interface. It
marshals the arguments, sends them to the server and unmarshals the result.
The skeleton was the server-side counterpart. Since Java 5 stubs are generated
at run time (dynamic proxies) and no separate skeleton class or `rmic` is needed.

**What is the RMI registry?**
A simple naming service. The server binds a remote object under a name; the
client looks up the name and receives the stub. Default port 1099. In this
project the server starts it itself with `LocateRegistry.createRegistry(1099)`.

**What must a remote interface do?**
Extend `java.rmi.Remote`, and every method must declare `throws RemoteException`.

**Why does the implementation extend `UnicastRemoteObject`?**
Its constructor exports the object, which means it starts listening for
incoming calls on a port and makes a stub available. The constructor must
declare `RemoteException`.

**`bind` vs `rebind`?**
`bind` throws `AlreadyBoundException` if the name is taken. `rebind` replaces
the old binding. The project uses `rebind`.

**Why do the model classes implement `Serializable`?**
Arguments and return values are sent over the network by value. RMI uses Java
serialization to convert them to bytes, so they must be `Serializable`.

**What is `serialVersionUID`?**
A version number of a serializable class. Sender and receiver must have the
same value, otherwise `InvalidClassException` is thrown.

**What is marshalling?**
Converting method arguments or results into a byte stream for the network.
Unmarshalling is the reverse.

**When is `RemoteException` thrown?**
For communication problems: server not running, network failure, or a
serialization error.

**Is RMI multithreaded?**
Yes. The RMI runtime handles each client call in a separate thread, so the
server code must be thread-safe. That is why `createBooking` is `synchronized`
and uses a database lock.

**How does a business error reach the client?**
The server throws `HotelException`, a checked exception declared in the remote
interface. RMI serializes it and rethrows it in the client.

**Pass by value or by reference?**
Serializable objects are passed by value (a copy). Remote objects are passed by reference (a stub).

**RMI vs sockets?**
With sockets the programmer designs the message format and parses it. RMI
hides that behind method calls and handles objects automatically.

**RMI vs REST / web services?**
RMI works only between Java programs and uses a binary protocol. REST uses
HTTP and JSON and works with any language.

## 4. JDBC questions

**What is JDBC?**
Java Database Connectivity: the standard Java API to connect to a relational
database, send SQL and read results.

**Steps to use JDBC?**
1. Have the driver on the classpath. 2. Get a `Connection` from
`DriverManager.getConnection(url, user, password)`. 3. Create a
`PreparedStatement`. 4. Set parameters and execute. 5. Read the `ResultSet`.
6. Close everything.

**Which driver type is Connector/J?**
Type 4: a pure Java driver that speaks the MySQL network protocol directly.

**Is `Class.forName("com.mysql.cj.jdbc.Driver")` needed?**
No. Since JDBC 4 the driver is loaded automatically when its jar is on the classpath.

**`Statement` vs `PreparedStatement`?**
`PreparedStatement` is precompiled and takes parameters with `?`. It prevents
SQL injection because user input is sent as data, never as part of the SQL
text, and it is faster when reused.

**What is SQL injection?**
When user input is concatenated into SQL, a user can type SQL that changes
the query, for example `' OR '1'='1`. Parameters prevent this.

**`executeQuery` vs `executeUpdate`?**
`executeQuery` is for SELECT and returns a `ResultSet`. `executeUpdate` is for
INSERT, UPDATE and DELETE and returns the number of affected rows.

**How do you get the auto-generated ID?**
Prepare the statement with `Statement.RETURN_GENERATED_KEYS` and read `getGeneratedKeys()`.

**What is a transaction? What is ACID?**
A group of statements that must all succeed or all fail. Atomicity,
Consistency, Isolation, Durability. In JDBC: `setAutoCommit(false)`, then
`commit()` or `rollback()`.

**Where are transactions used here?**
Registration (user and customer rows), booking, cancellation, check-in and
check-out (payment, booking status and room status together).

**What is try-with-resources?**
`try (Connection con = ...) { }` closes the resource automatically at the end
of the block, even when an exception is thrown.

**What does `SELECT ... FOR UPDATE` do?**
It locks the selected rows until the transaction ends, so another transaction
that wants the same row has to wait.

**Where are the database credentials?**
Only in `database/db.properties`, read by `DBConnection` on the server. The client has none.

## 5. Project-specific questions

**How do you prevent double booking?**
On the server, in `createBooking`: the method is `synchronized`; inside a
transaction the room row is locked with `FOR UPDATE`; then
`hasOverlappingBooking` runs; only if it finds nothing is the booking inserted.
A test with 5 simultaneous requests gives exactly 1 success.

**What is the overlap condition?**
`existing.check_in < new.check_out AND existing.check_out > new.check_in`.
A booking that starts on the day another one ends does not overlap.

**Why both `synchronized` and a database lock?**
`synchronized` protects within this one server JVM. The row lock would still
protect the data if a second server or another program used the same database.

**Why does the client not connect to MySQL directly?**
Security (no database password on client machines), one place for the business
rules, and the database can change without changing the clients.

**How is the bill calculated?**
Nights x room price, plus the sum of services, plus 12% GST on both. Done in
`BillCalculator` on the server. After check-out the stored payment amounts are shown.

**How does the room status change?**
`RoomDAO.refreshStatusFromBookings` sets OCCUPIED if the room has a
CHECKED_IN booking, BOOKED if it has a CONFIRMED booking, otherwise AVAILABLE.
It is called after booking, cancelling, check-in and check-out. MAINTENANCE is set manually.

**A room shows BOOKED. Can it still be booked for other dates?**
Yes. The status shows the current situation. Availability for a date range is
decided by the overlap query on the bookings table.

**How are passwords stored?**
As a SHA-256 hash. At login the typed password is hashed and compared with the stored hash.

**How are roles handled?**
`users.role` is CUSTOMER, RECEPTIONIST or ADMIN. After login the client opens
the frame for that role, and each frame shows only its own tabs and buttons.

**Why can a room with bookings not be deleted?**
`bookings.room_id` is a foreign key to `rooms`. Deleting the room would break
the booking history, so the server refuses and suggests MAINTENANCE.

**Why is there a `customers` table separate from `users`?**
Walk-in customers have no login. `customers.user_id` is NULL for them. Staff
have a user row but no customer row.

**What is a DAO?**
Data Access Object: a class that contains the SQL for one table, so the
service classes do not contain SQL.

**What happens if the server stops while a client is open?**
The next remote call throws `RemoteException` and the client shows "Cannot reach the hotel server".

**Can clients run on other computers?**
Yes: `run-client.bat <server IP>`. The server should be started with
`-Djava.rmi.server.hostname=<server IP>` and Java allowed through the firewall.

## 6. Limitations to state honestly if asked

- The server trusts the client for the role; there is no session token per call.
- No encryption of RMI traffic; SHA-256 without salt.
- No connection pool.
- No real payment gateway.
- Dates are typed as text.

## 7. Demo checklist

1. `start-mysql.bat` (if MySQL is not running)
2. `run-server.bat` and show the "[OK] Bound ..." lines
3. `run-client.bat` twice
4. Customer: register, search, book, My Bookings
5. Receptionist (`reception` / `reception123`): Check-In, Add Service, Check-Out & Bill
6. Customer: Refresh, View Bill shows PAID
7. Admin (`admin` / `admin123`): Statistics, add a room, Payments
8. Double booking attempt from two clients
9. Show the server console log lines for each action
10. Optional: `java -cp "out;lib/*" hotelmanagement.client.RmiTestClient`, then `reset-database.bat`
