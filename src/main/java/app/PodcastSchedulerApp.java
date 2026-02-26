package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Episode;
import model.EpisodeRepository;
import model.ScheduleConflictException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
/**
 * MAIN APPLICATION CLASS
 * This class builds the JavaFX GUI and connects it to the EpisodeRepository
 */
public class PodcastSchedulerApp extends Application {
    // Repository = where all episodes are created, scheduled, published, and saved
    private EpisodeRepository repo = new EpisodeRepository();

    @Override
    public void start(Stage stage){ //it creates the user interface and shows the window

        // ===== TITLE =====
        Label titleLabel = new Label("🎙 Podcast Scheduler Starter GUI");
        titleLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #E5E7EB;"   // light text
        );
 /* =========================
           ROOT LAYOUT (VERTICAL)
           ========================= */
        // Root VBox holds everything on the screen
        VBox root = new VBox(titleLabel); //VERTICAL LAYOUT
        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.setStyle("-fx-background-color: #000000;"); // BLACK background

        // ===== INPUT FIELDS =====
        // Episode title input
        TextField titleField = new TextField();
        titleField.setPromptText("Episode Title");
        titleField.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;" +
                        "-fx-prompt-text-fill: #9CA3AF;"
        );
// Duration input (must be a number)
        TextField durationField = new TextField();
        durationField.setPromptText("Duration (minutes)");
        durationField.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;" +
                        "-fx-prompt-text-fill: #9CA3AF;"
        );
        // Episode type (Regular or Bonus)
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Regular", "Bonus");
        typeBox.setPromptText("Episode Type");
        typeBox.setStyle(
                "-fx-background-color: #6a88b6;" +
                        "-fx-text-fill: #6d91b5;"
        );
// Date picker for scheduling
        DatePicker datePicker = new DatePicker();
        datePicker.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #6a88b6;"
        );
// Time input (HH:MM format)
        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM");
        timeField.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;" +
                        "-fx-prompt-text-fill: #9CA3AF;"
        );

        // ===== BUTTONS =====
        // Creates a new episode
        Button createBtn = new Button("Create Episode\uD83D\uDCDD");
        // Schedules the selected episode
        Button scheduleBtn = new Button("Schedule Episode⏸\uFE0F");
        // Publishes the selected episode
        Button publishBtn = new Button("Publish Selected✅");
        // Saves all episodes to file
        Button saveBtn = new Button("Save All\uD83D\uDC4C");

        createBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        scheduleBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        publishBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white;");
        saveBtn.setStyle("-fx-background-color: #7C3AED; -fx-text-fill: white;");

        // ===== LIST VIEW =====
        // Displays all episodes
        ListView<Episode> listView = new ListView<>();
        listView.setPrefHeight(250);
        listView.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-control-inner-background: #111827;" +
                        "-fx-text-fill: #E5E7EB;" +
                        "-fx-border-color: #374151;"
        );
        listView.getItems().addAll(repo.getEpisodes());

        // ===== FORM BOX =====
        // Holds all input fields and buttons
        VBox form = new VBox(8,
                new Label("\uD83D\uDCCETitle"), titleField,
                new Label("⏳Duration"), durationField,
                new Label("Type"), typeBox,
                new Label("\uD83D\uDCC6Date"), datePicker,
                new Label("⏰Time"), timeField,
                createBtn, scheduleBtn, publishBtn, saveBtn
        );

        // Label color inside form
        form.getChildren().filtered(n -> n instanceof Label)
                .forEach(n -> ((Label)n).setStyle("-fx-text-fill: #D1D5DB;"));

        form.setPadding(new Insets(10));
        form.setStyle(
                "-fx-background-color: #020617;" +
                        "-fx-border-color: #1E293B;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;"
        );
/* =========================
           MAIN LAYOUT
           ========================= */
        // Left = form, Right = episode list
        HBox layout = new HBox(20, form, listView); //HORIZONTAL LAYOUT
        root.getChildren().add(layout);
/* =========================
           BUTTON ACTIONS
           ========================= */

        /**
         * CREATE EPISODE
         * - Reads title, duration, and type
         * - Validates inputs
         * - Adds episode to repository and list
         */

        createBtn.setOnAction(e -> {
            try { //USED TO CATCH ERROR
                String title = titleField.getText();
                int duration = Integer.parseInt(durationField.getText());
                String type = typeBox.getValue();
// Input validation
                if (title.isEmpty() || type == null) {
                    showError("\uD83D\uDCA1Please fill in all fields.");
                    return;
                }
//Calls the repository
//Creates a new Episode object
                Episode ep = repo.createEpisode(type, title, duration);
                listView.getItems().add(ep);

                clearInputs(titleField, durationField, typeBox, datePicker, timeField);

            } catch (NumberFormatException ex) {
                showError("\uD83D\uDCA1Duration must be a number.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
/**
 * SCHEDULE EPISODE
 * - Requires selected episode
 * - Combines date + time
 * - Checks schedule conflicts
 */
        scheduleBtn.setOnAction(e -> {
            Episode selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("\uD83D\uDD0DSelect an episode to schedule.");
                return;
            }

            LocalDate date = datePicker.getValue();
            String timeText = timeField.getText();

            if (date == null || timeText.isBlank()) {
                showError("\uD83D\uDCDDPick a date and enter a time.");
                return;
            }

            try {
                LocalTime time = LocalTime.parse(timeText);
                LocalDateTime dt = LocalDateTime.of(date, time);

                repo.scheduleEpisode(selected, dt);
                listView.refresh();

            } catch (ScheduleConflictException ex) {
                showError("Schedule conflict: " + ex.getMessage());
            } catch (Exception ex) {
                showError("❗Invalid time format❗. Use HH:MM");
            }
        });
/**
 * PUBLISH EPISODE
 * - Publishes the selected episode
 * - Uses current time as publish time
 */
        publishBtn.setOnAction(e -> {
            Episode selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select an episode to publish.");
                return;
            }

            try {
                repo.publishEpisode(selected, LocalDateTime.now());
                listView.refresh();
            } catch (IllegalStateException ex) {
                showError("❌ " + ex.getMessage());
            }
        });

/**
 * SAVE EPISODES
 * - Writes all episodes to file
 */
        saveBtn.setOnAction(e -> {
            try {
                repo.saveToFile();
                showInfo("✅Episodes saved✅.");
            } catch (Exception ex) {
                showError("❌Could not save: " + ex.getMessage());
            }
        });
/* =========================
           STAGE SETUP
           ========================= */
        stage.setScene(new Scene(root, 900, 500));
        stage.setTitle("Podcast Scheduler");
        stage.show();
    }

    // ===== HELPERS =====
    // Clears all input fields after creating an episode
    private void clearInputs(TextField title, TextField duration, ComboBox<String> type,
                             DatePicker date, TextField time) {
        title.clear();
        duration.clear();
        type.getSelectionModel().clearSelection();
        date.setValue(null);
        time.clear();
    }
    // Shows error popup
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
    // Shows info popup
    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
