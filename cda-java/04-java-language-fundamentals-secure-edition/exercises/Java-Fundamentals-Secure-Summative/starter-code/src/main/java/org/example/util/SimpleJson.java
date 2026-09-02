package org.example.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON parser and writer.
 *
 * THIS CLASS IS PROVIDED FOR YOU -- it is infrastructure, not a graded
 * exercise. You do not need to modify it. Its job is only to turn JSON
 * text into plain Java objects (Map, List, String, Double, Boolean, null)
 * -- it does NOT know anything about security. That is exactly why the
 * output of SimpleJson.parse(...) must be treated as UNTRUSTED at the
 * trust boundary: nothing here validates ranges, formats, or allow-lists.
 * That validation is your job, in InputValidator and the *Parser classes.
 *
 * Supported JSON: objects, arrays, strings (with standard escapes),
 * numbers (returned as Double), true/false, null.
 */
public final class SimpleJson {

    private SimpleJson() { }

    /** Parses a JSON document into a Map (object) or List (array) of plain Java values. */
    public static Object parse(String text) {
        Parser p = new Parser(text);
        p.skipWhitespace();
        Object value = p.parseValue();
        p.skipWhitespace();
        if (!p.isAtEnd()) {
            throw new JsonParseException("Unexpected trailing content at position " + p.pos);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String text) {
        Object value = parse(text);
        if (!(value instanceof List)) {
            throw new JsonParseException("Expected a top-level JSON array but found: " +
                    (value == null ? "null" : value.getClass().getSimpleName()));
        }
        return (List<Object>) value;
    }

    public static String writePretty(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb, 0);
        return sb.toString();
    }

    private static void writeValue(Object value, StringBuilder sb, int indent) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            writeString((String) value, sb);
        } else if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                sb.append((long) d);
            } else {
                sb.append(d);
            }
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map) {
            writeObject((Map<?, ?>) value, sb, indent);
        } else if (value instanceof List) {
            writeArray((List<?>) value, sb, indent);
        } else {
            writeString(String.valueOf(value), sb);
        }
    }

    private static void writeObject(Map<?, ?> map, StringBuilder sb, int indent) {
        if (map.isEmpty()) { sb.append("{}"); return; }
        sb.append("{\n");
        int i = 0, n = map.size();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            indent(sb, indent + 1);
            writeString(String.valueOf(e.getKey()), sb);
            sb.append(": ");
            writeValue(e.getValue(), sb, indent + 1);
            if (++i < n) sb.append(",");
            sb.append("\n");
        }
        indent(sb, indent);
        sb.append("}");
    }

    private static void writeArray(List<?> list, StringBuilder sb, int indent) {
        if (list.isEmpty()) { sb.append("[]"); return; }
        sb.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            indent(sb, indent + 1);
            writeValue(list.get(i), sb, indent + 1);
            if (i < list.size() - 1) sb.append(",");
            sb.append("\n");
        }
        indent(sb, indent);
        sb.append("]");
    }

    private static void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append("  ");
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    public static class JsonParseException extends RuntimeException {
        public JsonParseException(String message) { super(message); }
    }

    private static final class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) { this.s = s; }

        boolean isAtEnd() { return pos >= s.length(); }

        void skipWhitespace() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        char peek() {
            if (pos >= s.length()) throw new JsonParseException("Unexpected end of input");
            return s.charAt(pos);
        }

        Object parseValue() {
            skipWhitespace();
            char c = peek();
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArrayInternal();
                case '"': return parseString();
                case 't': expectLiteral("true"); return Boolean.TRUE;
                case 'f': expectLiteral("false"); return Boolean.FALSE;
                case 'n': expectLiteral("null"); return null;
                default: return parseNumber();
            }
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char c = peek();
                if (c == ',') { pos++; continue; }
                if (c == '}') { pos++; break; }
                throw new JsonParseException("Expected ',' or '}' at position " + pos);
            }
            return map;
        }

        List<Object> parseArrayInternal() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                char c = peek();
                if (c == ',') { pos++; continue; }
                if (c == ']') { pos++; break; }
                throw new JsonParseException("Expected ',' or ']' at position " + pos);
            }
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= s.length()) throw new JsonParseException("Unterminated string");
                char c = s.charAt(pos++);
                if (c == '"') break;
                if (c == '\\') {
                    if (pos >= s.length()) throw new JsonParseException("Unterminated escape");
                    char esc = s.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u':
                            if (pos + 4 > s.length()) throw new JsonParseException("Bad unicode escape");
                            String hex = s.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                            break;
                        default: throw new JsonParseException("Unknown escape '\\" + esc + "'");
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Double parseNumber() {
            int start = pos;
            if (pos < s.length() && (s.charAt(pos) == '-' || s.charAt(pos) == '+')) pos++;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.'
                    || s.charAt(pos) == 'e' || s.charAt(pos) == 'E'
                    || s.charAt(pos) == '-' || s.charAt(pos) == '+')) {
                pos++;
            }
            String num = s.substring(start, pos);
            try {
                return Double.parseDouble(num);
            } catch (NumberFormatException e) {
                throw new JsonParseException("Invalid number literal '" + num + "' at position " + start);
            }
        }

        void expect(char c) {
            if (pos >= s.length() || s.charAt(pos) != c) {
                throw new JsonParseException("Expected '" + c + "' at position " + pos);
            }
            pos++;
        }

        void expectLiteral(String literal) {
            if (pos + literal.length() > s.length() || !s.startsWith(literal, pos)) {
                throw new JsonParseException("Expected literal '" + literal + "' at position " + pos);
            }
            pos += literal.length();
        }
    }
}
