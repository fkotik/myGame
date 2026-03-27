package com.example.mygame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {

    private Stage primaryStage;
    private SceneManager sceneManager;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        this.sceneManager = new SceneManager(stage);

        // Показываем hello сцену
        sceneManager.showHelloScene();
        primaryStage.show();


    }
}
