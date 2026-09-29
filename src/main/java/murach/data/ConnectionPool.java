package murach.data;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionPool {

    private static ConnectionPool pool = null;
    private static DataSource dataSource = null;

    private ConnectionPool() {
        try {
            InitialContext ic = new InitialContext();
            dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/murach");
        } catch (NamingException e) {
            System.err.println("NamingException in ConnectionPool: " + e.getMessage());
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }

    public Connection getConnection() {
        try {
            if (dataSource != null) {
                return dataSource.getConnection();
            } else {
                return DBUtil.getConnection();
            }
        } catch (SQLException e) {
            System.err.println("SQLException in getConnection: " + e.getMessage());
            return null;
        }
    }

    public void freeConnection(Connection c) {
        DBUtil.closeConnection(c);
    }
}
