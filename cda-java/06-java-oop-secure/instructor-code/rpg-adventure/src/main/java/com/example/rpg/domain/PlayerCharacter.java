package com.example.rpg.domain;

import java.util.*;

/**
 * Encapsulates mutable gameplay state. Callers cannot directly mutate the inventory or replace
 * validated fields; all state changes go through behavior methods.
 */
public final class PlayerCharacter implements Combatant {
    private final long userId; private final String characterName; private int level, experience, health, maxHealth, gold;
    private String location; private final List<Item> inventory; private Item weapon, armor;
    public PlayerCharacter(long userId, String characterName) {
        if (userId <= 0) throw new IllegalArgumentException("user id must be positive");
        this.userId=userId; this.characterName=Domain.cleanName(characterName,"character name",40);
        level=1; maxHealth=100; health=maxHealth; inventory=new ArrayList<>(); location="Greenhaven";
    }
    public PlayerCharacter(long userId,String name,int level,int experience,int health,int maxHealth,int gold,String location,List<Item> inventory,Item weapon,Item armor) {
        if(userId<=0||level<1||maxHealth<1||health<0||health>maxHealth||experience<0||gold<0) throw new IllegalArgumentException("invalid save state");
        this.userId=userId; characterName=Domain.cleanName(name,"character name",40); this.level=level;this.experience=experience;this.health=health;this.maxHealth=maxHealth;this.gold=gold;this.location=Domain.cleanName(location,"location",60);this.inventory=new ArrayList<>(Objects.requireNonNull(inventory));this.weapon=weapon;this.armor=armor;
    }
    public long userId(){return userId;} public String characterName(){return characterName;} public String name(){return characterName;} public int level(){return level;} public int experience(){return experience;} public int health(){return health;} public int maxHealth(){return maxHealth;} public int gold(){return gold;} public String location(){return location;} public Item weapon(){return weapon;} public Item armor(){return armor;}
    /** Copy-out boundary: callers receive an unmodifiable snapshot, not the backing list. */
    public List<Item> inventory(){return List.copyOf(inventory);} public int attackPower(){return 8+(weapon==null?0:weapon.power())+level;}
    public void moveTo(String place){location=Domain.cleanName(place,"location",60);} public void collect(Item item){inventory.add(Objects.requireNonNull(item));} public void equip(Item item){if(!inventory.contains(item)||!item.isEquippable())throw new IllegalArgumentException("item must be owned and equippable"); if(item.type()==ItemType.WEAPON)weapon=item; else armor=item;}
    public void reward(int xp,int coins){experience+=Domain.positive(xp,"experience");gold+=Domain.positive(coins,"gold");while(experience>=level*100){experience-=level*100;level++;maxHealth+=10;health=maxHealth;}}
    public void takeDamage(int damage){health=Math.max(0,health-Domain.positive(damage,"damage"));} public void heal(){health=maxHealth;}
}
