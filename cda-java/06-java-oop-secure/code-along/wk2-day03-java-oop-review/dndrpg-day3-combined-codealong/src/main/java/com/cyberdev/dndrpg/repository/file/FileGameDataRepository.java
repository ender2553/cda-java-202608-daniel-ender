package com.cyberdev.dndrpg.repository.file;

import com.cyberdev.dndrpg.exception.GameDataException;
import com.cyberdev.dndrpg.logging.AppLogger;
import com.cyberdev.dndrpg.model.CharacterClass;
import com.cyberdev.dndrpg.model.ClassDefinition;
import com.cyberdev.dndrpg.model.ItemTemplate;
import com.cyberdev.dndrpg.model.ItemType;
import com.cyberdev.dndrpg.model.MonsterTemplate;
import com.cyberdev.dndrpg.repository.GameDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * PROVIDED IMPLEMENTATION -- loads game reference data from CSV files
 * bundled as classpath resources. This is what the game runs on out of
 * the box, with zero database setup required.
 *
 * Notice what GameService never has to know: this class reads files,
 * parses text, and throws GameDataException on bad rows. None of that
 * leaks past the GameDataRepository interface -- which is exactly what
 * makes it possible to replace this whole class with
 * JdbcGameDataRepository later without touching GameService at all.
 */
@Repository
@Profile("!jdbc")
public final class FileGameDataRepository implements GameDataRepository {

    private final List<ClassDefinition> classes;
    private final List<MonsterTemplate> monsters;
    private final List<ItemTemplate> items;

    public FileGameDataRepository() {
        this.classes = loadClasses();
        this.monsters = loadMonsters();
        this.items = loadItems();
        AppLogger.info("Loaded game data from CSV files: "
                + classes.size() + " classes, " + monsters.size() + " monsters, "
                + items.size() + " items.");
    }

    @Override
    public List<ClassDefinition> findAllClasses() {
        return List.copyOf(classes);
    }

    @Override
    public List<MonsterTemplate> findAllMonsters() {
        return List.copyOf(monsters);
    }

    @Override
    public Optional<MonsterTemplate> findMonsterById(String id) {
        return monsters.stream().filter(m -> m.id().equals(id)).findFirst();
    }

    @Override
    public List<ItemTemplate> findAllItems() {
        return List.copyOf(items);
    }

    private List<ClassDefinition> loadClasses() {
        List<ClassDefinition> result = new ArrayList<>();
        for (String[] row : readCsv("/gamedata/classes.csv", 5)) {
            try {
                result.add(new ClassDefinition(
                        CharacterClass.valueOf(row[0].trim()),
                        Integer.parseInt(row[1].trim()),
                        Integer.parseInt(row[2].trim()),
                        Integer.parseInt(row[3].trim()),
                        row[4].trim()
                ));
            } catch (RuntimeException e) {
                // Fail closed at the WHOLE-FILE level for reference data:
                // a corrupted class definition means the game's balance
                // is unknown, so we refuse to start rather than silently
                // dropping one row and hoping nobody notices.
                throw new GameDataException("Malformed row in classes.csv: " + Arrays.toString(row), e);
            }
        }
        if (result.isEmpty()) {
            throw new GameDataException("classes.csv loaded zero class definitions");
        }
        return result;
    }

    private List<MonsterTemplate> loadMonsters() {
        List<MonsterTemplate> result = new ArrayList<>();
        for (String[] row : readCsv("/gamedata/monsters.csv", 8)) {
            try {
                result.add(new MonsterTemplate(
                        row[0].trim(),
                        row[1].trim(),
                        Integer.parseInt(row[2].trim()),
                        Integer.parseInt(row[3].trim()),
                        Integer.parseInt(row[4].trim()),
                        Integer.parseInt(row[5].trim()),
                        Integer.parseInt(row[6].trim()),
                        Boolean.parseBoolean(row[7].trim())
                ));
            } catch (RuntimeException e) {
                throw new GameDataException("Malformed row in monsters.csv: " + Arrays.toString(row), e);
            }
        }
        if (result.isEmpty()) {
            throw new GameDataException("monsters.csv loaded zero monster definitions");
        }
        return result;
    }

    private List<ItemTemplate> loadItems() {
        List<ItemTemplate> result = new ArrayList<>();
        for (String[] row : readCsv("/gamedata/items.csv", 6)) {
            try {
                result.add(new ItemTemplate(
                        row[0].trim(),
                        row[1].trim(),
                        ItemType.valueOf(row[2].trim()),
                        Integer.parseInt(row[3].trim()),
                        Integer.parseInt(row[4].trim()),
                        Integer.parseInt(row[5].trim())
                ));
            } catch (RuntimeException e) {
                throw new GameDataException("Malformed row in items.csv: " + Arrays.toString(row), e);
            }
        }
        if (result.isEmpty()) {
            throw new GameDataException("items.csv loaded zero item definitions");
        }
        return result;
    }

    /**
     * Reads a classpath CSV resource, skipping the header row and any
     * blank lines, and validates that every row has the expected number
     * of columns before handing it back -- treat file content as
     * untrusted, even when it shipped inside our own jar.
     */
    private List<String[]> readCsv(String resourcePath, int expectedColumns) {
        List<String[]> rows = new ArrayList<>();
        try (InputStream in = FileGameDataRepository.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new GameDataException("Missing game data resource: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) { first = false; continue; } // skip header
                    if (line.isBlank()) continue;
                    String[] columns = line.split(",", -1);
                    if (columns.length != expectedColumns) {
                        throw new GameDataException("Expected " + expectedColumns
                                + " columns in " + resourcePath + " but found " + columns.length
                                + ": " + line);
                    }
                    rows.add(columns);
                }
            }
        } catch (IOException e) {
            throw new GameDataException("Could not read game data resource: " + resourcePath, e);
        }
        return rows;
    }
}
