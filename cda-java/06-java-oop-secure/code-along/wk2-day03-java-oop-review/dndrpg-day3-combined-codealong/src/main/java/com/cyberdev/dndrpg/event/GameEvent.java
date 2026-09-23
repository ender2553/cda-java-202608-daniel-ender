package com.cyberdev.dndrpg.event;

/**
 * DAY 3 ADDITION. A closed set of outcomes that can happen while playing a
 * round of the game. Because the permits list is closed, a switch over this
 * type can be checked by the compiler for exhaustiveness -- no `default`
 * branch is needed (or wanted); see ConsoleUI's event-printing switch.
 *
 * INSTRUCTOR NOTE: deliberately kept OUTSIDE the com.cyberdev.dndrpg.exception
 * / GameException hierarchy. They are two different teaching points for two
 * different situations:
 *   - GameEvent (sealed, Day 3) models an EXPECTED outcome of playing the
 *     game -- an attack landing or missing, a character leveling up, an
 *     item dropping, a monster (or the player) being defeated. None of
 *     these are bugs; they are the normal, anticipated vocabulary of a
 *     combat round, so they are returned as data (accumulated into a
 *     List&lt;GameEvent&gt;) rather than thrown. Returning them as data also
 *     lets the compiler enforce exhaustive handling via sealed + switch.
 *   - GameException and its subtypes (exception package) model genuinely
 *     EXCEPTIONAL conditions: a corrupted persisted PlayerCharacter row, a
 *     failed login, a repository that can't reach its data store. These
 *     are thrown because there is no sensible "data" value to return in
 *     their place, and because the caller almost certainly wants the
 *     failure to propagate loudly rather than be silently absorbed into a
 *     result object.
 * A good discussion prompt for students: "why does a combat round return a
 * list of events but a bad login throws?" -- the answer is about which
 * outcomes are part of the normal gameplay vocabulary (attack, level-up,
 * item drop, defeat) versus which are defects/edge cases (corrupted state,
 * a rejected credential).
 */
public sealed interface GameEvent
        permits AttackEvent, DefeatEvent, LevelUpEvent, ItemFoundEvent, BossDefeatedEvent {
}
