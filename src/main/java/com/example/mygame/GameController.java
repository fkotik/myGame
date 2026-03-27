package com.example.mygame;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class GameController {

    private SceneManager sceneManager;
    private Scene currentScene;
    private boolean escape = true;

    @FXML
    private Pane gamePane;

    @FXML
    private Button backBtn;

    @FXML
    private ImageView escapeBackgroundImage;

    @FXML
    private ImageView cube;

    @FXML
    private ImageView platformImage1, platformImage2, platformImage3, platformImage4, platformImage5;

    @FXML
    private Label loseLabel;

    // Физические переменные
    private double velocityY = 0;
    private double gravity = 0.2;
    private double maxVelocityY = 10;

    private double velocityX = 0;
    private double acceleration = 0.5; // Ускорение
    private double maxSpeed = 4; // Максимальная скорость
    private double friction = 0.98; // Трение в воздухе
    private double groundFriction = 0.92; // Трение на земле

    private double jumpForce = -12; // Отрицательная сила для прыжка вверх
    private boolean isGrounded = false;

    private List<ImageView> obstacles = new ArrayList<>();
    private AnimationTimer gameLoop;

    // Клавиши управления
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public void setScene(Scene scene) {
        this.currentScene = scene;
        currentScene.setOnKeyPressed(this::handleKeyPress);
        currentScene.setOnKeyReleased(this::handleKeyRelease);
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode keyCode = event.getCode();

        switch (keyCode) {
            case W:
            case UP:
            case SPACE:
                jump();
                break;
            case A:
            case LEFT:
                leftPressed = true;
                break;
            case D:
            case RIGHT:
                rightPressed = true;
                break;
            case ESCAPE:
                toggleEscapeMenu();
                break;
            default:
                break;
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        KeyCode keyCode = event.getCode();

        switch (keyCode) {
            case A:
            case LEFT:
                leftPressed = false;
                break;
            case D:
            case RIGHT:
                rightPressed = false;
                break;
        }
    }

    private void jump() {
        if (isGrounded) {
            velocityY = jumpForce;
            isGrounded = false;
        }
    }

    private void updateMovement() {
        // Обновляем горизонтальную скорость
        if (leftPressed && !rightPressed) {
            velocityX -= acceleration;
            if (velocityX < -maxSpeed) velocityX = -maxSpeed;
        } else if (rightPressed && !leftPressed) {
            velocityX += acceleration;
            if (velocityX > maxSpeed) velocityX = maxSpeed;
        } else {
            // Применяем трение
            if (isGrounded) {
                velocityX *= groundFriction;
            } else {
                velocityX *= friction; // Меньшее трение в воздухе
            }

            if (Math.abs(velocityX) < 0.1) velocityX = 0;
        }

        // Применяем горизонтальное движение
        double newX = cube.getLayoutX() + velocityX;

        // Проверка границ по X
        if (newX < -25) {
            newX = gamePane.getWidth() - cube.getFitWidth();
        }
        if (newX > gamePane.getWidth() - cube.getFitWidth() + 25) {
            newX = 0;
        }

        cube.setLayoutX(newX);
    }

    private void falling() {
        // Сохраняем старую позицию
        double oldY = cube.getLayoutY();

        // Применяем гравитацию
        velocityY += gravity;

        // Ограничиваем максимальную скорость падения
        if (velocityY > maxVelocityY) velocityY = maxVelocityY;
        if (velocityY < -maxVelocityY) velocityY = -maxVelocityY;

        // Новая позиция после движения
        double newY = oldY + velocityY;

        // Сбрасываем флаг grounded
        isGrounded = false;

        // Проверяем коллизии со всеми платформами
        for (ImageView platform : obstacles) {
            // Получаем границы платформы
            double platformTop = platform.getLayoutY();
            double platformLeft = platform.getLayoutX();
            double platformRight = platform.getLayoutX() + platform.getFitWidth();

            // Получаем границы куба
            double cubeLeft = cube.getLayoutX();
            double cubeRight = cube.getLayoutX() + cube.getFitWidth();

            // Проверяем горизонтальное пересечение
            if (cubeRight > platformLeft && cubeLeft < platformRight) {
                // Проверяем движение вниз (падение)
                if (velocityY > 0) {
                    // Старая нижняя граница куба
                    double oldCubeBottom = oldY + cube.getFitHeight();
                    // Новая нижняя граница куба
                    double newCubeBottom = newY + cube.getFitHeight();

                    // Если куб падал и пересек верхнюю границу платформы
                    if (oldCubeBottom <= platformTop && newCubeBottom >= platformTop) {
                        // Приземляем на платформу
                        newY = platformTop - 58;
                        velocityY = 0;
                        isGrounded = true;
                        break; // Приземлились на первую же платформу
                    }
                }
            }
        }

        // Применяем новую позицию
        cube.setLayoutY(newY);

        // Дополнительная проверка: если куб все еще пересекается с платформой и не движется вверх, выталкиваем его
        if (!isGrounded && velocityY >= 0) {
            for (ImageView platform : obstacles) {
                if (cube.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    double platformTop = platform.getLayoutY();
                    cube.setLayoutY(platformTop - 58);
                    velocityY = 0;
                    isGrounded = true;
                    break;
                }
            }
        }

        // Проверка пола (нижняя граница)
        if (cube.getLayoutY() + cube.getFitHeight() >= gamePane.getHeight()) {
            escapeBackgroundImage.toFront();
            escapeBackgroundImage.setVisible(true);
            loseLabel.toFront();
            loseLabel.setVisible(true);
            backBtn.toFront();
            backBtn.setVisible(true);
            backBtn.setDisable(false);
            escape = false;
        }

    }

    //пауза
    private void toggleEscapeMenu() {
        if (escape) {
            escapeBackgroundImage.toFront();
            escapeBackgroundImage.setVisible(true);
            backBtn.toFront();
            backBtn.setVisible(true);
            backBtn.setDisable(false);
            escape = false;
            gameLoop.stop();
        } else {
            escapeBackgroundImage.setVisible(false);
            backBtn.setVisible(false);
            backBtn.setDisable(true);
            escape = true;
            startGameLoop();
        }
    }

    //запуск движка
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateMovement();  // Обновляем горизонтальное движение
                falling();   // Обновляем физику (падение и прыжки)
            }
        };
        gameLoop.start();
    }

    //выход в главное меню
    @FXML
    private void backToHello() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        sceneManager.showHelloScene();
    }

    //инициализация сцены
    @FXML
    void initialize() {
        // Добавляем платформы в список препятствий
        obstacles.add(platformImage1);
        obstacles.add(platformImage2);
        obstacles.add(platformImage3);
        obstacles.add(platformImage4);
        obstacles.add(platformImage5);

        // Устанавливаем начальную позицию куба
        cube.setLayoutX(250);
        cube.setLayoutY(300);

        // Запускаем игровой цикл
        startGameLoop();

    }
}