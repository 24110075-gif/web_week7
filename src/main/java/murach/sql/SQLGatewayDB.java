package murach.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import murach.data.DBUtil;

public class SQLGatewayDB {

    /**
     * Executes the given SQL statement using a PreparedStatement.
     * Separated from SQLGatewayServlet as required.
     *
     * @param sqlStatement The SQL query or update command.
     * @return HTML representation of results or execution status.
     */
    public static String executeSQL(String sqlStatement) {
        if (sqlStatement == null || sqlStatement.trim().isEmpty()) {
            return "<div class=\"result-box result-info\">Vui lòng nhập câu lệnh SQL để thực thi.</div>";
        }

        sqlStatement = sqlStatement.trim();
        String sqlResult = "";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = DBUtil.getConnection();
            if (connection == null) {
                return "<div class=\"result-box result-error\">Không thể kết nối đến cơ sở dữ liệu PostgreSQL.</div>";
            }

            // Using PreparedStatement instead of Statement
            ps = connection.prepareStatement(sqlStatement);

            // Determine if the statement is a SELECT query
            if (sqlStatement.length() >= 6) {
                String sqlType = sqlStatement.substring(0, 6);

                if (sqlType.equalsIgnoreCase("select")) {
                    rs = ps.executeQuery();
                    sqlResult = SQLUtil.getHtmlTable(rs);
                } else {
                    int i = ps.executeUpdate();
                    if (i == 0) {
                        sqlResult = "<div class=\"result-box result-success\">"
                                + "<strong>Thành công:</strong> Câu lệnh DDL/SQL đã được thực thi thành công."
                                + "</div>";
                    } else {
                        sqlResult = "<div class=\"result-box result-success\">"
                                + "<strong>Thành công:</strong> Câu lệnh đã được thực thi thành công.<br>"
                                + "Số dòng bị ảnh hưởng: <strong>" + i + "</strong> dòng."
                                + "</div>";
                    }
                }
            } else {
                sqlResult = "<div class=\"result-box result-warning\">Câu lệnh SQL không hợp lệ (độ dài quá ngắn).</div>";
            }
        } catch (SQLException e) {
            sqlResult = "<div class=\"result-box result-error\">"
                    + "<strong>Lỗi khi thực thi câu lệnh SQL:</strong><br>"
                    + "<pre>" + escapeHtml(e.getMessage()) + "</pre>"
                    + "</div>";
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            DBUtil.closeConnection(connection);
        }

        return sqlResult;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
