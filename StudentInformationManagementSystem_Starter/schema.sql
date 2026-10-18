CREATE DATABASE IF NOT EXISTS student_management;
USE student_management;

DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS marks;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS staff;

CREATE TABLE staff (
    id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    roll_no VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    email VARCHAR(120),
    address VARCHAR(255),
    edit_permission BOOLEAN DEFAULT FALSE
);

CREATE TABLE subjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    semester INT NOT NULL,
    code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL,
    credits DECIMAL(4,2) DEFAULT 0
);

CREATE TABLE marks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    cat1 DECIMAL(5,2) DEFAULT 0,
    cat2 DECIMAL(5,2) DEFAULT 0,
    sac1 DECIMAL(5,2) DEFAULT 0,
    sac2 DECIMAL(5,2) DEFAULT 0,
    sac3 DECIMAL(5,2) DEFAULT 0,
    sac4 DECIMAL(5,2) DEFAULT 0,
    sac5 DECIMAL(5,2) DEFAULT 0,
    UNIQUE KEY uq_marks (student_id, subject_id),
    FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE attendance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    att_date DATE NOT NULL,
    period INT NOT NULL,
    status ENUM('PRESENT','ABSENT') NOT NULL,
    UNIQUE KEY uq_attendance (student_id, subject_id, att_date, period),
    FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

INSERT INTO staff(staff_id,name,password)
VALUES ('STF001','CSE Staff','admin123');

INSERT INTO students(roll_no,name,department,year,mobile,email,address,edit_permission)
VALUES ('ES25CS21','Boopathi Vasan','CSE-A',2,'9000000000','student@example.com','Salem',TRUE);

INSERT INTO subjects(semester,code,name,type,credits) VALUES
(3,'23BS305','Discrete Mathematics','Theory',4.00),
(3,'23ES301','Digital System and Design','Theory',3.00),
(3,'23CS301','Data Structures','Theory',3.00),
(3,'23CS302','Object Oriented Programming','Theory',3.00),
(3,'23CS303','Database Management Systems','Theory',3.00),
(3,'23MC003','Indian Constitution','Theory',0.00),
(3,'23TPS03','Quantitative Aptitude and Logical Reasoning - I','Theory',1.00),
(3,'23CS304','Database Management System Laboratory','Practical',1.50),
(3,'23CS305','Data Structures Implementation Laboratory','Practical',1.50),
(3,'23CS306','Object Oriented Programming Laboratory','Practical',1.50);

-- Demo marks
INSERT INTO marks(student_id,subject_id,cat1,cat2,sac1,sac2,sac3,sac4,sac5)
SELECT st.id,s.id,63,0,19,20,0,0,0 FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23BS305';

INSERT INTO marks(student_id,subject_id,cat1,cat2,sac1,sac2,sac3,sac4,sac5)
SELECT st.id,s.id,62,0,19,20,0,0,0 FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23CS301';

INSERT INTO marks(student_id,subject_id,cat1,cat2,sac1,sac2,sac3,sac4,sac5)
SELECT st.id,s.id,70,75,18,19,17,0,0 FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23CS302';

-- Demo period-wise attendance
INSERT INTO attendance(student_id,subject_id,att_date,period,status)
SELECT st.id,s.id,'2026-09-15',1,'PRESENT' FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23CS301';
INSERT INTO attendance(student_id,subject_id,att_date,period,status)
SELECT st.id,s.id,'2026-09-15',2,'PRESENT' FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23CS301';
INSERT INTO attendance(student_id,subject_id,att_date,period,status)
SELECT st.id,s.id,'2026-09-16',1,'ABSENT' FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23CS301';
INSERT INTO attendance(student_id,subject_id,att_date,period,status)
SELECT st.id,s.id,'2026-09-16',2,'PRESENT' FROM students st,subjects s
WHERE st.roll_no='ES25CS21' AND s.code='23CS301';
