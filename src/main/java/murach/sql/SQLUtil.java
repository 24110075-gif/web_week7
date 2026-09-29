package murach.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SQLUtil {

    public static String getHtmlTable(ResultSet results) throws SQLException {
        StringBuilder htmlTable = new StringBuilder();
        ResultSetMetaData metaData = results.getMetaData();
        int columnCount = metaData.getColumnCount();

        htmlTable.append("<div class=\"table-responsive\">");
        htmlTable.append("<table class=\"sql-table\">");
        
        // Add header row
        htmlTable.append("<thead><tr>");
        for (int i = 1; i <= columnCount; i++) {
            htmlTable.append("<th>");
            htmlTable.append(escapeHtml(metaData.getColumnLabel(i)));
            htmlTable.append("</th>");
        }
        htmlTable.append("</tr></thead>");

        // Add body rows
        htmlTable.append("<tbody>");
        boolean hasRows = false;
        while (results.next()) {
            hasRows = true;
            htmlTable.append("<tr>");
            for (int i = 1; i <= columnCount; i++) {
                htmlTable.append("<td>");
                String value = results.getString(i);
                if (value == null) {
                    htmlTable.append("<span class=\"null-val\">NULL</span>");
                } else {
                    htmlTable.append(escapeHtml(value));
                }
                htmlTable.append("</td>");
            }
            htmlTable.append("</tr>");
        }

        if (!hasRows) {
            htmlTable.append("<tr><td colspan=\"").append(columnCount)
                     .append("\" class=\"no-records\">Không tìm thấy bản ghi nào.</td></tr>");
        }

        htmlTable.append("</tbody>");
        htmlTable.append("</table>");
        htmlTable.append("</div>");
        
        return htmlTable.toString();
    }

    public static String executeSQL(String sqlStatement) {
        return SQLGatewayDB.executeSQL(sqlStatement);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
