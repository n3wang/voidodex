package io.github.n3wang.voidcodex.model;

import java.util.Arrays;
import java.util.List;

/**
 * Crew member roles with associated primary skills.
 */
public enum CrewRole {
    CAPTAIN("Captain", Arrays.asList(Skill.NAVIGATION, Skill.COMBAT)),
    ENGINEER("Engineer", Arrays.asList(Skill.ENGINEERING, Skill.RESEARCH)),
    MEDIC("Medic", Arrays.asList(Skill.MEDICAL, Skill.RESEARCH)),
    PILOT("Pilot", Arrays.asList(Skill.NAVIGATION, Skill.ENGINEERING)),
    SOLDIER("Soldier", Arrays.asList(Skill.COMBAT, Skill.ENGINEERING)),
    SCIENTIST("Scientist", Arrays.asList(Skill.RESEARCH, Skill.MEDICAL));

    private final String displayName;
    private final List<Skill> primarySkills;

    CrewRole(String displayName, List<Skill> primarySkills) {
        this.displayName = displayName;
        this.primarySkills = primarySkills;
    }

    public String getDisplayName() { return displayName; }
    public List<Skill> getPrimarySkills() { return primarySkills; }
}

