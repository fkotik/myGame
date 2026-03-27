package com.example.mygame;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {

    private Stage stage;
    private HelloController helloController;
    private GameController gameController;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void showHelloScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            helloController = loader.getController();
            helloController.setSceneManager(this);

            stage.setScene(new Scene(root, 540, 740));
            stage.setTitle("Hello Application");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            Parent root = loader.load();

            gameController = loader.getController();
            gameController.setSceneManager(this);

            Scene scene = new Scene(root, 540, 740);

            gameController.setScene(scene);

            stage.setScene(scene);
            stage.setTitle("Game Application");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}