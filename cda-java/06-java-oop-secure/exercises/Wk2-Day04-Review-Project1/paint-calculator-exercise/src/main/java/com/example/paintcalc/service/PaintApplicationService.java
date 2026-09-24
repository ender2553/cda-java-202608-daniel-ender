package com.example.paintcalc.service;

import com.example.paintcalc.domain.*;
import com.example.paintcalc.repository.EstimateRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public final class PaintApplicationService {
    private final EstimateRepository estimates;
    private final PaintCalculatorFactory calculatorFactory;
    public PaintApplicationService(EstimateRepository estimates, PaintCalculatorFactory calculatorFactory) { this.estimates = estimates; this.calculatorFactory = calculatorFactory; }
    public PaintEstimate estimate(long userId, Room room, int coats, PaintColor color, PaintProduct product) {
        PaintEstimate result = calculatorFactory.forRoom().calculate(userId, room, coats, color, product);
        estimates.save(result, room);
        return result;
    }
    public List<PaintEstimate> history(long userId) { return estimates.findByUserId(userId); }
}
