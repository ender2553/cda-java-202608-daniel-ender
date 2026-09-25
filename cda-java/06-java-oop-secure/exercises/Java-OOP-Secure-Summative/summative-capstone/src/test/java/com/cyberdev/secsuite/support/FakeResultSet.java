package com.cyberdev.secsuite.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Map;

/**
 * GIVEN TEST INFRASTRUCTURE -- not a graded TODO.
 *
 * A test-only fake java.sql.ResultSet backed by a single in-memory row (a
 * Map&lt;String,Object&gt;), used to unit-test the RowMapper implementations (SEC-3, SEC-6,
 * SEC-8, SEC-9) WITHOUT a database connection. Adapted from the QuickPay POS FakeResultSet.
 *
 * java.sql.ResultSet declares dozens of methods; rather than a hand-rolled class implementing
 * all of them, this is a dynamic java.lang.reflect.Proxy that understands only the accessors a
 * RowMapper legitimately calls, READ BY COLUMN NAME:
 *   getString(String), getObject(String), getObject(String, Class), getTimestamp(String),
 *   getInt(String), getLong(String), getBigDecimal(String), wasNull().
 * Two deliberate traps, matching the RowMapper contract taught in SEC-3:
 *   - rs.next() throws: JdbcTemplate has ALREADY positioned the ResultSet on the row before it
 *     calls mapRow; a mapper that calls next() itself skips rows.
 *   - positional reads (getString(2), getObject(1, Long.class), ...) throw: columns must be
 *     read by name so reordering a SELECT list cannot silently shift every value.
 * Anything else throws UnsupportedOperationException naming the method, which is a deliberate
 * signal that the mapper under test is reaching for something this fake doesn't support.
 *
 * Column values: put a Long (or its String form) for Long columns, a String for text columns,
 * a java.sql.Timestamp (or null) for timestamp columns. A column that is absent from the map
 * reads as SQL NULL.
 */
public final class FakeResultSet {

    private FakeResultSet() {
    }

    public static ResultSet of(Map<String, Object> row) {
        boolean[] lastWasNull = {false};
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "FakeResultSet" + row;
                case "next":
                    throw new UnsupportedOperationException(
                            "RowMapper.mapRow must NOT call rs.next() -- JdbcTemplate has already positioned the row");
                case "wasNull":
                    return lastWasNull[0];
                default:
                    break;
            }
            if (args != null && args.length >= 1 && !(args[0] instanceof String)) {
                throw new UnsupportedOperationException("ResultSet." + name + "(" + args[0]
                        + ") reads a column by POSITION -- read every column by NAME instead");
            }
            String column = args == null || args.length == 0 ? null : (String) args[0];
            Object value = column == null ? null : row.get(column);
            lastWasNull[0] = value == null;
            switch (name) {
                case "getString":
                    return value == null ? null : value.toString();
                case "getObject":
                    if (args.length == 2 && args[1] == Long.class && value instanceof String s) {
                        return Long.valueOf(s);
                    }
                    return value;
                case "getTimestamp":
                    if (value == null || value instanceof Timestamp) {
                        return value;
                    }
                    throw new IllegalStateException("column " + column + " is not a Timestamp in this fake row");
                case "getInt":
                    return value == null ? 0 : ((Number) value).intValue();
                case "getLong":
                    return value == null ? 0L : ((Number) value).longValue();
                case "getBigDecimal":
                    return value;
                default:
                    throw new UnsupportedOperationException("FakeResultSet does not implement ResultSet." + name
                            + " -- supported: getString/getObject/getTimestamp/getInt/getLong/getBigDecimal/wasNull");
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                FakeResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                handler);
    }
}
