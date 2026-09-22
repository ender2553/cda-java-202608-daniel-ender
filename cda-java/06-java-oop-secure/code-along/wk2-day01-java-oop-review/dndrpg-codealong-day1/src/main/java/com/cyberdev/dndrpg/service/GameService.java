package com.cyberdev.dndrpg.service;

import com.cyberdev.dndrpg.logging.AppLogger;
import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.Monster;
import com.cyberdev.dndrpg.model.MonsterTemplate;
import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.repository.CharacterRepository;
import com.cyberdev.dndrpg.repository.GameDataRepository;

import java.util.List;
import java.util.Random;
import java.util.UUID;


public final class GameService {

    private static final int ROUNDS_PER_RUN = 3;

    private final GameDataRepository gameDataRepository;
    private final CharacterRepository characterRepository;
    private final CombatEngine combatEngine;
    private final Random random = new Random();

    public GameService(GameDataRepository gameDataRepository,
                        CharacterRepository characterRepository,
                        CombatEngine combatEngine) {
        this.gameDataRepository = gameDataRepository;
        this.characterRepository = characterRepository;
        this.combatEngine = combatEngine;
    }

    public List<ClassDefinition> availableClasses() {
        return gameDataRepository.findAllClasses();
    }

    public PlayerCharacter createCharacter(UUID playerId, String name, CharacterClass characterClass) {
        ClassDefinition definition = gameDataRepository.findAllClasses().stream()
                .filter(c -> c.characterClass() == characterClass)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown class: " + characterClass));
        PlayerCharacter character = new PlayerCharacter(UUID.randomUUID(), playerId, name, definition);
        characterRepository.save(character);
        return character;
    }

    /**
     * Runs one encounter for a single character against a random
     * non-boss monster. Returns true if the character survived.
     *
     */
    public boolean runEncounter(PlayerCharacter character) {
        List<MonsterTemplate> pool = gameDataRepository.findAllMonsters().stream()
                .filter(m -> !m.boss())
                .toList();
        MonsterTemplate template = pool.get(random.nextInt(pool.size()));
        Monster monster = Monster.of(template);

        while (!character.isDefeated() && !monster.isDefeated()) {
            System.out.println(combatEngine.resolveAttack(character, monster));
            if (monster.isDefeated()) break;
            System.out.println(combatEngine.resolveAttack(monster, character));
        }

        if (character.isDefeated()) {
            System.out.println("  " + character.getDisplayName() + " was defeated by " + template.name() + "...");
            return false;
        }

        int previousLevel = character.getLevel();
        character.gainXp(template.xpReward());
        character.addGold(template.goldReward());
        System.out.println("  Defeated " + template.name() + "! (+" + template.xpReward()
                + " XP, +" + template.goldReward() + " gold)");
        if (character.getLevel() > previousLevel) {
            System.out.println("  Leveled up to level " + character.getLevel() + "!");
        }
        characterRepository.save(character);
        return true;
    }

    /**
     * Runs the boss encounter against the party's chosen champion.
    */
    public boolean runBossEncounter(PlayerCharacter character) {
        MonsterTemplate bossTemplate = gameDataRepository.findAllMonsters().stream()
                .filter(MonsterTemplate::boss)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No boss monster defined in game data"));
        Monster boss = Monster.of(bossTemplate);

        while (!character.isDefeated() && !boss.isDefeated()) {
            System.out.println(combatEngine.resolveAttack(character, boss));
            if (boss.isDefeated()) break;
            System.out.println(combatEngine.resolveAttack(boss, character));
        }

        if (boss.isDefeated()) {
            character.gainXp(bossTemplate.xpReward());
            character.addGold(bossTemplate.goldReward());
            characterRepository.save(character);
            AppLogger.info(character.getDisplayName() + " defeated the boss: " + bossTemplate.name());
            return true;
        }
        return false;
    }

    public int roundsPerRun() {
        return ROUNDS_PER_RUN;
    }
}
