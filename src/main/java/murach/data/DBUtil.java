package murach.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DBUtil {

    private static String getDbUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isEmpty()) return envUrl;
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl != null && !envDbUrl.isEmpty()) return envDbUrl;
        return "jdbc:postgresql://localhost:5432/murach";
    }

    private static String getDbUser() {
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isEmpty()) return envUser;
        return "postgres";
    }

    private static String getDbPass() {
        String envPass = System.getenv("DB_PASS");
        if (envPass != null && !envPass.isEmpty()) return envPass;
        return "02052010";
    }

    // JPA EntityManagerFactory using Jakarta Persistence
    private static EntityManagerFactory emf;

    private static java.util.Map<String, String> getJpaProperties() {
        java.util.Map<String, String> props = new java.util.HashMap<>();
        props.put("jakarta.persistence.jdbc.url", getDbUrl());
        props.put("jakarta.persistence.jdbc.user", getDbUser());
        props.put("jakarta.persistence.jdbc.password", getDbPass());
        return props;
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
            emf = Persistence.createEntityManagerFactory("murachPU", getJpaProperties());
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error initializing JPA EntityManagerFactory: " + e.getMessage());
        }
    }

    public static EntityManagerFactory getEmFactory() {
        if (emf == null) {
            try {
                emf = Persistence.createEntityManagerFactory("murachPU", getJpaProperties());
            } catch (Exception e) {
                System.err.println("Error creating EntityManagerFactory: " + e.getMessage());
            }
        }
        return emf;
    }

    public static Connection getConnection() throws SQLException {
        // 1. Try JNDI DataSource first (Tomcat context.xml)
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:/comp/env/jdbc/murach");
            if (ds != null) {
                return ds.getConnection();
            }
        } catch (NamingException | SQLException e) {
            // JNDI not available or error, fall back to direct DriverManager connection
        }

        // 2. Direct PostgreSQL connection fallback
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPass());
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
