@echo off
rem Compiles every .java file under src\ into out\
setlocal enabledelayedexpansion

rem Use the BlueJ JDK if javac is not already on the PATH
where javac >nul 2>nul || set "PATH=C:\Program Files\BlueJ\jdk\bin;%PATH%"

if not exist out mkdir out

rem Build a list of source files (relative paths, forward slashes)
(for /r src %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:%CD%\=!"
    echo !p:\=/!
)) > out\sources.txt

javac -cp "lib/*" -d out @out\sources.txt
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED
    exit /b 1
)
echo Compilation successful. Class files are in out\
