package com.rti.legacy.reporting;

import com.rti.legacy.accounts.AccountService;
import com.rti.legacy.common.GlobalState;
import com.rti.legacy.ledger.LedgerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Produces account summaries and runs filter-based searches over accounts.
 */
@Service
public class ReportService {

    private final AccountService accounts;
    private final LedgerService ledger;
    private final GlobalState state;

    public ReportService(AccountService accounts, LedgerService ledger, GlobalState state) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.state = state;
    }

    public Map<String, Object> accountSummary(String accountId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("accountId", accountId);
        summary.put("balance", accounts.getBalance(accountId));
        summary.put("entries", ledger.history(accountId).size());

        state.cache.put("lastReport", accountId);
        state.cache.put("lastSummary", summary);
        return summary;
    }

    public List<Map<String, Object>> search(String filter) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String accountId : accounts.knownAccountIds()) {
            Map<String, Object> summary = accountSummary(accountId);
            if (matches(summary, filter)) {
                results.add(summary);
            }
        }
        return results;
    }

    private boolean matches(Map<String, Object> summary, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String op = null;
        for (String candidate : new String[]{">=", "<=", "!=", ">", "<", "=", "~"}) {
            if (filter.contains(candidate)) {
                op = candidate;
                break;
            }
        }
        if (op == null) {
            return true;
        }
        int idx = filter.indexOf(op);
        String field = filter.substring(0, idx).trim();
        String value = filter.substring(idx + op.length()).trim();

        Object raw = summary.get(field);
        if (raw == null) {
            raw = state.cache.get(field);
        }
        String actual = String.valueOf(raw);

        switch (op) {
            case ">":
                return toLong(actual) > toLong(value);
            case "<":
                return toLong(actual) < toLong(value);
            case ">=":
                return toLong(actual) >= toLong(value);
            case "<=":
                return toLong(actual) <= toLong(value);
            case "!=":
                return !actual.equals(value);
            case "~":
                return actual.contains(value);
            default:
                return actual.equals(value);
        }
    }

    private long toLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
