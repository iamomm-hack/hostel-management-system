@echo off
rem Drops and re-creates the hotel_management database with the demo data.
rem Asks for the MySQL root password. Restart the RMI server afterwards.
set "MYSQL_HOME=%USERPROFILE%\mysql-8.4.3-winx64"
"%MYSQL_HOME%\bin\mysql.exe" -u root -p --host=127.0.0.1 < database\hotel_management.sql
if errorlevel 1 (
    echo Database reset FAILED.
    exit /b 1
)
echo Database hotel_management was reset to the demo data.
