package com.example.rpg.service;
import com.example.rpg.domain.Monster;
/** Factory abstraction keeps monster creation rules out of AdventureService. */
public interface MonsterFactory { Monster randomMonster(int playerLevel); }
