package io.github.n3wang.voidcodex.model;

/**
 * Crew skills that can be trained.
 */
public enum Skill {
    ENGINEERING("Engineering"),
    MEDICAL("Medical"),
    NAVIGATION("Navigation"),
    COMBAT("Combat"),
    RESEARCH("Research");

    private final String displayName;

    Skill(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}

