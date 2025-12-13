// ============================================================================
// FILE: Quest.java
// PATH: src/main/java/com/elemental/battlepass/models/Quest.java
// ============================================================================
package com.elemental.battlepass.models;

public class Quest {
    private final String questId;
    private final int seasonId;
    private final String questType;
    private final String target;
    private final int requiredAmount;
    private final int timeLimitSeconds;
    private final boolean resetOnFail;
    private final String displayName;
    private final String description;

    public Quest(String questId, int seasonId, String questType, String target, int requiredAmount, 
                 int timeLimitSeconds, boolean resetOnFail, String displayName, String description) {
        this.questId = questId;
        this.seasonId = seasonId;
        this.questType = questType;
        this.target = target;
        this.requiredAmount = requiredAmount;
        this.timeLimitSeconds = timeLimitSeconds;
        this.resetOnFail = resetOnFail;
        this.displayName = displayName;
        this.description = description;
    }

    public String getQuestId() { return questId; }
    public int getSeasonId() { return seasonId; }
    public String getQuestType() { return questType; }
    public String getTarget() { return target; }
    public int getRequiredAmount() { return requiredAmount; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public boolean isResetOnFail() { return resetOnFail; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}