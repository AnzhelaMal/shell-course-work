package edu.semitotal.commander.client;

import edu.semitotal.commander.api.PanelPosition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.nio.file.Paths;

public class MainView extends BorderPane {

    private final FilePanel leftPanel;
    private final FilePanel rightPanel;
    private final ToolBar toolbar;
    private final StatusBar statusBar;
    private final ApiClient apiClient;
    private FilePanel activePanel;

    public MainView() {
        this.apiClient = new ApiClient("http://localhost:7890", this::handleApiError);
        this.leftPanel = new FilePanel(apiClient, "Left Panel", PanelPosition.LEFT);
        this.rightPanel = new FilePanel(apiClient, "Right Panel", PanelPosition.RIGHT);
        this.toolbar = createToolbar();
        this.statusBar = new StatusBar();
        this.activePanel = leftPanel;

        // Wire up activation callbacks
        leftPanel.setOnActivationRequested(() -> setActivePanel(leftPanel));
        rightPanel.setOnActivationRequested(() -> setActivePanel(rightPanel));

        leftPanel.setActive(true);
        setupLayout();
        setupKeyboardShortcuts();
        loadState();
    }

    private void handleApiError(Throwable error) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Operation Failed");
        alert.setHeaderText(null);
        alert.setContentText(error.getMessage());
        alert.showAndWait();
    }

    private void setupLayout() {
        setTop(toolbar);

        var centerPane = new HBox(10);
        centerPane.setPadding(new Insets(10));
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        centerPane.getChildren().addAll(leftPanel, rightPanel);

        setCenter(centerPane);
        setBottom(statusBar);

        leftPanel.setOnMouseClicked(_ -> setActivePanel(leftPanel));
        rightPanel.setOnMouseClicked(_ -> setActivePanel(rightPanel));
    }

    private ToolBar createToolbar() {
        var toolbar = new ToolBar();

        var btnRefresh = new Button("Refresh (F5)");
        btnRefresh.setOnAction(_ -> activePanel.refresh());

        var btnCopy = new Button("Copy (F5)");
        btnCopy.setOnAction(_ -> copyFiles());

        var btnMove = new Button("Move (F6)");
        btnMove.setOnAction(_ -> moveFiles());

        var btnDelete = new Button("Delete (F8)");
        btnDelete.setOnAction(_ -> deleteFiles());

        var btnSearch = new Button("Search (Ctrl+F)");
        btnSearch.setOnAction(_ -> showSearchDialog());

        toolbar.getItems().addAll(
            btnRefresh, new Separator(),
            btnCopy, btnMove, btnDelete, new Separator(),
            btnSearch
        );

        return toolbar;
    }

    private void setupKeyboardShortcuts() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                activePanel.refresh();
            } else if (event.getCode() == KeyCode.F6) {
                moveFiles();
            } else if (event.getCode() == KeyCode.F8) {
                deleteFiles();
            } else if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                showSearchDialog();
            } else if (event.getCode() == KeyCode.TAB) {
                switchActivePanel();
                event.consume();
            }
        });
    }

    private void loadState() {
        apiClient.getState()
            .thenAccept(state -> Platform.runLater(() -> {
                leftPanel.navigateTo(state.leftPanelPath());
                rightPanel.navigateTo(state.rightPanelPath());

                if (state.activePanelPosition() == PanelPosition.RIGHT) {
                    setActivePanel(rightPanel);
                } else {
                    setActivePanel(leftPanel);
                }
            }));
    }

    private void setActivePanel(FilePanel panel) {
        leftPanel.setActive(false);
        rightPanel.setActive(false);
        panel.setActive(true);
        activePanel = panel;

        // Save active panel state
        var position = panel == leftPanel ? PanelPosition.LEFT : PanelPosition.RIGHT;
        apiClient.updateActivePanel(position);
    }

    private void switchActivePanel() {
        if (activePanel == leftPanel) {
            setActivePanel(rightPanel);
        } else {
            setActivePanel(leftPanel);
        }
    }

    private FilePanel getInactivePanel() {
        return activePanel == leftPanel ? rightPanel : leftPanel;
    }

    private void copyFiles() {
        var selectedFiles = activePanel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            showAlert("No Selection", "Please select files to copy");
            return;
        }

        var targetPath = getInactivePanel().getCurrentPath();
        var fileCount = selectedFiles.size();

        for (var file : selectedFiles) {
            var targetFile = Paths.get(targetPath, file.name()).toString();
            apiClient.copy(file.path(), targetFile)
                .thenAccept(_ -> Platform.runLater(() -> {
                    if (fileCount == 1) {
                        statusBar.setMessage("Successfully copied '" + file.name() + "' to " + targetPath);
                    } else {
                        statusBar.setMessage("Copied " + fileCount + " file(s) to " + targetPath);
                    }
                    getInactivePanel().refresh();
                }));
        }
    }

    private void moveFiles() {
        var selectedFiles = activePanel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            showAlert("No Selection", "Please select files to move");
            return;
        }

        var targetPath = getInactivePanel().getCurrentPath();
        var fileCount = selectedFiles.size();

        for (var file : selectedFiles) {
            var targetFile = Paths.get(targetPath, file.name()).toString();
            apiClient.move(file.path(), targetFile)
                .thenAccept(_ -> Platform.runLater(() -> {
                    if (fileCount == 1) {
                        statusBar.setMessage("Successfully moved '" + file.name() + "' from " + activePanel.getCurrentPath() + " to " + targetPath);
                    } else {
                        statusBar.setMessage("Moved " + fileCount + " file(s) from " + activePanel.getCurrentPath() + " to " + targetPath);
                    }
                    activePanel.refresh();
                    getInactivePanel().refresh();
                }));
        }
    }

    private void deleteFiles() {
        var selectedFiles = activePanel.getSelectedFiles();
        if (selectedFiles.isEmpty()) {
            showAlert("No Selection", "Please select files to delete");
            return;
        }

        var confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete " + selectedFiles.size() + " file(s)?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                var fileCount = selectedFiles.size();
                for (var file : selectedFiles) {
                    apiClient.delete(file.path())
                        .thenAccept(_ -> Platform.runLater(() -> {
                            if (fileCount == 1) {
                                statusBar.setMessage("Successfully deleted '" + file.name() + "' from " + activePanel.getCurrentPath());
                            } else {
                                statusBar.setMessage("Deleted " + fileCount + " file(s) from " + activePanel.getCurrentPath());
                            }
                            activePanel.refresh();
                        }));
                }
            }
        });
    }

    private void showSearchDialog() {
        var dialog = new SearchDialog(activePanel.getCurrentPath(), apiClient);
        dialog.showAndWait().ifPresent(results -> {
            if (results.isEmpty()) {
                statusBar.setMessage("Search completed - no files found in " + activePanel.getCurrentPath());
            } else {
                statusBar.setMessage("Search completed - found " + results.size() + " file(s) in " + activePanel.getCurrentPath());
            }
        });
    }

    private void showAlert(String title, String message) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
