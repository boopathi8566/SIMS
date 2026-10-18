package esec.sims;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {

    public static void main(String[] args) throws Exception {

        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0
        );

        // Frontend static assets
        server.createContext("/", WebServer::serveFrontend);

        // Authentication API
        server.createContext("/api/login", WebServer::login);

        // Student Dashboard & Profile APIs
        server.createContext("/api/dashboard", WebServer::dashboard);
        server.createContext("/api/student/profile", WebServer::studentProfile);

        // Staff - Students Management APIs (CRUD & Search)
        server.createContext("/api/students", WebServer::students);

        // Staff - Subjects APIs
        server.createContext("/api/subjects", WebServer::subjects);

        // Staff - Marks APIs
        server.createContext("/api/marks", WebServer::marks);

        // Staff - Attendance APIs
        server.createContext("/api/attendance", WebServer::attendance);

        // Staff - Profile Permission API
        server.createContext("/api/profile-permission", WebServer::profilePermission);

        // Administrator - Staff Management & Stats APIs
        server.createContext("/api/admin/staff/toggle", WebServer::adminToggleStaff);
        server.createContext("/api/admin/staff", WebServer::adminStaff);
        server.createContext("/api/admin/stats", WebServer::adminStats);

        server.setExecutor(null);
        server.start();

        System.out.println("=================================");
        System.out.println("ESEC SIMS WEB SERVER");
        System.out.println("Running at: http://localhost:" + port);
        System.out.println("Connected to MySQL via DB.java");
        System.out.println("=================================");
    }

    // =========================================================
    // FRONTEND STATIC FILE HANDLER
    // =========================================================

    static void serveFrontend(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        Path file = Paths.get("frontend", path.substring(1));

        if (!Files.exists(file) || Files.isDirectory(file)) {
            send(exchange, 404, "404 - File Not Found", "text/plain");
            return;
        }

        String contentType = getContentType(file);
        byte[] data = Files.readAllBytes(file);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, data.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    // =========================================================
    // LOGIN API
    // =========================================================

    static void login(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            send(exchange, 405, "{\"success\":false,\"message\":\"POST required\"}", "application/json");
            return;
        }

        Map<String, String> data = parsePayload(exchange);

        String role = data.getOrDefault("role", "").trim();
        String id = data.getOrDefault("id", "").trim();
        String credential = data.getOrDefault("credential", "").trim();

        if (role.isBlank() || id.isBlank() || credential.isBlank()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Enter all login details.\"}", "application/json");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            if (role.equalsIgnoreCase("Student")) {

                String sql = "SELECT roll_no, name FROM students WHERE roll_no=? AND mobile=?";
                try (PreparedStatement ps = DB.prepare(conn, sql, id, credential);
                     ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        String name = escapeJson(rs.getString("name"));
                        send(exchange, 200,
                                "{\"success\":true,\"role\":\"Student\",\"id\":\"" + escapeJson(id) + "\",\"name\":\"" + name + "\"}",
                                "application/json");
                    } else {
                        send(exchange, 401, "{\"success\":false,\"message\":\"Invalid roll number or registered mobile.\"}", "application/json");
                    }
                }

            } else if (role.equalsIgnoreCase("Staff")) {

                String sql = "SELECT staff_id, name FROM staff WHERE staff_id=? AND password=? AND (active=TRUE OR active IS NULL)";
                try (PreparedStatement ps = DB.prepare(conn, sql, id, credential);
                     ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        String name = escapeJson(rs.getString("name"));
                        send(exchange, 200,
                                "{\"success\":true,\"role\":\"Staff\",\"id\":\"" + escapeJson(id) + "\",\"name\":\"" + name + "\"}",
                                "application/json");
                    } else {
                        send(exchange, 401, "{\"success\":false,\"message\":\"Invalid staff ID or password.\"}", "application/json");
                    }
                }

            } else if (role.equalsIgnoreCase("Administrator") || role.equalsIgnoreCase("Admin")) {

                String sql = "SELECT admin_id, name FROM administrators WHERE admin_id=? AND password=? AND (active=TRUE OR active IS NULL)";
                try (PreparedStatement ps = DB.prepare(conn, sql, id, credential);
                     ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        String name = escapeJson(rs.getString("name"));
                        send(exchange, 200,
                                "{\"success\":true,\"role\":\"Administrator\",\"id\":\"" + escapeJson(id) + "\",\"name\":\"" + name + "\"}",
                                "application/json");
                    } else {
                        send(exchange, 401, "{\"success\":false,\"message\":\"Invalid administrator ID or password.\"}", "application/json");
                    }
                }

            } else {
                send(exchange, 400, "{\"success\":false,\"message\":\"Invalid role.\"}", "application/json");
            }

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    // =========================================================
    // DASHBOARD API (STUDENT)
    // =========================================================

    static void dashboard(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            send(exchange, 405, "{\"success\":false,\"message\":\"GET required\"}", "application/json");
            return;
        }

        String rollNo = getQueryParam(exchange, "id");
        if (rollNo == null || rollNo.isBlank()) {
            rollNo = getQueryParam(exchange, "rollNo");
        }

        if (rollNo == null || rollNo.isBlank()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Student ID required\"}", "application/json");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            // Student information
            String studentSql = "SELECT roll_no, name, department, year, mobile, email, address, batch, edit_permission FROM students WHERE roll_no=?";
            String studentJson = null;

            try (PreparedStatement ps = DB.prepare(conn, studentSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    studentJson = "{"
                            + "\"rollNo\":\"" + escapeJson(rs.getString("roll_no")) + "\","
                            + "\"name\":\"" + escapeJson(rs.getString("name")) + "\","
                            + "\"department\":\"" + escapeJson(rs.getString("department")) + "\","
                            + "\"year\":" + rs.getInt("year") + ","
                            + "\"mobile\":\"" + escapeJson(rs.getString("mobile")) + "\","
                            + "\"email\":\"" + escapeJson(rs.getString("email")) + "\","
                            + "\"address\":\"" + escapeJson(rs.getString("address")) + "\","
                            + "\"batch\":\"" + escapeJson(rs.getString("batch")) + "\","
                            + "\"editPermission\":" + rs.getBoolean("edit_permission")
                            + "}";
                }
            }

            if (studentJson == null) {
                send(exchange, 404, "{\"success\":false,\"message\":\"Student not found\"}", "application/json");
                return;
            }

            // Marks
            StringBuilder marksJson = new StringBuilder("[");
            String marksSql = "SELECT sub.code, sub.name, sub.type, "
                    + "COALESCE(m.cat1, 0) cat1, COALESCE(m.cat2, 0) cat2, "
                    + "COALESCE(m.sac1, 0) sac1, COALESCE(m.sac2, 0) sac2, COALESCE(m.sac3, 0) sac3, COALESCE(m.sac4, 0) sac4, COALESCE(m.sac5, 0) sac5 "
                    + "FROM subjects sub "
                    + "LEFT JOIN marks m ON m.subject_id=sub.id AND m.student_id=(SELECT id FROM students WHERE roll_no=?) "
                    + "WHERE sub.semester=3 "
                    + "ORDER BY sub.id";

            double totalPerf = 0;
            int perfSubjects = 0;
            boolean firstMark = true;

            try (PreparedStatement ps = DB.prepare(conn, marksSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    if (!firstMark) marksJson.append(",");
                    firstMark = false;

                    double cat1 = rs.getDouble("cat1");
                    double cat2 = rs.getDouble("cat2");
                    double sac1 = rs.getDouble("sac1");
                    double sac2 = rs.getDouble("sac2");
                    double sac3 = rs.getDouble("sac3");
                    double sac4 = rs.getDouble("sac4");
                    double sac5 = rs.getDouble("sac5");

                    double total = cat1 + cat2 + sac1 + sac2 + sac3 + sac4 + sac5;

                    double perfSum = 0;
                    int perfCount = 0;
                    if (cat1 > 0) { perfSum += cat1; perfCount++; }
                    if (cat2 > 0) { perfSum += cat2; perfCount++; }
                    for (double s : new double[]{sac1, sac2, sac3, sac4, sac5}) {
                        if (s > 0) { perfSum += s * 5; perfCount++; }
                    }
                    double subjectPerf = perfCount == 0 ? 0 : perfSum / perfCount;
                    if (subjectPerf > 0) {
                        totalPerf += subjectPerf;
                        perfSubjects++;
                    }

                    marksJson.append("{")
                            .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                            .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                            .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                            .append("\"cat1\":").append(cat1).append(",")
                            .append("\"cat2\":").append(cat2).append(",")
                            .append("\"sac1\":").append(sac1).append(",")
                            .append("\"sac2\":").append(sac2).append(",")
                            .append("\"sac3\":").append(sac3).append(",")
                            .append("\"sac4\":").append(sac4).append(",")
                            .append("\"sac5\":").append(sac5).append(",")
                            .append("\"total\":").append(total).append(",")
                            .append("\"performance\":").append(String.format(Locale.US, "%.1f", subjectPerf))
                            .append("}");
                }
            }
            marksJson.append("]");

            // Attendance
            StringBuilder attendanceJson = new StringBuilder("[");
            String attSql = "SELECT sub.code, sub.name, "
                    + "COUNT(a.id) AS total, "
                    + "SUM(CASE WHEN a.status='PRESENT' OR a.status='OD' THEN 1 ELSE 0 END) AS present, "
                    + "SUM(CASE WHEN a.status='ABSENT' THEN 1 ELSE 0 END) AS absent "
                    + "FROM subjects sub "
                    + "LEFT JOIN attendance a ON a.subject_id=sub.id AND a.student_id=(SELECT id FROM students WHERE roll_no=?) "
                    + "WHERE sub.semester=3 "
                    + "GROUP BY sub.id, sub.code, sub.name "
                    + "ORDER BY sub.id";

            double totalAttPercent = 0;
            int attCount = 0;
            boolean firstAtt = true;

            try (PreparedStatement ps = DB.prepare(conn, attSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    if (!firstAtt) attendanceJson.append(",");
                    firstAtt = false;

                    int total = rs.getInt("total");
                    int present = rs.getInt("present");
                    int absent = rs.getInt("absent");
                    double percentage = total == 0 ? 0 : (present * 100.0) / total;

                    if (total > 0) {
                        totalAttPercent += percentage;
                        attCount++;
                    }

                    attendanceJson.append("{")
                            .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                            .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                            .append("\"present\":").append(present).append(",")
                            .append("\"absent\":").append(absent).append(",")
                            .append("\"total\":").append(total).append(",")
                            .append("\"percentage\":").append(String.format(Locale.US, "%.1f", percentage))
                            .append("}");
                }
            }
            attendanceJson.append("]");

            double avgPerformance = perfSubjects == 0 ? 0 : totalPerf / perfSubjects;
            double avgAttendance = attCount == 0 ? 0 : totalAttPercent / attCount;
            int alerts = 0;
            if (avgAttendance > 0 && avgAttendance < 75.0) alerts++;
            if (avgPerformance > 0 && avgPerformance < 50.0) alerts++;

            String response = "{"
                    + "\"success\":true,"
                    + "\"student\":" + studentJson + ","
                    + "\"marks\":" + marksJson + ","
                    + "\"attendance\":" + attendanceJson + ","
                    + "\"stats\":{"
                    + "\"avgPerformance\":\"" + String.format(Locale.US, "%.1f", avgPerformance) + "\","
                    + "\"avgAttendance\":\"" + String.format(Locale.US, "%.1f", avgAttendance) + "\","
                    + "\"alerts\":" + alerts
                    + "}"
                    + "}";

            send(exchange, 200, response, "application/json");

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    // =========================================================
    // STUDENT PROFILE UPDATE API (STUDENT SELF-SERVICE)
    // =========================================================

    static void studentProfile(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();
        if (!method.equals("POST") && !method.equals("PUT")) {
            send(exchange, 405, "{\"success\":false,\"message\":\"POST or PUT required\"}", "application/json");
            return;
        }

        Map<String, String> data = parsePayload(exchange);
        String rollNo = data.getOrDefault("rollNo", "").trim();
        String name = data.getOrDefault("name", "").trim();
        String mobile = data.getOrDefault("mobile", "").trim();
        String email = data.getOrDefault("email", "").trim();
        String address = data.getOrDefault("address", "").trim();

        if (rollNo.isEmpty() || name.isEmpty() || mobile.isEmpty()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Roll number, name and mobile are required.\"}", "application/json");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            // Check if student exists and whether profile edit is allowed
            String checkSql = "SELECT edit_permission FROM students WHERE roll_no=?";
            try (PreparedStatement ps = DB.prepare(conn, checkSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                    return;
                }

                boolean allowed = rs.getBoolean("edit_permission");
                if (!allowed) {
                    send(exchange, 403, "{\"success\":false,\"message\":\"Profile editing is currently locked. Staff permission required.\"}", "application/json");
                    return;
                }
            }

            // Update permitted fields only (roll_no, department, and year remain protected)
            String updateSql = "UPDATE students SET name=?, mobile=?, email=?, address=? WHERE roll_no=?";
            int updated = DB.update(updateSql, name, mobile, email, address, rollNo);

            if (updated > 0) {
                send(exchange, 200, "{\"success\":true,\"message\":\"Profile updated successfully.\"}", "application/json");
            } else {
                send(exchange, 404, "{\"success\":false,\"message\":\"Student record could not be updated.\"}", "application/json");
            }

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    // =========================================================
    // STUDENTS CRUD & SEARCH API (STAFF / MANAGEMENT)
    // =========================================================

    static void students(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {
            handleGetStudents(exchange);
        } else if (method.equals("POST")) {
            Map<String, String> data = parsePayload(exchange);
            String action = data.getOrDefault("_action", data.getOrDefault("action", "")).toLowerCase();
            if (action.equals("delete")) {
                handleDeleteStudent(exchange, data);
            } else if (action.equals("update")) {
                handleUpdateStudent(exchange, data);
            } else {
                handleAddStudent(exchange, data);
            }
        } else if (method.equals("PUT")) {
            Map<String, String> data = parsePayload(exchange);
            handleUpdateStudent(exchange, data);
        } else if (method.equals("DELETE")) {
            Map<String, String> data = parsePayload(exchange);
            handleDeleteStudent(exchange, data);
        } else {
            send(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}", "application/json");
        }
    }

    private static void handleGetStudents(HttpExchange exchange) throws IOException {

        String query = getQueryParam(exchange, "search");
        String singleId = getQueryParam(exchange, "id");
        if (singleId == null) singleId = getQueryParam(exchange, "rollNo");

        try (Connection conn = DB.getConnection()) {

            if (singleId != null && !singleId.isBlank()) {
                String sql = "SELECT roll_no, name, department, year, mobile, email, address, batch, edit_permission FROM students WHERE roll_no=?";
                try (PreparedStatement ps = DB.prepare(conn, sql, singleId);
                     ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        String studentJson = "{"
                                + "\"rollNo\":\"" + escapeJson(rs.getString("roll_no")) + "\","
                                + "\"name\":\"" + escapeJson(rs.getString("name")) + "\","
                                + "\"department\":\"" + escapeJson(rs.getString("department")) + "\","
                                + "\"year\":" + rs.getInt("year") + ","
                                + "\"mobile\":\"" + escapeJson(rs.getString("mobile")) + "\","
                                + "\"email\":\"" + escapeJson(rs.getString("email")) + "\","
                                + "\"address\":\"" + escapeJson(rs.getString("address")) + "\","
                                + "\"batch\":\"" + escapeJson(rs.getString("batch")) + "\","
                                + "\"editPermission\":" + rs.getBoolean("edit_permission")
                                + "}";
                        send(exchange, 200, "{\"success\":true,\"student\":" + studentJson + "}", "application/json");
                    } else {
                        send(exchange, 404, "{\"success\":false,\"message\":\"Student not found\"}", "application/json");
                    }
                    return;
                }
            }

            String sql;
            PreparedStatement ps;

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim() + "%";
                sql = "SELECT roll_no, name, department, year, mobile, email, address, batch, edit_permission "
                        + "FROM students WHERE roll_no LIKE ? OR name LIKE ? OR department LIKE ? ORDER BY roll_no";
                ps = DB.prepare(conn, sql, pattern, pattern, pattern);
            } else {
                sql = "SELECT roll_no, name, department, year, mobile, email, address, batch, edit_permission FROM students ORDER BY roll_no";
                ps = conn.prepareStatement(sql);
            }

            StringBuilder sb = new StringBuilder("{\"success\":true,\"students\":[");
            boolean first = true;

            try (ps; ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;

                    sb.append("{")
                            .append("\"rollNo\":\"").append(escapeJson(rs.getString("roll_no"))).append("\",")
                            .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                            .append("\"department\":\"").append(escapeJson(rs.getString("department"))).append("\",")
                            .append("\"year\":").append(rs.getInt("year")).append(",")
                            .append("\"mobile\":\"").append(escapeJson(rs.getString("mobile"))).append("\",")
                            .append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",")
                            .append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\",")
                            .append("\"batch\":\"").append(escapeJson(rs.getString("batch"))).append("\",")
                            .append("\"editPermission\":").append(rs.getBoolean("edit_permission"))
                            .append("}");
                }
            }
            sb.append("]}");

            send(exchange, 200, sb.toString(), "application/json");

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    private static void handleAddStudent(HttpExchange exchange, Map<String, String> data) throws IOException {

        String rollNo = data.getOrDefault("rollNo", data.getOrDefault("roll", "")).trim().toUpperCase();
        String name = data.getOrDefault("name", "").trim();
        String dept = data.getOrDefault("department", data.getOrDefault("dept", "CSE-A")).trim();
        String yearStr = data.getOrDefault("year", "2").trim();
        String mobile = data.getOrDefault("mobile", "").trim();
        String email = data.getOrDefault("email", "").trim();
        String address = data.getOrDefault("address", "").trim();
        String batch = data.getOrDefault("batch", "I").trim();

        if (rollNo.isEmpty() || name.isEmpty()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Roll number and name are required.\"}", "application/json");
            return;
        }

        int year = 2;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException ignored) {}

        try (Connection conn = DB.getConnection()) {

            // Check duplicate
            String checkSql = "SELECT 1 FROM students WHERE roll_no=?";
            try (PreparedStatement ps = DB.prepare(conn, checkSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    send(exchange, 409, "{\"success\":false,\"message\":\"Student with roll number " + rollNo + " already exists.\"}", "application/json");
                    return;
                }
            }

            String insertSql = "INSERT INTO students (roll_no, name, department, year, mobile, email, address, batch, edit_permission) VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)";
            DB.update(insertSql, rollNo, name, dept, year, mobile, email, address, batch);

            send(exchange, 201, "{\"success\":true,\"message\":\"Student added successfully.\"}", "application/json");

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    private static void handleUpdateStudent(HttpExchange exchange, Map<String, String> data) throws IOException {

        String rollNo = data.getOrDefault("rollNo", data.getOrDefault("roll", "")).trim().toUpperCase();
        String name = data.getOrDefault("name", "").trim();
        String dept = data.getOrDefault("department", data.getOrDefault("dept", "")).trim();
        String yearStr = data.getOrDefault("year", "").trim();
        String mobile = data.getOrDefault("mobile", "").trim();
        String email = data.getOrDefault("email", "").trim();
        String address = data.getOrDefault("address", "").trim();
        String batch = data.getOrDefault("batch", "").trim();

        if (rollNo.isEmpty()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Roll number is required for update.\"}", "application/json");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            // Fetch current values
            String fetchSql = "SELECT name, department, year, mobile, email, address, batch FROM students WHERE roll_no=?";
            String curName, curDept, curMobile, curEmail, curAddress, curBatch;
            int curYear;

            try (PreparedStatement ps = DB.prepare(conn, fetchSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                    return;
                }

                curName = rs.getString("name");
                curDept = rs.getString("department");
                curYear = rs.getInt("year");
                curMobile = rs.getString("mobile");
                curEmail = rs.getString("email");
                curAddress = rs.getString("address");
                curBatch = rs.getString("batch");
            }

            String finalName = name.isEmpty() ? curName : name;
            String finalDept = dept.isEmpty() ? curDept : dept;
            int finalYear = yearStr.isEmpty() ? curYear : Integer.parseInt(yearStr);
            String finalMobile = mobile.isEmpty() ? curMobile : mobile;
            String finalEmail = email.isEmpty() ? curEmail : email;
            String finalAddress = address.isEmpty() ? curAddress : address;
            String finalBatch = batch.isEmpty() ? (curBatch != null ? curBatch : "I") : batch;

            String updateSql = "UPDATE students SET name=?, department=?, year=?, mobile=?, email=?, address=?, batch=? WHERE roll_no=?";
            int updated = DB.update(updateSql, finalName, finalDept, finalYear, finalMobile, finalEmail, finalAddress, finalBatch, rollNo);

            if (updated > 0) {
                send(exchange, 200, "{\"success\":true,\"message\":\"Student updated successfully.\"}", "application/json");
            } else {
                send(exchange, 404, "{\"success\":false,\"message\":\"Student could not be updated.\"}", "application/json");
            }

        } catch (Exception ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    private static void handleDeleteStudent(HttpExchange exchange, Map<String, String> data) throws IOException {

        String rollNo = data.getOrDefault("rollNo", data.getOrDefault("roll", "")).trim().toUpperCase();
        if (rollNo.isEmpty()) {
            rollNo = getQueryParam(exchange, "id");
            if (rollNo == null || rollNo.isBlank()) rollNo = getQueryParam(exchange, "rollNo");
        }

        if (rollNo == null || rollNo.isBlank()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Roll number is required for deletion.\"}", "application/json");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            // Check if student exists
            String checkSql = "SELECT name FROM students WHERE roll_no=?";
            try (PreparedStatement ps = DB.prepare(conn, checkSql, rollNo);
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                    return;
                }
            }

            // Database CASCADE deletes marks and attendance records automatically
            String delSql = "DELETE FROM students WHERE roll_no=?";
            int deleted = DB.update(delSql, rollNo);

            if (deleted > 0) {
                send(exchange, 200, "{\"success\":true,\"message\":\"Student deleted successfully.\"}", "application/json");
            } else {
                send(exchange, 404, "{\"success\":false,\"message\":\"Student could not be deleted.\"}", "application/json");
            }

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    // =========================================================
    // SUBJECTS API
    // =========================================================

    static void subjects(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {

            String semParam = getQueryParam(exchange, "semester");
            Integer semester = null;
            if (semParam != null && !semParam.isBlank()) {
                try { semester = Integer.parseInt(semParam); } catch (NumberFormatException ignored) {}
            }

            try (Connection conn = DB.getConnection()) {

                String sql = "SELECT id, semester, code, name, type, credits FROM subjects "
                        + (semester != null ? "WHERE semester=? " : "")
                        + "ORDER BY semester, code";

                PreparedStatement ps = semester != null ? DB.prepare(conn, sql, semester) : conn.prepareStatement(sql);

                StringBuilder sb = new StringBuilder("{\"success\":true,\"subjects\":[");
                boolean first = true;

                try (ps; ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;

                        sb.append("{")
                                .append("\"id\":").append(rs.getInt("id")).append(",")
                                .append("\"semester\":").append(rs.getInt("semester")).append(",")
                                .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                                .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                                .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                                .append("\"credits\":").append(rs.getDouble("credits"))
                                .append("}");
                    }
                }
                sb.append("]}");

                send(exchange, 200, sb.toString(), "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else if (method.equals("POST")) {

            Map<String, String> data = parsePayload(exchange);
            String semStr = data.getOrDefault("semester", "3").trim();
            String code = data.getOrDefault("code", "").trim().toUpperCase();
            String name = data.getOrDefault("name", "").trim();
            String type = data.getOrDefault("type", "Theory").trim();
            String creditsStr = data.getOrDefault("credits", "3.0").trim();

            if (code.isEmpty() || name.isEmpty()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Subject code and name are required.\"}", "application/json");
                return;
            }

            int sem = 3;
            double credits = 3.0;
            try { sem = Integer.parseInt(semStr); } catch (NumberFormatException ignored) {}
            try { credits = Double.parseDouble(creditsStr); } catch (NumberFormatException ignored) {}

            try (Connection conn = DB.getConnection()) {

                // Check duplicate
                try (PreparedStatement ps = DB.prepare(conn, "SELECT 1 FROM subjects WHERE code=?", code);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        send(exchange, 409, "{\"success\":false,\"message\":\"Subject code already exists.\"}", "application/json");
                        return;
                    }
                }

                String insertSql = "INSERT INTO subjects (semester, code, name, type, credits) VALUES (?, ?, ?, ?, ?)";
                DB.update(insertSql, sem, code, name, type, credits);

                send(exchange, 201, "{\"success\":true,\"message\":\"Subject added successfully.\"}", "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else {
            send(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}", "application/json");
        }
    }

    // =========================================================
    // MARKS API
    // =========================================================

    static void marks(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {

            String rollNo = getQueryParam(exchange, "rollNo");
            String code = getQueryParam(exchange, "code");

            if (rollNo == null || rollNo.isBlank()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Student roll number required.\"}", "application/json");
                return;
            }

            try (Connection conn = DB.getConnection()) {

                String sql = "SELECT sub.code, sub.name, sub.type, "
                        + "COALESCE(m.cat1, 0) cat1, COALESCE(m.cat2, 0) cat2, "
                        + "COALESCE(m.sac1, 0) sac1, COALESCE(m.sac2, 0) sac2, COALESCE(m.sac3, 0) sac3, COALESCE(m.sac4, 0) sac4, COALESCE(m.sac5, 0) sac5 "
                        + "FROM subjects sub "
                        + "LEFT JOIN marks m ON m.subject_id=sub.id AND m.student_id=(SELECT id FROM students WHERE roll_no=?) "
                        + "WHERE (? IS NULL OR ? = '' OR sub.code=?) "
                        + "ORDER BY sub.id";

                StringBuilder sb = new StringBuilder("{\"success\":true,\"marks\":[");
                boolean first = true;

                try (PreparedStatement ps = DB.prepare(conn, sql, rollNo, code, code, code);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;

                        double cat1 = rs.getDouble("cat1");
                        double cat2 = rs.getDouble("cat2");
                        double sac1 = rs.getDouble("sac1");
                        double sac2 = rs.getDouble("sac2");
                        double sac3 = rs.getDouble("sac3");
                        double sac4 = rs.getDouble("sac4");
                        double sac5 = rs.getDouble("sac5");
                        double total = cat1 + cat2 + sac1 + sac2 + sac3 + sac4 + sac5;

                        sb.append("{")
                                .append("\"code\":\"").append(escapeJson(rs.getString("code"))).append("\",")
                                .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                                .append("\"type\":\"").append(escapeJson(rs.getString("type"))).append("\",")
                                .append("\"cat1\":").append(cat1).append(",")
                                .append("\"cat2\":").append(cat2).append(",")
                                .append("\"sac1\":").append(sac1).append(",")
                                .append("\"sac2\":").append(sac2).append(",")
                                .append("\"sac3\":").append(sac3).append(",")
                                .append("\"sac4\":").append(sac4).append(",")
                                .append("\"sac5\":").append(sac5).append(",")
                                .append("\"total\":").append(total)
                                .append("}");
                    }
                }
                sb.append("]}");

                send(exchange, 200, sb.toString(), "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else if (method.equals("POST")) {

            Map<String, String> data = parsePayload(exchange);
            String rollNo = data.getOrDefault("rollNo", "").trim().toUpperCase();
            String code = data.getOrDefault("code", "").trim().toUpperCase();

            if (rollNo.isEmpty() || code.isEmpty()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Roll number and subject code are required.\"}", "application/json");
                return;
            }

            double cat1 = parseDoubleSafe(data.get("cat1"));
            double cat2 = parseDoubleSafe(data.get("cat2"));
            double sac1 = parseDoubleSafe(data.get("sac1"));
            double sac2 = parseDoubleSafe(data.get("sac2"));
            double sac3 = parseDoubleSafe(data.get("sac3"));
            double sac4 = parseDoubleSafe(data.get("sac4"));
            double sac5 = parseDoubleSafe(data.get("sac5"));

            if (cat1 < 0 || cat1 > 100 || cat2 < 0 || cat2 > 100 ||
                sac1 < 0 || sac1 > 20 || sac2 < 0 || sac2 > 20 ||
                sac3 < 0 || sac3 > 20 || sac4 < 0 || sac4 > 20 || sac5 < 0 || sac5 > 20) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Invalid marks. CAT marks must be 0-100; SAC marks must be 0-20.\"}", "application/json");
                return;
            }

            try (Connection conn = DB.getConnection()) {

                int studentId = -1;
                int subjectId = -1;

                try (PreparedStatement ps = DB.prepare(conn, "SELECT id FROM students WHERE roll_no=?", rollNo);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) studentId = rs.getInt(1);
                }

                try (PreparedStatement ps = DB.prepare(conn, "SELECT id FROM subjects WHERE code=?", code);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) subjectId = rs.getInt(1);
                }

                if (studentId < 0) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                    return;
                }
                if (subjectId < 0) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Subject not found.\"}", "application/json");
                    return;
                }

                String upsertSql = "INSERT INTO marks (student_id, subject_id, cat1, cat2, sac1, sac2, sac3, sac4, sac5) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "cat1=VALUES(cat1), cat2=VALUES(cat2), "
                        + "sac1=VALUES(sac1), sac2=VALUES(sac2), sac3=VALUES(sac3), "
                        + "sac4=VALUES(sac4), sac5=VALUES(sac5)";

                DB.update(upsertSql, studentId, subjectId, cat1, cat2, sac1, sac2, sac3, sac4, sac5);

                send(exchange, 200, "{\"success\":true,\"message\":\"Marks saved successfully.\",\"total\":" + (cat1 + cat2 + sac1 + sac2 + sac3 + sac4 + sac5) + "}", "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else {
            send(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}", "application/json");
        }
    }

    // =========================================================
    // ATTENDANCE API
    // =========================================================

    static void attendance(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {

            String dateStr = getQueryParam(exchange, "date");
            String code = getQueryParam(exchange, "code");

            if (dateStr == null || dateStr.isBlank() || code == null || code.isBlank()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Both date and subject code are required.\"}", "application/json");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateStr.trim());
            } catch (Exception e) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Invalid date format. Use YYYY-MM-DD.\"}", "application/json");
                return;
            }

            String dayOfWeek = date.getDayOfWeek().name();

            try (Connection conn = DB.getConnection()) {

                int subjectId = -1;
                try (PreparedStatement ps = DB.prepare(conn, "SELECT id FROM subjects WHERE code=?", code.trim());
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) subjectId = rs.getInt(1);
                }

                if (subjectId < 0) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Subject not found.\"}", "application/json");
                    return;
                }

                // Check timetable slots
                String ttSql = "SELECT period, batch FROM timetable WHERE semester=3 AND day_of_week=? AND subject_id=? ORDER BY period";
                List<Integer> scheduledPeriods = new ArrayList<>();
                Map<Integer, String> periodBatchMap = new HashMap<>();

                try (PreparedStatement ps = DB.prepare(conn, ttSql, dayOfWeek, subjectId);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int p = rs.getInt("period");
                        scheduledPeriods.add(p);
                        periodBatchMap.put(p, rs.getString("batch"));
                    }
                }

                // Load students
                String stSql = "SELECT id, roll_no, name, COALESCE(batch, 'I') batch FROM students ORDER BY roll_no";
                StringBuilder sb = new StringBuilder("{\"success\":true,");
                sb.append("\"day\":\"").append(dayOfWeek).append("\",");
                sb.append("\"hasSlots\":").append(!scheduledPeriods.isEmpty()).append(",");
                sb.append("\"scheduledPeriods\":[");
                for (int i = 0; i < scheduledPeriods.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(scheduledPeriods.get(i));
                }
                sb.append("],\"students\":[");

                boolean firstSt = true;
                try (PreparedStatement ps = conn.prepareStatement(stSql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        if (!firstSt) sb.append(",");
                        firstSt = false;

                        int sid = rs.getInt("id");
                        String roll = rs.getString("roll_no");
                        String name = rs.getString("name");
                        String batch = rs.getString("batch");

                        sb.append("{")
                                .append("\"id\":").append(sid).append(",")
                                .append("\"rollNo\":\"").append(escapeJson(roll)).append("\",")
                                .append("\"name\":\"").append(escapeJson(name)).append("\",")
                                .append("\"batch\":\"").append(escapeJson(batch)).append("\",")
                                .append("\"periods\":{");

                        boolean firstP = true;
                        for (int period = 1; period <= 7; period++) {
                            if (!firstP) sb.append(",");
                            firstP = false;

                            boolean isScheduled = false;
                            if (periodBatchMap.containsKey(period)) {
                                String slotBatch = periodBatchMap.get(period);
                                if ("ALL".equalsIgnoreCase(slotBatch) || slotBatch.equalsIgnoreCase(batch)) {
                                    isScheduled = true;
                                }
                            }

                            String status = "—";
                            if (isScheduled) {
                                status = "ABSENT"; // default if scheduled
                                String attCheck = "SELECT status FROM attendance WHERE student_id=? AND subject_id=? AND att_date=? AND period=?";
                                try (PreparedStatement ap = DB.prepare(conn, attCheck, sid, subjectId, java.sql.Date.valueOf(date), period);
                                     ResultSet ar = ap.executeQuery()) {
                                    if (ar.next()) status = ar.getString(1);
                                }
                            }

                            sb.append("\"").append(period).append("\":{")
                                    .append("\"scheduled\":").append(isScheduled).append(",")
                                    .append("\"status\":\"").append(status).append("\"")
                                    .append("}");
                        }
                        sb.append("}}");
                    }
                }
                sb.append("]}");

                send(exchange, 200, sb.toString(), "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else if (method.equals("POST")) {

            Map<String, String> data = parsePayload(exchange);
            String rollNo = data.getOrDefault("rollNo", "").trim().toUpperCase();
            String code = data.getOrDefault("code", "").trim().toUpperCase();
            String dateStr = data.getOrDefault("date", "").trim();
            String periodStr = data.getOrDefault("period", "").trim();
            String status = data.getOrDefault("status", "PRESENT").trim().toUpperCase();

            if (rollNo.isEmpty() || code.isEmpty() || dateStr.isEmpty() || periodStr.isEmpty()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Roll number, subject code, date and period are required.\"}", "application/json");
                return;
            }

            if (!status.equals("PRESENT") && !status.equals("ABSENT") && !status.equals("OD")) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Status must be PRESENT, ABSENT or OD.\"}", "application/json");
                return;
            }

            int period;
            LocalDate date;
            try {
                period = Integer.parseInt(periodStr);
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Invalid period or date.\"}", "application/json");
                return;
            }

            try (Connection conn = DB.getConnection()) {

                int studentId = -1;
                int subjectId = -1;

                try (PreparedStatement ps = DB.prepare(conn, "SELECT id FROM students WHERE roll_no=?", rollNo);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) studentId = rs.getInt(1);
                }

                try (PreparedStatement ps = DB.prepare(conn, "SELECT id FROM subjects WHERE code=?", code);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) subjectId = rs.getInt(1);
                }

                if (studentId < 0) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                    return;
                }
                if (subjectId < 0) {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Subject not found.\"}", "application/json");
                    return;
                }

                String upsertSql = "INSERT INTO attendance (student_id, subject_id, att_date, period, status) "
                        + "VALUES (?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE status=VALUES(status)";

                DB.update(upsertSql, studentId, subjectId, java.sql.Date.valueOf(date), period, status);

                send(exchange, 200, "{\"success\":true,\"message\":\"Attendance recorded successfully.\"}", "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else {
            send(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}", "application/json");
        }
    }

    // =========================================================
    // PROFILE PERMISSION API
    // =========================================================

    static void profilePermission(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {

            String rollNo = getQueryParam(exchange, "rollNo");
            if (rollNo == null || rollNo.isBlank()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Roll number required.\"}", "application/json");
                return;
            }

            try (Connection conn = DB.getConnection()) {
                String sql = "SELECT edit_permission FROM students WHERE roll_no=?";
                try (PreparedStatement ps = DB.prepare(conn, sql, rollNo.trim().toUpperCase());
                     ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        boolean allowed = rs.getBoolean("edit_permission");
                        send(exchange, 200, "{\"success\":true,\"rollNo\":\"" + escapeJson(rollNo) + "\",\"editPermission\":" + allowed + "}", "application/json");
                    } else {
                        send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                    }
                }
            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else if (method.equals("POST")) {

            Map<String, String> data = parsePayload(exchange);
            String rollNo = data.getOrDefault("rollNo", "").trim().toUpperCase();
            String permStr = data.getOrDefault("permission", "false").trim();

            if (rollNo.isEmpty()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Roll number required.\"}", "application/json");
                return;
            }

            boolean allowed = permStr.equalsIgnoreCase("true") || permStr.equalsIgnoreCase("allow") || permStr.equals("1");

            try (Connection conn = DB.getConnection()) {
                String sql = "UPDATE students SET edit_permission=? WHERE roll_no=?";
                int updated = DB.update(sql, allowed, rollNo);

                if (updated > 0) {
                    send(exchange, 200, "{\"success\":true,\"message\":\"Permission updated successfully.\",\"editPermission\":" + allowed + "}", "application/json");
                } else {
                    send(exchange, 404, "{\"success\":false,\"message\":\"Student not found.\"}", "application/json");
                }
            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else {
            send(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}", "application/json");
        }
    }

    // =========================================================
    // ADMINISTRATOR - STAFF MANAGEMENT APIS
    // =========================================================

    static void adminStaff(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();

        if (method.equals("GET")) {

            try (Connection conn = DB.getConnection()) {
                String sql = "SELECT staff_id, name, COALESCE(active, TRUE) active FROM staff ORDER BY staff_id";
                StringBuilder sb = new StringBuilder("{\"success\":true,\"staff\":[");
                boolean first = true;

                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;

                        sb.append("{")
                                .append("\"staffId\":\"").append(escapeJson(rs.getString("staff_id"))).append("\",")
                                .append("\"name\":\"").append(escapeJson(rs.getString("name"))).append("\",")
                                .append("\"active\":").append(rs.getBoolean("active"))
                                .append("}");
                    }
                }
                sb.append("]}");

                send(exchange, 200, sb.toString(), "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else if (method.equals("POST")) {

            Map<String, String> data = parsePayload(exchange);
            String staffId = data.getOrDefault("staffId", "").trim();
            String name = data.getOrDefault("name", "").trim();
            String password = data.getOrDefault("password", "").trim();

            if (staffId.isEmpty() || name.isEmpty() || password.isEmpty()) {
                send(exchange, 400, "{\"success\":false,\"message\":\"Staff ID, name and password are required.\"}", "application/json");
                return;
            }

            try (Connection conn = DB.getConnection()) {

                try (PreparedStatement ps = DB.prepare(conn, "SELECT 1 FROM staff WHERE staff_id=?", staffId);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        send(exchange, 409, "{\"success\":false,\"message\":\"Staff ID already exists.\"}", "application/json");
                        return;
                    }
                }

                String insertSql = "INSERT INTO staff (staff_id, name, password, active) VALUES (?, ?, ?, TRUE)";
                DB.update(insertSql, staffId, name, password);

                send(exchange, 201, "{\"success\":true,\"message\":\"Staff account created successfully.\"}", "application/json");

            } catch (SQLException ex) {
                send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
            }

        } else {
            send(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}", "application/json");
        }
    }

    static void adminToggleStaff(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            send(exchange, 405, "{\"success\":false,\"message\":\"POST required\"}", "application/json");
            return;
        }

        Map<String, String> data = parsePayload(exchange);
        String staffId = data.getOrDefault("staffId", "").trim();

        if (staffId.isEmpty()) {
            send(exchange, 400, "{\"success\":false,\"message\":\"Staff ID is required.\"}", "application/json");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            String sql = "UPDATE staff SET active = NOT COALESCE(active, TRUE) WHERE staff_id=?";
            int updated = DB.update(sql, staffId);

            if (updated > 0) {
                send(exchange, 200, "{\"success\":true,\"message\":\"Staff status updated successfully.\"}", "application/json");
            } else {
                send(exchange, 404, "{\"success\":false,\"message\":\"Staff not found.\"}", "application/json");
            }

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    static void adminStats(HttpExchange exchange) throws IOException {

        if (handleCorsPreflight(exchange)) return;

        try (Connection conn = DB.getConnection()) {

            int totalStudents = countRows(conn, "students");
            int totalStaff = countRows(conn, "staff");
            int totalSubjects = countRows(conn, "subjects");
            int totalAttendance = countRows(conn, "attendance");
            int totalMarks = countRows(conn, "marks");

            String json = "{"
                    + "\"success\":true,"
                    + "\"totalStudents\":" + totalStudents + ","
                    + "\"totalStaff\":" + totalStaff + ","
                    + "\"totalSubjects\":" + totalSubjects + ","
                    + "\"totalAttendance\":" + totalAttendance + ","
                    + "\"totalMarks\":" + totalMarks
                    + "}";

            send(exchange, 200, json, "application/json");

        } catch (SQLException ex) {
            send(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(ex.getMessage()) + "\"}", "application/json");
        }
    }

    private static int countRows(Connection conn, String table) throws SQLException {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // =========================================================
    // UTILITIES & PARSERS
    // =========================================================

    private static boolean handleCorsPreflight(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    static Map<String, String> parsePayload(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (body.isEmpty()) {
            return new HashMap<>();
        }

        if (body.startsWith("{") && body.endsWith("}")) {
            Map<String, String> map = new HashMap<>();
            Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([truefalsnull0-9\\.\\-]+))");
            Matcher m = p.matcher(body);
            while (m.find()) {
                String key = m.group(1);
                String val = m.group(2) != null ? m.group(2) : m.group(3);
                if (val != null) {
                    map.put(key, val);
                }
            }
            return map;
        }

        return parseForm(body);
    }

    static Map<String, String> parseForm(String body) throws UnsupportedEncodingException {
        Map<String, String> result = new HashMap<>();
        if (body == null || body.isBlank()) return result;

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                result.put(key, value);
            }
        }
        return result;
    }

    static String getQueryParam(HttpExchange exchange, String key) {
        try {
            return parseQuery(exchange.getRequestURI().getRawQuery(), key);
        } catch (Exception e) {
            return null;
        }
    }

    static String parseQuery(String query, String key) throws UnsupportedEncodingException {
        if (query == null || query.isBlank()) return null;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String currentKey = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                if (currentKey.equals(key)) {
                    return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    static double parseDoubleSafe(String val) {
        if (val == null || val.isBlank()) return 0.0;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    static void send(HttpExchange exchange, int status, String response, String contentType) throws IOException {
        byte[] data = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        exchange.sendResponseHeaders(status, data.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    static String getContentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();

        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg")) return "image/svg+xml";

        return "application/octet-stream";
    }

    static String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}