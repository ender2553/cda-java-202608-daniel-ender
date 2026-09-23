package com.cyberdev.dndrpg.service;

import com.cyberdev.dndrpg.event.AttackEvent;
import com.cyberdev.dndrpg.event.BossDefeatedEvent;
import com.cyberdev.dndrpg.event.DefeatEvent;
import com.cyberdev.dndrpg.event.GameEvent;
import com.cyberdev.dndrpg.event.ItemFoundEvent;
import com.cyberdev.dndrpg.event.LevelUpEvent;
import com.cyberdev.dndrpg.logging.AppLogger;
import com.cyberdev.dndrpg.logging.AuditLogger;
import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.ItemTemplate;
import com.cyberdev.dndrpg.model.Monster;
import com.cyberdev.dndrpg.model.MonsterTemplate;
import com.cyberdev.dndrpg.model.PlayerCharacter;
import com.cyberdev.dndrpg.repository.CharacterRepository;
import com.cyberdev.dndrpg.repository.GameDataRepository;
import com.cyberdev.dndrpg.repository.LeaderboardRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * DAY 3 SNAPSHOT -- the sealed GameEvent hierarchy has now arrived.
 * runEncounter and runBossEncounter no longer print as they go; each one
 * builds and returns a List&lt;GameEvent&gt;, and ConsoleUI is responsible
 * for turning that list into console output via an exhaustive switch.
 * This is the same "GameService has no idea how its output gets
 * displayed" discipline the repository layer already taught with
 * persistence -- now applied to presentation.
 *
 * New in this rework:
 *   - Level-up detection emits a LevelUpEvent (same before/after level
 *     comparison the old println version already did).
 *   - Defeating a non-boss monster has a flat, unweighted 30% chance to
 *     drop a random item from GameDataRepository.findAllItems() (kept
 *     deliberately simple -- no rarity tiers, no weighting).
 *   - Being defeated by the monster emits a DefeatEvent instead of a
 *     println.
 *   - A successful boss kill emits a BossDefeatedEvent. The existing
 *     AuditLogger.log("BOSS_DEFEATED", ...) call is UNCHANGED -- the event
 *     and the audit log are two different, both-still-needed things: one
 *     is what gets displayed to the player, the other is the permanent
 *     security record of who beat the boss and when.
 *
 * Day 2's repository-pattern material (LeaderboardRepository, AuditLogger)
 * is unchanged from before.
 */
@Service
public final class GameService {

    private static final int ROUNDS_PER_RUN = 3;

    /** Flat, unweighted item-drop chance on defeating a non-boss monster. No rarity tiers. */
    private static final double ITEM_DROP_CHANCE = 0.30;

    private final GameDataRepository gameDataRepository;
    private final CharacterRepository characterRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final CombatEngine combatEngine;
    private final AuditLogger auditLogger;
    private final Random random = new Random();

    public GameService(GameDataRepository gameDataRepository,
                        CharacterRepository characterRepository,
                        LeaderboardRepository leaderboardRepository,
                        CombatEngine combatEngine,
                        AuditLogger auditLogger) {
        this.gameDataRepository = gameDataRepository;
        this.characterRepository = characterRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.combatEngine = combatEngine;
        this.auditLogger = auditLogger;
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
     * Resolves one random non-boss encounter and returns every GameEvent
     * that happened during it, in order: one AttackEvent per swing, then
     * either a DefeatEvent (character lost), or on a win, an optional
     * LevelUpEvent, an optional ItemFoundEvent, in that order.
     */
    public List<GameEvent> runEncounter(PlayerCharacter character) {
        List<GameEvent> events = new ArrayList<>();
        List<MonsterTemplate> pool = gameDataRepository.findAllMonsters().stream()
                .filter(m -> !m.boss())
                .toList();
        MonsterTemplate template = pool.get(random.nextInt(pool.size()));
        Monster monster = Monster.of(template);

        while (!character.isDefeated() && !monster.isDefeated()) {
            events.add(combatEngine.resolveAttack(character, monster));
            if (monster.isDefeated()) break;
            events.add(combatEngine.resolveAttack(monster, character));
        }

        if (character.isDefeated()) {
            events.add(new DefeatEvent(character.getDisplayName(), template.name()));
            return events;
        }

        int previousLevel = character.getLevel();
        character.gainXp(template.xpReward());
        character.addGold(template.goldReward());
        if (character.getLevel() > previousLevel) {
            events.add(new LevelUpEvent(character.getName(), character.getLevel()));
        }

        // Flat, unweighted 30% item-drop chance -- deliberately simple,
        // no rarity tiers or weighting by item value.
        List<ItemTemplate> allItems = gameDataRepository.findAllItems();
        if (!allItems.isEmpty() && random.nextDouble() < ITEM_DROP_CHANCE) {
            ItemTemplate dropped = allItems.get(random.nextInt(allItems.size()));
            character.addItem(dropped);
            events.add(new ItemFoundEvent(character.getName(), dropped.name()));
        }

        characterRepository.save(character);
        return events;
    }

    /**
     * AUDIT-1: defeating the final boss is exactly the kind of event a
     * real system must be able to prove happened after the fact -- who,
     * and when. Logged via auditLogger before returning, regardless of
     * whether auditLogger is backed by InMemoryAuditLogRepository (today)
     * or JdbcAuditLogRepository (once a real database is wired in). This
     * audit call is unchanged from Day 2 -- the BossDefeatedEvent added
     * below it is a SEPARATE, additional thing (what gets displayed to
     * the player), not a replacement for the permanent security record.
     */
    public List<GameEvent> runBossEncounter(PlayerCharacter character) {
        List<GameEvent> events = new ArrayList<>();
        MonsterTemplate bossTemplate = gameDataRepository.findAllMonsters().stream()
                .filter(MonsterTemplate::boss)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No boss monster defined in game data"));
        Monster boss = Monster.of(bossTemplate);

        while (!character.isDefeated() && !boss.isDefeated()) {
            events.add(combatEngine.resolveAttack(character, boss));
            if (boss.isDefeated()) break;
            events.add(combatEngine.resolveAttack(boss, character));
        }

        if (boss.isDefeated()) {
            character.gainXp(bossTemplate.xpReward());
            character.addGold(bossTemplate.goldReward());
            characterRepository.save(character);
            auditLogger.log("BOSS_DEFEATED", character.getId(),
                    "boss=" + bossTemplate.name() + " character=" + character.getName());
            AppLogger.info(character.getDisplayName() + " defeated the boss: " + bossTemplate.name());
            events.add(new BossDefeatedEvent(character.getName(), bossTemplate.name()));
            return events;
        }
        events.add(new DefeatEvent(character.getDisplayName(), bossTemplate.name()));
        return events;
    }

    public void finalizeSession(List<PlayerCharacter> party) {
        for (PlayerCharacter c : party) {
            leaderboardRepository.recordScore(c.getName(), c.getScore());
        }
    }

    public List<LeaderboardRepository.LeaderboardEntry> leaderboard() {
        return leaderboardRepository.topScores(10);
    }

    public int roundsPerRun() {
        return ROUNDS_PER_RUN;
    }
}
