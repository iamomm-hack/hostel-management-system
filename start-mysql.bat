@echo off
rem Starts the MySQL server installed in the user profile (it is not a Windows service,
rem so it must be started once after every restart of the computer).
set "MYSQL_HOME=%USERPROFILE%\mysql-8.4.3-winx64"

"%MYSQL_HOME%\bin\mysqladmin.exe" -u hotel_user -photel123 --host=127.0.0.1 ping >nul 2>nul
if not errorlevel 1 (
    echo MySQL is already running.
    exit /b 0
)

echo Starting MySQL...
start "MySQL Server" /min "%MYSQL_HOME%\bin\mysqld.exe" --defaults-file="%MYSQL_HOME%\my.ini" --console

rem wait until the server answers (up to about 30 seconds)
for /l %%i in (1,1,30) do (
    "%MYSQL_HOME%\bin\mysqladmin.exe" -u hotel_user -photel123 --host=127.0.0.1 ping >nul 2>nul
    if not errorlevel 1 (
        echo MySQL is running on port 3306.
        exit /b 0
    )
    timeout /t 1 /nobreak >nul
)
echo MySQL did not start. Check the "MySQL Server" window for errors.
exit /b 1
