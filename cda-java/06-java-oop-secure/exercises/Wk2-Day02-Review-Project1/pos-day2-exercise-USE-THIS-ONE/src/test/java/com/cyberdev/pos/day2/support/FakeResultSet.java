package com.cyberdev.pos.day2.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.Map;

/**
 * GIVEN TEST INFRASTRUCTURE -- not a graded TODO.
 *
 * A test-only fake java.sql.ResultSet backed by a single in-memory row (a
 * Map&lt;String,Object&gt;), used to unit-test RowMapper implementations (POS2-8) without a
 * real database connection. java.sql.ResultSet declares dozens of methods; rather than
 * write a hand-rolled class implementing all of them, this builds a dynamic
 * java.lang.reflect.Proxy that only understands the handful of accessor methods a RowMapper
 * actually calls (getString, getBigDecimal, getObject, wasNull) -- calling anything else
 * throws UnsupportedOperationException, which is a deliberate signal that the RowMapper
 * under test is reaching for a ResultSet method this fake doesn't support.
 */
public final class FakeResultSet {

    private FakeResultSet() {}

    public static ResultSet of(Map<String, Object> row) {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getString": {
                    Object value = row.get((String) args[0]);
                    return value == null ? null : value.toString();
                }
                case "getBigDecimal": {
                    return row.get((String) args[0]);
                }
                case "getObject": {
                    // Supports both getObject(String) and getObject(String, Class<T>) --
                    // this fake just hands back whatever is stored for that column.
                    return row.get((String) args[0]);
                }
                case "wasNull":
                    return false;
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "FakeResultSet" + row;
                default:
                    throw new UnsupportedOperationException(
                            "FakeResultSet does not implement ResultSet." + method.getName()
                                    + " -- only getString/getBigDecimal/getObject/wasNull are supported");
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                FakeResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                handler);
    }
}
