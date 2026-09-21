package com.cyberdev.dndrpg.repository;

import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.ItemTemplate;
import com.cyberdev.dndrpg.model.MonsterTemplate;

import java.util.List;
import java.util.Optional;

/**
 * The contract for loading read-only game reference data (classes,
 * monsters, items). GameService depends ONLY on this interface -- it has
 * no idea, and no way to tell, whether the data underneath came from a
 * CSV file or a PostgreSQL database. That's the whole exercise.
 */
public interface GameDataRepository {
    List<ClassDefinition> findAllClasses();
    List<MonsterTemplate> findAllMonsters();
    Optional<MonsterTemplate> findMonsterById(String id);
    List<ItemTemplate> findAllItems();
}
