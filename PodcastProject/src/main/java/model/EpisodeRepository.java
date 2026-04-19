package model;

import java.util.*;
import java.time.LocalDateTime;
import java.io.*;

/**
 Repository class responsible for managing Episode objects
 and handling file persistence.
 */
public class EpisodeRepository {

    // In-memory storage of all episodes
    private List<Episode> episodes = new ArrayList<>();

    // File used as a simple database
    private static final String FILE_NAME = "episodes.txt";

    /**
     Loads episodes from file when the application starts.
     */
    public EpisodeRepository() {
        try {
            loadFromFile();
        } catch (Exception e) {
            // If file is missing or corrupted, start with empty list
            System.err.println("Could not load episodes: " + e.getMessage());
        }
    }

    // Returns all episodes (used by GUI)
    public List<Episode> getEpisodes() { return episodes; }

    /**
     Factory method that creates an episode based on its type.
     */
    public Episode createEpisode(String type, String title, int duration) {
        //For unique identifier for each episode
        String id = UUID.randomUUID().toString();
        Episode ep;
        //creating episode type
        if (type.equalsIgnoreCase("Regular")) {
            ep = new RegularEpisode(id, title, duration);
        } else if (type.equalsIgnoreCase("Bonus")) {
            ep = new BonusEpisode(id, title, duration);
        } else {
            throw new IllegalArgumentException("Unknown episode type: " + type);
        }
        //store episodes in memory
        episodes.add(ep);
        return ep;
    }

    /**
     Schedules an episode if no other episode uses the same date and time.
     */
    public void scheduleEpisode(Episode ep, LocalDateTime dt)
            throws ScheduleConflictException {

// Check all existing episodes for scheduling conflicts
        for (Episode other : episodes) {
            // Skip checking against itself
            if (other == ep) continue;
            //If another episode already has same date & time, throws exception
            if (other.getScheduledDateTime() != null &&
                    other.getScheduledDateTime().equals(dt)) {

                throw new ScheduleConflictException(
                        "Episode '" + other.getTitle() + "' already scheduled at " + dt);
            }
        }
        // No conflicts found, schedule the ep
        ep.schedule(dt);
    }

    /**
     Saves all episodes to a text file.
     */
    public void saveToFile() throws EpisodePersistenceException {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            // Write each episode in serialized format
            for (Episode ep : episodes) {
                out.println(serialize(ep));
            }

        } catch (IOException e) {

            throw new EpisodePersistenceException("Error saving episodes", e);
        }
    }

    // Converts an Episode into a single line of text
    private String serialize(Episode ep) {
        return String.join("|",
                ep.getId(),
                ep.getTypeLabel(),
                ep.getTitle(),
                String.valueOf(ep.getDurationMinutes()),
                ep.getStatus().name(),
                (ep.getScheduledDateTime() == null ? "null" : ep.getScheduledDateTime().toString())
        );
    }

    /**
     Loads episodes from the text file into memory.
     */
    private void loadFromFile() throws EpisodePersistenceException {
        File file = new File(FILE_NAME);
        // If file does not exist there is nothing to load
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {
                // Split line into fields
                String[] parts = line.split("\\|");
                if (parts.length < 6) continue;

                String id = parts[0];
                String type = parts[1];
                String title = parts[2];
                int duration = Integer.parseInt(parts[3]); //can throw NumberFormatException
                EpisodeStatus status = EpisodeStatus.valueOf(parts[4]);

                LocalDateTime dt = parts[5].equals("null")
                        ? null
                        : LocalDateTime.parse(parts[5]);
                // Recreate the correct episode subtype
                Episode ep = type.equalsIgnoreCase("Regular")
                        ? new RegularEpisode(id, title, duration)
                        : new BonusEpisode(id, title, duration);
                // Restore episode state
                ep.setStatus(status);
                ep.setScheduledDateTime(dt);

                episodes.add(ep);
            }

        } catch (Exception e) {
            throw new EpisodePersistenceException("Unexpected error loading episodes", e);
        }
    }

    // Publishes the episode if allowed
    public void publishEpisode(Episode ep, LocalDateTime now) {
        ep.publish(now);
    }
}
