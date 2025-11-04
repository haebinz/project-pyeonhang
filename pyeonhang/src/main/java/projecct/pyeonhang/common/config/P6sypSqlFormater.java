package projecct.pyeonhang.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class P6sypSqlFormater implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared,
                                String sql, String url) {

        sql = formatSql(category, sql);
        LocalDateTime currentDate = LocalDateTime.now();
        String nowDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return nowDate + " | OperationTime : " + elapsed + "ms | " + sql;
    }


    private String formatSql(String category, String sql) {

        if (category.contains("statement") && sql.trim().toLowerCase(Locale.ROOT).startsWith("create")) {
            return sql;
        }

        // Hibernate SQL 포맷 적용
        if (category.equals("statement")) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("select") ||
                    trimmedSQL.startsWith("insert") ||
                    trimmedSQL.startsWith("update") ||
                    trimmedSQL.startsWith("delete")) {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
                return "\n(P6Spy sql,Hibernate format):\n" + sql;
            }
        }

        return "\nP6Spy sql:\n" + sql;


    }

}
