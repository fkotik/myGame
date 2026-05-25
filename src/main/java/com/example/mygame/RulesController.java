package com.example.mygame;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class RulesController {

    private SceneManager sceneManager;

    @FXML
    private Button backBtn;

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    private void backToHello() {
        sceneManager.showHelloScene();
    }

    @FXML
    void initialize() {
        backBtn.setOnAction(event -> backToHello());
    }
}