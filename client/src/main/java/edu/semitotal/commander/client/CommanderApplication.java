package edu.semitotal.commander.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class CommanderApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Total Commander");

        var mainView = new MainView();
        var scene = new Scene(mainView, 1400, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
