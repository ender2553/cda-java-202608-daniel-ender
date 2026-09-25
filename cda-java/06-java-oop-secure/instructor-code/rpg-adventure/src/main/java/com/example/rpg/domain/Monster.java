package com.example.rpg.domain;

public final class Monster extends AbstractCombatant {
    private final int rewardXp, rewardGold;
    public Monster(String name,int health,int attackPower,int rewardXp,int rewardGold){super(name,health,attackPower);if(rewardXp<0||rewardGold<0)throw new IllegalArgumentException("invalid reward");this.rewardXp=rewardXp;this.rewardGold=rewardGold;}
    public int rewardXp(){return rewardXp;} public int rewardGold(){return rewardGold;}
}
