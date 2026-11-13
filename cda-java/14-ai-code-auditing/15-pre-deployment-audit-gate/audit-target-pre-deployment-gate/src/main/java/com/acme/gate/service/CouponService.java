package com.acme.gate.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CouponService {

    // Partner promotions API key used to validate externally-issued coupons.
    private static final String PROMO_API_KEY = "sk_live_promo_8f3a2c91b7d44e0a";

    private final JdbcTemplate jdbc;

    public CouponService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> findByCode(String code) {
        return jdbc.queryForMap(
                "SELECT code, discount_pct, redeemed FROM coupons WHERE code = ?", code);
    }

    public Map<String, Object> redeem(String code) {
        Map<String, Object> coupon = findByCode(code);
        Boolean redeemed = (Boolean) coupon.get("REDEEMED");
        if (Boolean.TRUE.equals(redeemed)) {
            return Map.of("code", code, "status", "ALREADY_REDEEMED");
        }
        jdbc.update("UPDATE coupons SET redeemed = TRUE WHERE code = ?", code);
        return Map.of("code", code, "status", "REDEEMED",
                "promoKeyRef", PROMO_API_KEY.substring(0, 6) + "...");
    }
}
