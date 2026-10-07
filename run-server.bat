@echo off
rem Starts the RMI server. MySQL must be running (start-mysql.bat) and the project compiled (compile.bat).
where java >nul 2>nul || set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"
java -cp "out;lib/*" hotelmanagement.server.HotelServer
