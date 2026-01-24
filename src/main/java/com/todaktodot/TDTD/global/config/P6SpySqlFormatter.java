package com.todaktodot.TDTD.global.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.util.Locale;

/**
 * p6spy SQL 포맷터 - Hibernate FormatStyle을 사용하여 SQL을 예쁘게 출력
 */
public class P6SpySqlFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                 String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // JPQL 주석 제거 (/* ... */ 부분)
        String cleanSql = removeJpqlComment(sql);

        // SQL 포맷팅 (줄바꿈, 들여쓰기)
        String formattedSql = formatSql(category, cleanSql);

        return String.format("\n\n[SQL] %s | %dms%s\n", now, elapsed, formattedSql);
    }

    private String removeJpqlComment(String sql) {
        // /* JPQL 주석 */ 제거
        if (sql.contains("/*") && sql.contains("*/")) {
            int start = sql.indexOf("/*");
            int end = sql.indexOf("*/") + 2;
            if (start >= 0 && end > start) {
                return sql.substring(end).trim();
            }
        }
        return sql;
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // DDL, DML 구분하여 포맷팅
        if (Category.STATEMENT.getName().equals(category)) {
            String trimmedSql = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSql.startsWith("create") || trimmedSql.startsWith("alter") || trimmedSql.startsWith("drop")) {
                return FormatStyle.DDL.getFormatter().format(sql);
            }
        }
        return FormatStyle.BASIC.getFormatter().format(sql);
    }
}
