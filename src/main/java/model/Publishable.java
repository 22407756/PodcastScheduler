package model;

import java.time.LocalDateTime;

public interface Publishable {
    void schedule(LocalDateTime dateTime) throws ScheduleConflictException;
    void publish(LocalDateTime now);
    boolean canPublish(LocalDateTime now);
}
