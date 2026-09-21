package com.cyberdev.dndrpg.model;

/**
 * Allow-listed set of playable character classes.
 * Using an enum here (rather than a raw String) means an invalid class
 * name can never even be represented in memory -- it's rejected at the
 * language level, not by a runtime "if" check we might forget to write.
 */
public enum CharacterClass {
    WARRIOR,
    MAGE,
    ROGUE,
    CLERIC
}
