package murach.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DBUtil {

    private static String dbUrl = "jdbc:postgresql://localhost:5432/murach";
    private static String dbUser = "postgres";
    private static String dbPass = "02052010";

    private static EntityManagerFactory emf;

    static {
        loadConfig();
        try {
            Class.forName("org.postgresql.Driver");
            emf = createEmf();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo JPA EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        String envDbUrl = getEnvValue("DB_URL");
        if (envDbUrl == null || envDbUrl.isEmpty()) {
            envDbUrl = getEnvValue("DATABASE_URL");
        }

        if (envDbUrl != null && !envDbUrl.isEmpty()) {
            envDbUrl = envDbUrl.trim();
            if (envDbUrl.startsWith("postgres://") || envDbUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(envDbUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        dbUser = parts[0];
                        dbPass = parts[1];
                    }
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    dbUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + path;
                    String query = uri.getQuery();
                    if (query != null && !query.isEmpty()) {
                        dbUrl += "?" + query;
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi phân tích DATABASE_URL: " + e.getMessage());
                    dbUrl = envDbUrl.startsWith("jdbc:") ? envDbUrl : "jdbc:" + envDbUrl;
                }
            } else if (envDbUrl.startsWith("jdbc:")) {
                dbUrl = envDbUrl;
            } else {
                dbUrl = "jdbc:" + envDbUrl;
            }
        }

        String envUser = getEnvValue("DB_USER");
        if (envUser != null && !envUser.isEmpty()) {
            dbUser = envUser.trim();
        }

        String envPass = getEnvValue("DB_PASS");
        if (envPass != null && !envPass.isEmpty()) {
            dbPass = envPass.trim();
        }
    }

    private static String getEnvValue(String key) {
        String val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#") || !line.contains("=")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts[0].trim().equalsIgnoreCase(key)) {
                        String v = parts[1].trim();
                        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                            v = v.substring(1, v.length() - 1);
                        }
                        return v;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static EntityManagerFactory createEmf() {
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", dbUrl);
        props.put("jakarta.persistence.jdbc.user", dbUser);
        props.put("jakarta.persistence.jdbc.password", dbPass);
        props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
        return Persistence.createEntityManagerFactory("murachPU", props);
    }

    public static synchronized EntityManagerFactory getEmFactory() {
        if (emf == null || !emf.isOpen()) {
            loadConfig();
            try {
                emf = createEmf();
            } catch (Exception e) {
                System.err.println("Không thể tạo EntityManagerFactory: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Không thể kết nối đến cơ sở dữ liệu (" + dbUrl + "): " + e.getMessage(), e);
            }
        }
        return emf;
    }

    public static Connection getConnection() throws SQLException {
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:/comp/env/jdbc/murach");
            if (ds != null) {
                return ds.getConnection();
            }
        } catch (NamingException | SQLException ignored) {
        }

        loadConfig();
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    public static void closeStatement(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing statement: " + e.getMessage());
        }
    }

    public static void closePreparedStatement(Statement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing prepared statement: " + e.getMessage());
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing result set: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
