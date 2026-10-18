USE student_management;

-- ============================================================
-- SIMS REVIEW 2
-- 03 - SEED DATA
-- Demo / Initial Data Only
-- ============================================================
-- ============================================================
-- 1. ADMINISTRATOR DEMO ACCOUNT
-- ============================================================

INSERT INTO administrators
(admin_id, username, password, full_name, status)
VALUES
('ADM001', 'admin', 'admin123', 'System Administrator', 'ACTIVE');
-- ============================================================
-- 2. STAFF DEMO ACCOUNTS
-- ============================================================

INSERT INTO staff
(staff_id, name, password, active)
VALUES
('STF001', 'Demo Staff', 'admin123', 1);

-- ============================================================
-- 3. STUDENT DEMO DATA
-- ============================================================

INSERT INTO students
(roll_no, name, department, year, mobile, email, address, edit_permission, batch)
VALUES
('ES25CS21', 'Boopathivasan B', 'CSE', 2,
 '8807428875', 'boopathi8566@gmail.com',
 'Salem, Tamil Nadu', 1, 'A');
 
 -- ============================================================
-- 4. SUBJECT DEMO DATA
-- Semester 3 - CSE
-- ============================================================

INSERT INTO subjects
(semester, code, name, type, credits)
VALUES
(3, '23BS305', 'Discrete Mathematics', 'THEORY', 4.00),
(3, '23ES301', 'Digital System and Design', 'THEORY', 4.00),
(3, '23CS301', 'Data Structures', 'THEORY', 4.00),
(3, '23CS302', 'Object Oriented Programming', 'THEORY', 4.00),
(3, '23CS303', 'Database Management Systems', 'THEORY', 4.00),
(3, '23MC003', 'Indian Constitution', 'THEORY', 0.00),
(3, '23TPS03', 'Quantitative Aptitude and Logical Reasoning - I', 'THEORY', 0.00),
(3, '23CS304', 'Database Management Systems Laboratory', 'LAB', 2.00),
(3, '23CS305', 'Data Structures Implementation Laboratory', 'LAB', 2.00),
(3, '23CS306', 'Object Oriented Programming Laboratory', 'LAB', 2.00);

-- ============================================================
-- 5. STAFF - SUBJECT ASSIGNMENT
-- Demo Staff is assigned to Semester 3 subjects
-- ============================================================

INSERT INTO staff_subjects (staff_id, subject_id)
SELECT s.id, sub.id
FROM staff s
JOIN subjects sub
WHERE s.staff_id = 'STF001'
  AND sub.semester = 3
  AND NOT EXISTS (
      SELECT 1
      FROM staff_subjects ss
      WHERE ss.staff_id = s.id
        AND ss.subject_id = sub.id
  );
  
  -- ============================================================
-- 6. TIMETABLE DATA
-- Existing Semester 3 CSE-A timetable
-- Timetable records are already present in the database.
-- ============================================================

-- Timetable data is maintained through the existing
-- semester-wise timetable records.
-- No duplicate INSERT statements are required here.

-- ============================================================
-- 7. DEMO MARKS
-- Existing marks are retained.
-- Application can create/update marks using the marks module.
-- ============================================================

-- ============================================================
-- 8. DEMO ATTENDANCE
-- Existing attendance records are retained.
-- Attendance is controlled through timetable validation.
-- ============================================================