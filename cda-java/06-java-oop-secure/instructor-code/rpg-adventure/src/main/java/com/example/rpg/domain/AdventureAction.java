package com.example.rpg.domain;
public sealed interface AdventureAction permits ExploreAction, BattleAction { String summary(); }
