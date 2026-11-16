package com.acme.gate.web;

import com.acme.gate.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/lookup")
    public Map<String, Object> lookup(@RequestParam String code) {
        try {
            return couponService.findByCode(code);
        } catch (Exception e) {
            return Map.of("error", e.toString(), "trace", stackToString(e));
        }
    }

    @PostMapping("/redeem")
    public ResponseEntity<Map<String, Object>> redeem(@RequestParam String code) {
        return ResponseEntity.ok(couponService.redeem(code));
    }

    private String stackToString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : e.getStackTrace()) {
            sb.append(el.toString()).append("\n");
        }
        return sb.toString();
    }
}
