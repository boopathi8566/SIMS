package esec.sims;
import java.sql.*;

public class DB {
    // Default local credentials (used as fallback when running locally)
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "1234";

    public static Connection getConnection() throws SQLException {
        String envUrl = System.getenv("MYSQL_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getenv("DATABASE_URL");
        }
        String url = (envUrl != null && !envUrl.isBlank()) ? envUrl : DEFAULT_URL;

        String envUser = System.getenv("MYSQL_USER");
        if (envUser == null || envUser.isBlank()) {
            envUser = System.getenv("DB_USER");
        }
        String user = (envUser != null && !envUser.isBlank()) ? envUser : DEFAULT_USER;

        String envPass = System.getenv("MYSQL_PASSWORD");
        if (envPass == null || envPass.isBlank()) {
            envPass = System.getenv("DB_PASSWORD");
        }
        String password = (envPass != null && !envPass.isBlank()) ? envPass : DEFAULT_PASSWORD;

        return DriverManager.getConnection(url, user, password);
    }

    public static PreparedStatement prepare(Connection c, String sql, Object... values)
            throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
        return ps;
    }

    public static int update(String sql, Object... values) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = prepare(c, sql, values)) {
            return ps.executeUpdate();
        }
    }
}
