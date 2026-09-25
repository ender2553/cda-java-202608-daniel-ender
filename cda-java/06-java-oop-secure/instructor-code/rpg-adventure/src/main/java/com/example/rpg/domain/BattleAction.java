package com.example.rpg.domain;
public record BattleAction(String monsterName,boolean won,int damageTaken) implements AdventureAction { public BattleAction { monsterName=Domain.cleanName(monsterName,"monster name",40); if(damageTaken<0)throw new IllegalArgumentException("damage cannot be negative"); } public String summary(){return (won?"Defeated ":"Lost to ")+monsterName;} }
