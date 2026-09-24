package com.example.paintcalc.service;

import com.example.paintcalc.domain.PaintEstimate;
import com.example.paintcalc.domain.PaintProduct;
import com.example.paintcalc.domain.Room;

public interface PaintCalculator {
    PaintEstimate calculate(long userId, Room room, int coats, com.example.paintcalc.domain.PaintColor color, PaintProduct product);
}
