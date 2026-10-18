USE student_management;

-- ============================================================
-- SIMS REVIEW 2
-- 04 - VERIFICATION
-- Database Integrity & Demo Data Checks
-- ============================================================


-- ============================================================
-- 1. CHECK TABLES
-- ============================================================

SHOW TABLES;


-- ============================================================
-- 2. CHECK ADMINISTRATORS
-- ============================================================

SELECT
    admin_id,
    name,
    active
FROM administrators;


-- ============================================================
-- 3. CHECK STAFF
-- ============================================================

SELECT
    staff_id,
    name,
    active
FROM staff;


-- ============================================================
-- 4. CHECK STUDENTS
-- ============================================================

SELECT
    roll_no,
    name,
    department,
    year,
    batch,
    edit_permission
FROM students;


-- ============================================================
-- 5. CHECK SUBJECTS
-- ============================================================

SELECT
    id,
    semester,
    code,
    name,
    type,
    credits
FROM subjects
ORDER BY semester, id;


-- ============================================================
-- 6. CHECK STAFF-SUBJECT ASSIGNMENTS
-- ============================================================

SELECT
    s.staff_id,
    s.name AS staff_name,
    sub.code AS subject_code,
    sub.name AS subject_name
FROM staff_subjects ss
JOIN staff s
    ON ss.staff_id = s.id
JOIN subjects sub
    ON ss.subject_id = sub.id
ORDER BY s.staff_id, sub.id;


-- ============================================================
-- 7. CHECK TIMETABLE
-- ============================================================

SELECT
    day_of_week,
    period,
    start_time,
    end_time,
    subject_id,
    activity_name,
    batch
FROM timetable
ORDER BY
    FIELD(day_of_week,
        'Monday',
        'Tuesday',
        'Wednesday',
        'Thursday',
        'Friday',
        'Saturday'
    ),
    period;


-- ============================================================
-- 8. RECORD COUNTS
-- ============================================================

SELECT 'Administrators' AS table_name, COUNT(*) AS total
FROM administrators

UNION ALL

SELECT 'Staff', COUNT(*)
FROM staff

UNION ALL

SELECT 'Students', COUNT(*)
FROM students

UNION ALL

SELECT 'Subjects', COUNT(*)
FROM subjects

UNION ALL

SELECT 'Staff-Subject Assignments', COUNT(*)
FROM staff_subjects

UNION ALL

SELECT 'Timetable', COUNT(*)
FROM timetable

UNION ALL

SELECT 'Marks', COUNT(*)
FROM marks

UNION ALL

SELECT 'Attendance', COUNT(*)
FROM attendance;