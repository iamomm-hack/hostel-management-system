@echo off
rem Starts the RMI server. MySQL must be running (start-mysql.bat) and the project compiled (compile.bat).
where java >nul 2>nul || if exist "C:\Program Files\Java\jdk-21.0.12\bin\java.exe" set "PATH=C:\Program Files\Java\jdk-21.0.12\bin;%PATH%"
where java >nul 2>nul || if exist "C:\Program Files\Java\latest\bin\java.exe" set "PATH=C:\Program Files\Java\latest\bin;%PATH%"
where java >nul 2>nul || set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"
java -Djava.rmi.server.hostname=127.0.0.1 -cp "out;lib/*" hotelmanagement.server.HotelServer
