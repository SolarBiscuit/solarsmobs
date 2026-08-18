package com.solarbiscuit.faction;

/**
 * High-level stance toward players / other factions.
 * Future mobs should pick one of these and rely on {@link FactionRelations} for targeting.
 */
public enum Faction {
    /** Never initiates on players; never retaliates when hit by a player. */
    VIRTUE,
    /** Never initiates on players; retaliates when attacked first. */
    NEUTRAL,
    /** Hostile on sight (vanilla monster behavior). */
    EVIL,
    /** Neutral to players unless provoked; hostile to {@link #EVIL} and {@link #DEMONIC}. */
    HOLY,
    /** Opposed to {@link #HOLY}. */
    DEMONIC,
    /** Neutral until provoked; pack-aggros like zombified piglins. */
    ENDER
}
