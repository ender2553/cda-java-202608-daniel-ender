package com.example.paintcalc.repository;

import com.example.paintcalc.domain.PaintEstimate;
import java.util.List;

public interface EstimateRepository {
    void save(PaintEstimate estimate, com.example.paintcalc.domain.Room room);
    List<PaintEstimate> findByUserId(long userId);
}
