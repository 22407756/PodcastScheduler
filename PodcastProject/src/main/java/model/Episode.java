package model;

import java.time.LocalDateTime;

/**
 * Abstract base class for all podcast episodes.
 * Contains shared attributes and behaviors by both Regular and Bonus episodes
 * It implement Publishable interface for scheduling and publishing functionality.
 */
public abstract class Episode implements Publishable {

    // Core episode attributes
    private String id;
    private String title;
    private int durationMinutes;

    // Current lifecycle state (DRAFT, SCHEDULED, PUBLISHED)
    private EpisodeStatus status;

    // Date and time when the episode is scheduled to be released
    private LocalDateTime scheduledDateTime;

    /**
     * Creates a new episode in DRAFT state.
     */
    public Episode(String id, String title, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.status = EpisodeStatus.DRAFT;
    }

    // Protected setters used internally by repository logic
    protected void setStatus(EpisodeStatus status) { this.status = status; }
    protected void setScheduledDateTime(LocalDateTime dt) { this.scheduledDateTime = dt; }

    // Public getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }
    public EpisodeStatus getStatus() { return status; }
    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }

    // Implemented by subclasses to identify episode type
    public abstract String getTypeLabel();

    // Schedules the episode for a date and time, updates its status
    @Override
    public void schedule(LocalDateTime dateTime) {
        this.scheduledDateTime = dateTime;
        this.status = EpisodeStatus.SCHEDULED;
    }

    /**
     Make sure Publishing is allowed only if:
     the episode has been scheduled (we already have a schedule date&time)
     the current time is after the scheduled time
     the episode status is SCHEDULED
     */
    @Override
    public boolean canPublish(LocalDateTime now) {
        return scheduledDateTime != null &&
                now.isAfter(scheduledDateTime)  &&
                status == EpisodeStatus.SCHEDULED;
    }

    // Publishes the episode by updating status to PUBLISHED if conditions are satisfied
    @Override
    public void publish(LocalDateTime now) {
        if (canPublish(now)) {
            this.status = EpisodeStatus.PUBLISHED;
        }
    }

    /**
     Readable string that represents an Episode object.
     This method is automatically called by JavaFX when an Episode
     object is displayed inside a ListView.
     IF the episode doesnt have a schedule datetime then the text "Not scheduled" will appear
     And if it was scheduled the date and time will be shown

     This makes it easy for users to understand:
     * - the type of episode (Regular or Bonus)
     * - the episode title
     * - the duration of episode
     * - its current status
     * - its scheduled or published date
     */
    @Override
    public String toString() {
        String dateText = (scheduledDateTime == null)
                ? "Not scheduled"
                : scheduledDateTime.toString();

        return String.format("[%s] %s (%d min) - %s | %s",
                getTypeLabel(), title, durationMinutes, status, dateText);
    }
}


