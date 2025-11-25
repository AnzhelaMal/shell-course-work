package edu.semitotal.commander.client;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBar extends HBox {
    private final Label messageLabel;

    public StatusBar() {
        setPadding(new Insets(5, 10, 5, 10));
        setSpacing(10);
        getStyleClass().add("status-bar");

        messageLabel = new Label("Ready");
        getChildren().add(messageLabel);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
