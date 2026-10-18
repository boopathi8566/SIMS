USE student_management;

-- ============================================================
-- SIMS Review 2 / Phase 1 Migration
-- Adds Administrator + Faculty Assignment + Timetable + Batches
-- SAFE MIGRATION: does NOT drop existing students, subjects,
-- marks or attendance records.
-- ============================================================

-- 1) Administrator accounts
CREATE TABLE IF NOT EXISTS administrators (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- 2) Account status for existing staff accounts
ALTER TABLE staff
    ADD COLUMN active BOOLEAN DEFAULT TRUE;

-- 3) Batch for students (needed for practical/lab timetable)
ALTER TABLE students
    ADD COLUMN batch VARCHAR(10) DEFAULT 'I';

-- 4) Staff-to-subject assignment
CREATE TABLE IF NOT EXISTS staff_subjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    staff_id INT NOT NULL,
    subject_id INT NOT NULL,
    UNIQUE KEY uq_staff_subject (staff_id, subject_id),
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- 5) Timetable
-- day_of_week: MONDAY ... SATURDAY
-- batch: ALL for theory, I/II for batch-specific practicals
CREATE TABLE IF NOT EXISTS timetable (
    id INT PRIMARY KEY AUTO_INCREMENT,
    semester INT NOT NULL,
    department VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    day_of_week VARCHAR(12) NOT NULL,
    period INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    subject_id INT NULL,
    activity_name VARCHAR(150) NULL,
    batch VARCHAR(10) NOT NULL DEFAULT 'ALL',
    UNIQUE KEY uq_timetable (semester, department, section, day_of_week, period, batch),
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT
);

-- 6) Attendance supports OD as requested.
ALTER TABLE attendance
    MODIFY COLUMN status ENUM('PRESENT','ABSENT','OD') NOT NULL;

-- ============================================================
-- DEMO ADMIN LOGIN
-- Admin ID: ADM001
-- Password: admin123
-- ============================================================
INSERT INTO administrators(admin_id, name, password, active)
SELECT 'ADM001', 'System Administrator', 'admin123', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM administrators WHERE admin_id='ADM001'
);

-- Keep existing staff active by default.
UPDATE staff SET active=TRUE WHERE active IS NULL;

-- Existing demo student belongs to Batch I.
UPDATE students SET batch='I' WHERE roll_no='ES25CS21' AND (batch IS NULL OR batch='');

-- ============================================================
-- STAFF DEMO ACCOUNTS
-- These are only starter accounts for testing the new role system.
-- Admin can later edit/disable them from the application.
-- ============================================================
INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_DM', 'Dr. G. Mahendrakumar', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_DM');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_DSD', 'Dr. J. Vijay Franklin', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_DSD');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_DS', 'Mr. G. Dhanasekaran', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_DS');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_OOP', 'Mr. D. Vaduganathan', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_OOP');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_DBMS', 'Mr. M. Sakthivel', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_DBMS');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_IC', 'Dr. D. Ambika', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_IC');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_QALR', 'New Trainer - T&P', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_QALR');

INSERT INTO staff(staff_id, name, password, active)
SELECT 'STF_DBMSLAB', 'Ms. P. Briskilla', 'staff123', TRUE
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE staff_id='STF_DBMSLAB');

-- ============================================================
-- STAFF-SUBJECT ASSIGNMENTS
-- ============================================================
INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_DM' AND su.code='23BS305';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_DSD' AND su.code='23ES301';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_DS' AND su.code='23CS301';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_OOP' AND su.code='23CS302';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_DBMS' AND su.code='23CS303';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_IC' AND su.code='23MC003';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_QALR' AND su.code='23TPS03';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_DBMSLAB' AND su.code='23CS304';

-- The DS and OOP faculty also handle their respective labs.
INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_DS' AND su.code='23CS305';

INSERT IGNORE INTO staff_subjects(staff_id, subject_id)
SELECT st.id, su.id FROM staff st, subjects su
WHERE st.staff_id='STF_OOP' AND su.code='23CS306';

-- ============================================================
-- CLEAR/REBUILD ONLY TIMETABLE DATA
-- This table is new, so this is safe.
-- ============================================================
DELETE FROM timetable;

-- Helper pattern used below: subject is resolved by code.
-- Monday
INSERT INTO timetable
(semester,department,section,day_of_week,period,start_time,end_time,subject_id,batch)
SELECT 3,'CSE','A','MONDAY',1,'09:30:00','10:20:00',id,'ALL' FROM subjects WHERE code='23ES301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','MONDAY',2,'10:20:00','11:10:00',id,'ALL' FROM subjects WHERE code='23ES301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','MONDAY',3,'11:25:00','12:15:00',id,'ALL' FROM subjects WHERE code='23TPS03';
INSERT INTO timetable SELECT NULL,3,'CSE','A','MONDAY',4,'12:15:00','13:05:00',id,'ALL' FROM subjects WHERE code='23TPS03';
INSERT INTO timetable SELECT NULL,3,'CSE','A','MONDAY',5,'14:00:00','14:50:00',id,'ALL' FROM subjects WHERE code='23CS303';
INSERT INTO timetable
(semester,department,section,day_of_week,period,start_time,end_time,subject_id,activity_name,batch)
VALUES (3,'CSE','A','MONDAY',6,'14:50:00','15:40:00',NULL,'Library (LIB)','ALL');
INSERT INTO timetable SELECT NULL,3,'CSE','A','MONDAY',7,'15:40:00','16:30:00',id,'ALL' FROM subjects WHERE code='23BS305';

-- Tuesday
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',1,'09:30:00','10:20:00',id,'ALL' FROM subjects WHERE code='23CS302';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',2,'10:20:00','11:10:00',id,'ALL' FROM subjects WHERE code='23CS303';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',3,'11:25:00','12:15:00',id,'ALL' FROM subjects WHERE code='23CS301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',4,'12:15:00','13:05:00',id,'ALL' FROM subjects WHERE code='23BS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',5,'14:00:00','14:50:00',id,'I' FROM subjects WHERE code='23CS306';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',6,'14:50:00','15:40:00',id,'I' FROM subjects WHERE code='23CS306';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',7,'15:40:00','16:30:00',id,'I' FROM subjects WHERE code='23CS306';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',5,'14:00:00','14:50:00',id,'II' FROM subjects WHERE code='23CS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',6,'14:50:00','15:40:00',id,'II' FROM subjects WHERE code='23CS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','TUESDAY',7,'15:40:00','16:30:00',id,'II' FROM subjects WHERE code='23CS305';

-- Wednesday
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',1,'09:30:00','10:20:00',id,'ALL' FROM subjects WHERE code='23CS302';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',2,'10:20:00','11:10:00',id,'I' FROM subjects WHERE code='23CS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',3,'11:25:00','12:15:00',id,'I' FROM subjects WHERE code='23CS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',4,'12:15:00','13:05:00',id,'I' FROM subjects WHERE code='23CS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',2,'10:20:00','11:10:00',id,'II' FROM subjects WHERE code='23CS304';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',3,'11:25:00','12:15:00',id,'II' FROM subjects WHERE code='23CS304';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',4,'12:15:00','13:05:00',id,'II' FROM subjects WHERE code='23CS304';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',5,'14:00:00','14:50:00',id,'ALL' FROM subjects WHERE code='23CS303';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',6,'14:50:00','15:40:00',id,'ALL' FROM subjects WHERE code='23MC003';
INSERT INTO timetable SELECT NULL,3,'CSE','A','WEDNESDAY',7,'15:40:00','16:30:00',id,'ALL' FROM subjects WHERE code='23CS301';

-- Thursday
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',1,'09:30:00','10:20:00',id,'ALL' FROM subjects WHERE code='23ES301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',2,'10:20:00','11:10:00',id,'ALL' FROM subjects WHERE code='23CS302';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',3,'11:25:00','12:15:00',id,'ALL' FROM subjects WHERE code='23CS301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',4,'12:15:00','13:05:00',id,'ALL' FROM subjects WHERE code='23BS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',5,'14:00:00','14:50:00',id,'I' FROM subjects WHERE code='23CS304';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',6,'14:50:00','15:40:00',id,'I' FROM subjects WHERE code='23CS304';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',7,'15:40:00','16:30:00',id,'I' FROM subjects WHERE code='23CS304';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',5,'14:00:00','14:50:00',id,'II' FROM subjects WHERE code='23CS306';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',6,'14:50:00','15:40:00',id,'II' FROM subjects WHERE code='23CS306';
INSERT INTO timetable SELECT NULL,3,'CSE','A','THURSDAY',7,'15:40:00','16:30:00',id,'II' FROM subjects WHERE code='23CS306';

-- Friday
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',1,'09:30:00','10:20:00',id,'ALL' FROM subjects WHERE code='23BS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',2,'10:20:00','11:10:00',id,'ALL' FROM subjects WHERE code='23CS303';
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',3,'11:25:00','12:15:00',id,'ALL' FROM subjects WHERE code='23ES301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',4,'12:15:00','13:05:00',id,'ALL' FROM subjects WHERE code='23CS301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',5,'14:00:00','14:50:00',id,'ALL' FROM subjects WHERE code='23BS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',6,'14:50:00','15:40:00',id,'ALL' FROM subjects WHERE code='23CS301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','FRIDAY',7,'15:40:00','16:30:00',id,'ALL' FROM subjects WHERE code='23CS302';

-- Saturday
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',1,'09:30:00','10:20:00',id,'ALL' FROM subjects WHERE code='23CS303';
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',2,'10:20:00','11:10:00',id,'ALL' FROM subjects WHERE code='23BS305';
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',3,'11:25:00','12:15:00',id,'ALL' FROM subjects WHERE code='23MC003';
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',4,'12:15:00','13:05:00',id,'ALL' FROM subjects WHERE code='23CS302';
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',5,'14:00:00','14:50:00',id,'ALL' FROM subjects WHERE code='23CS301';
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',6,'14:50:00','15:40:00',id,'ALL' FROM subjects WHERE code='23CS302';
INSERT INTO timetable SELECT NULL,3,'CSE','A','SATURDAY',7,'15:40:00','16:30:00',id,'ALL' FROM subjects WHERE code='23ES301';

-- Verification queries
SELECT 'ADMIN' AS section, admin_id, name, active FROM administrators;
SELECT 'STAFF' AS section, staff_id, name, active FROM staff ORDER BY staff_id;
SELECT 'TIMETABLE' AS section, day_of_week, period, start_time, end_time, COALESCE(s.code,'LIB') AS code, COALESCE(s.name,t.activity_name) AS subject_name, batch
FROM timetable t LEFT JOIN subjects s ON s.id=t.subject_id
ORDER BY FIELD(day_of_week,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'), period, batch;
