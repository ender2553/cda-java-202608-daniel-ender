package com.example.rpg.domain;
/** Shared validated base class demonstrating abstraction and inherited polymorphic behavior. */
public abstract non-sealed class AbstractCombatant implements Combatant {
    private final String name; private final int health; private final int attackPower;
    protected AbstractCombatant(String name,int health,int attackPower){this.name=Domain.cleanName(name,"combatant name",40);if(health<1||attackPower<0)throw new IllegalArgumentException("invalid combatant");this.health=health;this.attackPower=attackPower;}
    public final String name(){return name;} public final int health(){return health;} public final int attackPower(){return attackPower;}
}
