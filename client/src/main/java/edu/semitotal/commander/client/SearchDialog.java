package edu.semitotal.commander.client;

import edu.semitotal.commander.api.FileInfo;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class SearchDialog extends Dialog<List<FileInfo>> {

    private final String rootPath;
    private final ApiClient apiClient;
    private final TextField patternField;
    private final CheckBox caseSensitiveCheckBox;
    private final CheckBox searchContentCheckBox;
    private final ListView<FileInfo> resultsList;
    private final Label statusLabel;
    private final ProgressIndicator progressIndicator;
    private final PauseTransition debounceTimer;

    public SearchDialog(String rootPath, ApiClient apiClient) {
        this.rootPath = rootPath;
        this.apiClient = apiClient;
        this.patternField = new TextField();
        this.caseSensitiveCheckBox = new CheckBox("Case sensitive");
        this.searchContentCheckBox = new CheckBox("Search in file content");
        this.resultsList = new ListView<>();
        this.statusLabel = new Label();
        this.progressIndicator = new ProgressIndicator();
        this.debounceTimer = new PauseTransition(Duration.millis(500));

        progressIndicator.setMaxSize(20, 20);
        progressIndicator.setVisible(false);

        setupDialog();
        setupDebounce();
    }

    private void setupDialog() {
        setTitle("Search Files");
        setHeaderText("Search in: " + rootPath);

        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Pattern:"), 0, 0);
        grid.add(patternField, 1, 0);
        grid.add(caseSensitiveCheckBox, 1, 1);
        grid.add(searchContentCheckBox, 1, 2);

        resultsList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(FileInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText((item.isDirectory() ? "[DIR] " : "") + item.path());
                }
            }
        });
        resultsList.setPrefHeight(300);

        var statusBox = new HBox(10);
        statusBox.getChildren().addAll(statusLabel, progressIndicator);

        var resultsBox = new VBox(10);
        resultsBox.getChildren().addAll(new Label("Results:"), resultsList, statusBox);
        VBox.setVgrow(resultsList, Priority.ALWAYS);

        var content = new VBox(15);
        content.getChildren().addAll(grid, resultsBox);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return resultsList.getItems();
            }
            return null;
        });

        patternField.requestFocus();
    }

    private void setupDebounce() {
        debounceTimer.setOnFinished(_ -> performSearch());

        patternField.textProperty().addListener((_, _, newValue) -> {
            debounceTimer.stop();
            if (!newValue.isEmpty()) {
                debounceTimer.playFromStart();
            } else {
                resultsList.getItems().clear();
                statusLabel.setText("");
            }
        });

        caseSensitiveCheckBox.selectedProperty().addListener((_, _, _) -> {
            if (!patternField.getText().isEmpty()) {
                debounceTimer.stop();
                debounceTimer.playFromStart();
            }
        });

        searchContentCheckBox.selectedProperty().addListener((_, _, _) -> {
            if (!patternField.getText().isEmpty()) {
                debounceTimer.stop();
                debounceTimer.playFromStart();
            }
        });
    }

    private void performSearch() {
        var pattern = patternField.getText();
        if (pattern.isEmpty()) {
            return;
        }

        progressIndicator.setVisible(true);
        statusLabel.setText("Searching...");

        apiClient.search(
            rootPath,
            pattern,
            caseSensitiveCheckBox.isSelected(),
            searchContentCheckBox.isSelected()
        )
            .thenAccept(response -> Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                resultsList.getItems().setAll(response.results());
                var count = response.totalCount();
                if (count == 0) {
                    statusLabel.setText("No files found matching '" + pattern + "'");
                } else {
                    statusLabel.setText("Found " + count + " file(s) matching '" + pattern + "'");
                }
            }));
    }
}
