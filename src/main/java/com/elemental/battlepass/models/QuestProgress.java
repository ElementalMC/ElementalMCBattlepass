// ============================================================================
// FILE: QuestProgress.java
// PATH: src/main/java/com/elemental/battlepass/models/QuestProgress.java
// ============================================================================
package com.elemental.battlepass.models;

public class QuestProgress {
    private final String uuid;
    private final int seasonId;
    private final String questId;
    private int progress;
    private long startTimestamp;
    private boolean completed;

    public QuestProgress(String uuid, int seasonId, String questId, int progress, 
                        long startTimestamp, boolean completed) {
        this.uuid = uuid;
        this.seasonId = seasonId;
        this.questId = questId;
        this.progress = progress;
        this.startTimestamp = startTimestamp;
        this.completed = completed;
    }

    public String getUuid() { return uuid; }
    public int getSeasonId() { return seasonId; }
    public String getQuestId() { return questId; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public long getStartTimestamp() { return startTimestamp; }
    public void setStartTimestamp(long timestamp) { this.startTimestamp = timestamp; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
