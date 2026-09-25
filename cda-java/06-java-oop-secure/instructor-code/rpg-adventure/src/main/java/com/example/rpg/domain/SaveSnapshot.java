package com.example.rpg.domain;
import java.util.List;
/** Persistence DTO/value snapshot: stores a safe copy instead of exposing a live domain object. */
public record SaveSnapshot(long userId,String characterName,int level,int experience,int health,int maxHealth,int gold,String location,List<Item> inventory,Item weapon,Item armor) {
    /** Copy-in boundary: the repository cannot later observe mutations to the original list. */
    public SaveSnapshot { inventory=List.copyOf(inventory); }
    public static SaveSnapshot from(PlayerCharacter p){return new SaveSnapshot(p.userId(),p.characterName(),p.level(),p.experience(),p.health(),p.maxHealth(),p.gold(),p.location(),p.inventory(),p.weapon(),p.armor());}
    public PlayerCharacter toCharacter(){return new PlayerCharacter(userId,characterName,level,experience,health,maxHealth,gold,location,inventory,weapon,armor);}
}
