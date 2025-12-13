// ============================================================================
// FILE: Season.java
// PATH: src/main/java/com/elemental/battlepass/models/Season.java
// ============================================================================
package com.elemental.battlepass.models;

public class Season {
    private final int seasonId;
    private final String seasonName;
    private final long startDate;
    private final Long endDate;
    private final boolean active;

    public Season(int seasonId, String seasonName, long startDate, Long endDate, boolean active) {
        this.seasonId = seasonId;
        this.seasonName = seasonName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public int getSeasonId() { return seasonId; }
    public String getSeasonName() { return seasonName; }
    public long getStartDate() { return startDate; }
    public Long getEndDate() { return endDate; }
    public boolean isActive() { return active; }
}