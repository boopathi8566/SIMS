# Student Information Management System - ESEC

## Stack
- Java 21
- Swing GUI
- MySQL
- JDBC
- VS Code

## Demo accounts
Staff:
- ID: STF001
- Password: admin123

Student:
- Roll No: ES25CS21
- Registered mobile: 9000000000

The mobile in the SQL is a DEMO value, not a real personal number.

## 1. Database setup
Open MySQL Workbench.
Open `schema.sql`.
Run the whole script.

## 2. MySQL password
Open `src/esec/sims/DB.java`.
Change:
USER = "root"
PASSWORD = "root"
to your own MySQL Workbench root username/password.

## 3. MySQL Connector/J
For the simplest VS Code setup, put MySQL Connector/J 8.4.x jar into `lib/`.
The included `pom.xml` can also download the dependency automatically if Maven is installed.

## 4. Run in VS Code
Recommended:
- Install Extension Pack for Java.
- Open the project folder.
- If Maven is installed: run `mvn compile exec:java`
- Without Maven, add the connector jar to the Java project's referenced libraries and run `Main.java`.

## Important project rules
- Only staff can add/update marks.
- Only staff can add/update attendance.
- Attendance is stored period-wise.
- Students can only view academic records.
- Student profile edit is controlled by staff permission.
- Subjects are semester-wise.
- Student dashboard calculates performance and attendance automatically.
- Pie/circle chart shows subject-wise performance.
