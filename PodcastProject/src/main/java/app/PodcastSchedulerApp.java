package app;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Episode;
import model.EpisodePersistenceException;
import model.EpisodeRepository;
import model.ScheduleConflictException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
/**
 This class is responsible for:
 * Building the graphical user interface (GUI)
 * Handling user interactions
 * Connecting the UI with the EpisodeRepository
 */
public class PodcastSchedulerApp extends Application {
    // Repository handles all episode logic & persistence

    private EpisodeRepository repo = new EpisodeRepository();
    //the first called method when the app starts
    @Override
    public void start(Stage stage){
        // ===== TITLE =====
        Label titleLabel = new Label("🎙 Podcast Scheduler Starter GUI");
        titleLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #E5E7EB;"   // light text
        );

        // Root container (vertical layout)
        VBox root = new VBox(titleLabel);
        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.setStyle("-fx-background-color: #000000;"); // BLACK background of the VBox title
        // TextField to have a title input
        TextField titleField = new TextField();
        titleField.setPromptText("Episode Title");
        titleField.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;" +
                        "-fx-prompt-text-fill: #9CA3AF;"
        );
        // TextField to have a duration input
        TextField durationField = new TextField();
        durationField.setPromptText("Duration (minutes)");
        durationField.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;" +
                        "-fx-prompt-text-fill: #9CA3AF;"
        );
        //ComboBox to be able to select the episode type
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Regular", "Bonus");
        typeBox.setPromptText("Episode Type");
        typeBox.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;"
        );
        //Date for scheduling
        DatePicker datePicker = new DatePicker();
        datePicker.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;"
        );
        //for input time
        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM");
        timeField.setStyle(
                "-fx-background-color: #1F2933;" +
                        "-fx-text-fill: #F9FAFB;" +
                        "-fx-prompt-text-fill: #9CA3AF;"
        );

        Button createBtn = new Button("Create Episode");
        Button scheduleBtn = new Button("Schedule Episode");
        Button publishBtn = new Button("Publish Selected");
        Button saveBtn = new Button("Save All");
        createBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        scheduleBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
        publishBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white;");
        saveBtn.setStyle("-fx-background-color: #7C3AED; -fx-text-fill: white;");

        //ListView to display all episodes using Episode.toString()
        ListView<Episode> listView = new ListView<>();
        listView.setPrefHeight(250);
        listView.setStyle(
                "-fx-background-color: #111827;" +
                        "-fx-control-inner-background: #111827;" +
                        "-fx-text-fill: #E5E7EB;" +
                        "-fx-border-color: #374151;"
        );


        // ===== FORM BOX =====
        // Holds all input fields and buttons
        //labels has been added related to each element then the bottoms below them
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
        //Horizontal layout will show form on the left and list on the right
        HBox layout = new HBox(20, form, listView);
        root.getChildren().add(layout);

        //ListView with existing episodes from repository
        listView.getItems().addAll(repo.getEpisodes());

        // ===== ACTIONS (UNCHANGED) =====
        // using multi line lambda expression(events -> {statements})
        createBtn.setOnAction(e -> {
            try {
                String title = titleField.getText();
                int duration = Integer.parseInt(durationField.getText());
                String type = typeBox.getValue();
                // Input validation
                if (title.isEmpty() || type == null) {
                    showError("\uD83D\uDCA1Please fill in all fields.");
                    return;
                }

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
            // for selecting an episode from the listview
            Episode selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select an episode to schedule.");
                return;
            }

            LocalDate date = datePicker.getValue();
            String timeText = timeField.getText();

            if (date == null || timeText.isBlank()) {
                showError("Pick a date and enter a time.");
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
 * - Publishes only if scheduled time has passed
 */
        publishBtn.setOnAction(e -> {
            Episode selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("⚠\uFE0FSelect an episode to publish.");
                return;
            }

            repo.publishEpisode(selected, LocalDateTime.now());
            listView.refresh();
        });
/**
 * SAVE EPISODES
 * - Writes all episodes to file
 */
        saveBtn.setOnAction(e -> {
            try {
                repo.saveToFile();
                showInfo("✅Episodes saved✅.");
            } catch(EpisodePersistenceException ex){
                showError("Persistence Error: " + ex.getMessage());
            }
            catch (Exception ex) {
                showError("Could not save: " + ex.getMessage());
            }
        });

        stage.setScene(new Scene(root, 700, 500));
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


