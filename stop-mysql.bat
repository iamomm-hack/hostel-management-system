@echo off
rem Stops the MySQL server cleanly. Asks for the MySQL root password.
set "MYSQL_HOME=%USERPROFILE%\mysql-8.4.3-winx64"
"%MYSQL_HOME%\bin\mysqladmin.exe" -u root -p --host=127.0.0.1 shutdown
