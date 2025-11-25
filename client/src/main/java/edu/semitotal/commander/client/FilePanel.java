package edu.semitotal.commander.client;

import edu.semitotal.commander.api.DiskInfo;
import edu.semitotal.commander.api.FileInfo;
import edu.semitotal.commander.api.PanelPosition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FilePanel extends VBox {

    private final ApiClient apiClient;
    private final PanelPosition panelPosition;
    private final Label titleLabel;
    private final ComboBox<String> driveComboBox;
    private final TextField pathField;
    private final TableView<FileInfo> fileTable;
    private final ObservableList<FileInfo> files;
    private final MenuItem newFileItemEmpty;
    private final MenuItem newFolderItemEmpty;
    @Getter
    private String currentPath;
    private Comparator<FileInfo> currentSort;
    @Setter
    private Runnable onActivationRequested;

    public FilePanel(ApiClient apiClient, String title, PanelPosition panelPosition) {
        this.apiClient = apiClient;
        this.panelPosition = panelPosition;
        this.files = FXCollections.observableArrayList();
        this.titleLabel = new Label(title);
        this.driveComboBox = new ComboBox<>();
        this.pathField = new TextField();
        this.newFileItemEmpty = new MenuItem("New File...");
        this.newFolderItemEmpty = new MenuItem("New Folder...");
        this.fileTable = createFileTable();

        setupLayout();
        loadDrives();
    }

    private void requestActivation() {
        if (onActivationRequested != null) {
            onActivationRequested.run();
        }
    }

    private void setupLayout() {
        setPadding(new Insets(5));
        setSpacing(5);
        getStyleClass().add("file-panel");

        titleLabel.getStyleClass().add("panel-title");

        var topBar = new HBox(10);
        topBar.getChildren().addAll(new Label("Drive:"), driveComboBox);

        driveComboBox.setOnAction(_ -> {
            var selectedDrive = driveComboBox.getValue();
            if (selectedDrive != null && (currentPath == null || !currentPath.startsWith(selectedDrive))) {
                navigateTo(selectedDrive);
            }
        });

        pathField.setOnAction(_ -> navigateTo(pathField.getText()));
        pathField.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                requestActivation();
            }
        });
        HBox.setHgrow(pathField, Priority.ALWAYS);

        var pathBar = new HBox(10);
        pathBar.getChildren().addAll(new Label("Path:"), pathField);

        getChildren().addAll(titleLabel, topBar, pathBar, fileTable);
        VBox.setVgrow(fileTable, Priority.ALWAYS);
    }

    @SuppressWarnings("unchecked")
    private TableView<FileInfo> createFileTable() {
        var table = new TableView<FileInfo>();
        table.setItems(files);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        var nameCol = new TableColumn<FileInfo, String>("Name");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        nameCol.setPrefWidth(300);
        nameCol.setComparator(String.CASE_INSENSITIVE_ORDER);

        var sizeCol = createSizeCol();
        var modifiedCol = createModifiedCol();

        table.getColumns().addAll(nameCol, sizeCol, modifiedCol);

        table.getSortOrder().addListener((ListChangeListener<? super TableColumn<FileInfo, ?>>) _ -> {
            if (!table.getSortOrder().isEmpty()) {
                var sortCol = table.getSortOrder().getFirst();
                var sortType = sortCol.getSortType();

                Comparator<FileInfo> comparator = null;
                if (sortCol == nameCol) {
                    comparator = Comparator.comparing(FileInfo::name, String.CASE_INSENSITIVE_ORDER);
                } else if (sortCol == sizeCol) {
                    comparator = Comparator.comparing(FileInfo::size);
                } else if (sortCol == modifiedCol) {
                    comparator = Comparator.comparing(FileInfo::lastModified);
                }

                if (comparator != null && sortType == TableColumn.SortType.DESCENDING) {
                    comparator = comparator.reversed();
                }

                currentSort = comparator;
            }
        });

        table.setRowFactory(_ -> {
            var row = new TableRow<FileInfo>() {
                @Override
                protected void updateItem(FileInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else if (!item.writable()) {
                        setStyle("-fx-text-fill: #888888;");
                    } else {
                        setStyle("");
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    var file = row.getItem();
                    if (file.isDirectory()) {
                        navigateTo(file.path());
                    }
                }
            });

            var contextMenu = new ContextMenu();

            var renameItem = new MenuItem("Rename");
            renameItem.setOnAction(_ -> {
                if (!row.isEmpty()) {
                    showRenameDialog(row.getItem());
                }
            });

            var deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(_ -> {
                if (!row.isEmpty()) {
                    deleteFile(row.getItem());
                }
            });

            var newFileItem = new MenuItem("New File...");
            newFileItem.setOnAction(_ -> showCreateFileDialog(false));

            var newFolderItem = new MenuItem("New Folder...");
            newFolderItem.setOnAction(_ -> showCreateFileDialog(true));

            contextMenu.getItems().addAll(renameItem, deleteItem,
                new SeparatorMenuItem(), newFileItem, newFolderItem);

            row.itemProperty().addListener((_, _, newItem) -> {
                if (newItem != null) {
                    var isWritable = newItem.writable();
                    renameItem.setDisable(!isWritable);
                    deleteItem.setDisable(!isWritable);
                }
            });

            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            return row;
        });

        newFileItemEmpty.setOnAction(_ -> showCreateFileDialog(false));
        newFolderItemEmpty.setOnAction(_ -> showCreateFileDialog(true));

        var emptyContextMenu = new ContextMenu();
        emptyContextMenu.getItems().addAll(newFileItemEmpty, newFolderItemEmpty);
        table.setContextMenu(emptyContextMenu);

        // Request activation when table is clicked or focused
        table.setOnMouseClicked(_ -> requestActivation());
        table.focusedProperty().addListener((_, _, focused) -> {
            if (focused) {
                requestActivation();
            }
        });

        return table;
    }

    private static TableColumn<FileInfo, Instant> createModifiedCol() {
        var modifiedCol = new TableColumn<FileInfo, Instant>("Modified");
        modifiedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().lastModified()));
        modifiedCol.setCellFactory(_ -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

            @Override
            protected void updateItem(Instant instant, boolean empty) {
                super.updateItem(instant, empty);
                if (empty || instant == null) {
                    setText(null);
                } else {
                    setText(formatter.format(instant));
                }
            }
        });
        modifiedCol.setPrefWidth(180);
        return modifiedCol;
    }

    private TableColumn<FileInfo, Long> createSizeCol() {
        var sizeCol = new TableColumn<FileInfo, Long>("Size");
        sizeCol.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().size()).asObject());
        sizeCol.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(Long size, boolean empty) {
                super.updateItem(size, empty);
                if (empty || size == null) {
                    setText(null);
                } else {
                    var item = getTableView().getItems().get(getIndex());
                    if (item.isDirectory()) {
                        setText("<DIR>");
                    } else {
                        setText(formatSize(size));
                    }
                }
            }
        });
        sizeCol.setPrefWidth(100);
        return sizeCol;
    }

    private void loadDrives() {
        apiClient.listDisks()
            .thenAccept(response -> Platform.runLater(() -> {
                var driveNames = response.disks().stream()
                    .map(DiskInfo::path)
                    .toList();
                driveComboBox.getItems().setAll(driveNames);
                if (!driveNames.isEmpty()) {
                    driveComboBox.setValue(driveNames.getFirst());
                }
            }));
    }

    public void navigateTo(String path) {
        this.currentPath = path;
        this.pathField.setText(path);

        apiClient.listDirectory(path)
            .thenAccept(response -> Platform.runLater(() -> {
                var fileList = new ArrayList<>(response.files());

                if (!isRootDirectory(path)) {
                    var parentPath = Paths.get(path).getParent();
                    if (parentPath != null) {
                        var parentDir = FileInfo.builder()
                            .name("..")
                            .path(parentPath.toString())
                            .isDirectory(true)
                            .size(0)
                            .lastModified(Instant.now())
                            .readable(true)
                            .writable(false)
                            .executable(false)
                            .build();
                        fileList.addFirst(parentDir);
                    }
                }

                var currentSortOrder = new ArrayList<>(fileTable.getSortOrder());

                files.setAll(fileList);

                if (!currentSortOrder.isEmpty()) {
                    fileTable.getSortOrder().setAll(currentSortOrder);
                    fileTable.sort();
                } else if (currentSort != null) {
                    var hasParent = !files.isEmpty() && "..".equals(files.getFirst().name());
                    var toSort = hasParent ? files.subList(1, files.size()) : files;
                    toSort.sort(currentSort);
                }

                saveState(path);
            }));
    }

    private boolean isRootDirectory(String path) {
        var p = Paths.get(path);
        return p.getParent() == null || p.toFile().getParent() == null;
    }

    private void saveState(String path) {
        if (panelPosition == PanelPosition.LEFT) {
            apiClient.updateLeftPanelPath(path);
        } else if (panelPosition == PanelPosition.RIGHT) {
            apiClient.updateRightPanelPath(path);
        }
    }

    public void refresh() {
        if (currentPath != null) {
            navigateTo(currentPath);
        }
    }

    public List<FileInfo> getSelectedFiles() {
        return fileTable.getSelectionModel().getSelectedItems();
    }

    public void setActive(boolean active) {
        if (active) {
            titleLabel.getStyleClass().add("active");
            getStyleClass().add("active-panel");
        } else {
            titleLabel.getStyleClass().remove("active");
            getStyleClass().remove("active-panel");
        }
    }

    private void showInfo(String title, String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showRenameDialog(FileInfo file) {
        var dialog = new TextInputDialog(file.name());
        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename " + (file.isDirectory() ? "folder" : "file"));
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty() && !newName.equals(file.name())) {
                apiClient.rename(file.path(), newName)
                    .thenAccept(_ -> Platform.runLater(() -> {
                        refresh();
                        showInfo("Rename Successful",
                            "Successfully renamed '" + file.name() + "' to '" + newName + "' in " + currentPath);
                    }));
            }
        });
    }

    private void deleteFile(FileInfo file) {
        var confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete " + file.name() + "?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                var itemType = file.isDirectory() ? "folder" : "file";
                apiClient.delete(file.path())
                    .thenAccept(_ -> Platform.runLater(() -> {
                        refresh();
                        showInfo("Delete Successful",
                            "Successfully deleted " + itemType + " '" + file.name() + "' from " + currentPath);
                    }));
            }
        });
    }

    private void showCreateFileDialog(boolean isDirectory) {
        var dialog = new TextInputDialog();
        var itemType = isDirectory ? "folder" : "file";
        dialog.setTitle(isDirectory ? "New Folder" : "New File");
        dialog.setHeaderText("Create new " + itemType);
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isEmpty()) {
                var newPath = Paths.get(currentPath, name).toString();
                apiClient.createFile(newPath, isDirectory)
                    .thenAccept(_ -> Platform.runLater(() -> {
                        refresh();
                        showInfo("Create Successful",
                            "Successfully created " + itemType + " '" + name + "' in " + currentPath);
                    }));
            }
        });
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        }
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        }
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}
