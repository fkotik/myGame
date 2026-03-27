package com.example.mygame;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

public class CollisionManager {

    private List<ImageView> obstacles = new ArrayList<>();
    private Pane gamePane;

    public CollisionManager(Pane gamePane) {
        this.gamePane = gamePane;
    }

    public void addObstacle(ImageView obstacle) {
        obstacles.add(obstacle);
        gamePane.getChildren().add(obstacle);
    }

    // Проверка коллизии со всеми препятствиями
    public boolean checkCollisionWithAll(ImageView object) {
        for (ImageView obstacle : obstacles) {
            if (object != obstacle &&
                    object.getBoundsInParent().intersects(obstacle.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }

    // Движение с проверкой коллизий
    public boolean tryMove(ImageView object, double dx, double dy) {
        double oldX = object.getLayoutX();
        double oldY = object.getLayoutY();

        // Пробуем переместить
        object.setLayoutX(oldX + dx);
        object.setLayoutY(oldY + dy);

        // Проверяем коллизии
        if (checkCollisionWithAll(object)) {
            // Откатываем движение
            object.setLayoutX(oldX);
            object.setLayoutY(oldY);
            return false;
        }

        // Проверяем границы
        checkBoundaries(object);
        return true;
    }

    private void checkBoundaries(ImageView object) {
        if (object.getLayoutX() < 0) object.setLayoutX(0);
        if (object.getLayoutX() > gamePane.getWidth() - object.getFitWidth())
            object.setLayoutX(gamePane.getWidth() - object.getFitWidth());
        if (object.getLayoutY() < 0) object.setLayoutY(0);
        if (object.getLayoutY() > gamePane.getHeight() - object.getFitHeight())
            object.setLayoutY(gamePane.getHeight() - object.getFitHeight());
    }
}

