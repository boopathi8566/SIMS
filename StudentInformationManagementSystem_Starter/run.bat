@echo off
echo Student Information Management System
echo.
if exist "lib\mysql-connector-j-8.4.0.jar" (
  echo Connector found.
) else (
  echo MySQL Connector/J jar is missing.
  echo Run download-driver.ps1 in PowerShell first.
  pause
  exit /b 1
)
javac -cp "lib\mysql-connector-j-8.4.0.jar" -d out src\esec\sims\DB.java src\esec\sims\Main.java
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
java -cp "out;lib\mysql-connector-j-8.4.0.jar" esec.sims.Main
