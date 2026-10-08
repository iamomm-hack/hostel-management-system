@echo off
rem Starts one Swing client. The server must already be running (run-server.bat).
rem Optional argument: the server's IP address (default is localhost).
where java >nul 2>nul || if exist "C:\Program Files\Java\jdk-21.0.12\bin\java.exe" set "PATH=C:\Program Files\Java\jdk-21.0.12\bin;%PATH%"
where java >nul 2>nul || if exist "C:\Program Files\Java\latest\bin\java.exe" set "PATH=C:\Program Files\Java\latest\bin;%PATH%"
where java >nul 2>nul || set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"
java -cp "out;lib/*" hotelmanagement.client.HotelClient %1
