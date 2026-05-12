package com.example.mygame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    private ImageView bulletImage;

    @FXML
    private ImageView enemyImage;

    @FXML
    private ImageView escapeBackgroundImage;

    @FXML
    private ImageView cube;

    @FXML
    private ImageView platformImage1, platformImage2, platformImage3, platformImage4, platformImage5, platformImage6, platformImage7, platformImage8;

    @FXML
    private Label loseLabel;
    @FXML
    private Label scoreLabel;

    private double enemyVelocityX = 2;
    private boolean enemyActive = false;
    private int scoreToSpawnEnemy = 50;
    private double enemyMinX = 0;
    private double enemyMaxX = 0;

    private int score = 0;
    private double highestY = 0;
    private double totalScrolled = 0;

    // ==========================================
    // СИСТЕМА ПУЛЬ
    // ==========================================
    private List<Bullet> bullets = new ArrayList<>();
    private double bulletSpeed = 8;

    // Кулдаун между выстрелами
    private long lastShootTime = 0; // Время последнего выстрела в наносекундах
    private long shootCooldown = 500_000_000; // Кулдаун 0.5 секунды (500 миллисекунд в наносекундах)
    // Для 1 секунды используйте: 1_000_000_000

    private class Bullet {
        ImageView imageView;
        double velocityY;

        Bullet(ImageView imageView, double velocityY) {
            this.imageView = imageView;
            this.velocityY = velocityY;
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    // Физические переменные
    private double velocityY = 0;
    private double gravity = 0.3;
    private double maxVelocityY = 15;

    private double velocityX = 0;
    private double acceleration = 0.6;
    private double maxSpeed = 5;
    private double friction = 0.8;

    private double jumpForce = -12;
    private boolean isGrounded = false;

    private List<ImageView> obstacles = new ArrayList<>();
    private AnimationTimer gameLoop;
    private Random random = new Random();

    private double scrollThreshold = 300;

    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public void setScene(Scene scene) {
        this.currentScene = scene;
        currentScene.setOnKeyPressed(this::handleKeyPress);
        currentScene.setOnKeyReleased(this::handleKeyRelease);
        currentScene.setOnMouseClicked(this::handleMouseClick);
    }

    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    // ==========================================
    // ОБРАБОТКА МЫШИ
    // ==========================================
    private void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && escape) {
            shootBullet();
        }
    }

    // ==========================================
    // ВЫСТРЕЛ ПУЛИ С КУЛДАУНОМ
    // ==========================================
    private void shootBullet() {
        long currentTime = System.nanoTime();

        // Проверяем, прошло ли достаточно времени с последнего выстрела
        if (currentTime - lastShootTime < shootCooldown) {
            return; // Кулдаун ещё не прошёл, выход из метода
        }

        // Обновляем время последнего выстрела
        lastShootTime = currentTime;

        // Создаём новую пулю (клонируем изображение)
        ImageView newBullet = new ImageView(bulletImage.getImage());
        newBullet.setFitWidth(bulletImage.getFitWidth());
        newBullet.setFitHeight(bulletImage.getFitHeight());
        newBullet.setPreserveRatio(bulletImage.isPreserveRatio());

        // Позиционируем пулю в центре куба
        double bulletX = cube.getLayoutX() + cube.getFitWidth() / 2 - newBullet.getFitWidth() / 2;
        double bulletY = cube.getLayoutY();

        newBullet.setLayoutX(bulletX);
        newBullet.setLayoutY(bulletY);
        newBullet.setVisible(true);

        // Добавляем на игровую панель
        gamePane.getChildren().add(newBullet);

        // Сохраняем в список активных пуль
        bullets.add(new Bullet(newBullet, -bulletSpeed));
    }

    // ==========================================
    // ОБНОВЛЕНИЕ ПУЛЬ
    // ==========================================
    private void updateBullets() {
        Iterator<Bullet> iterator = bullets.iterator();

        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();

            // Двигаем пулю вверх
            bullet.imageView.setLayoutY(bullet.imageView.getLayoutY() + bullet.velocityY);

            // Проверяем столкновение с врагом
            if (enemyActive && checkBulletEnemyCollision(bullet.imageView)) {
                // Враг уничтожен
                enemyActive = false;
                enemyImage.setVisible(false);

                // Удаляем пулю
                gamePane.getChildren().remove(bullet.imageView);
                iterator.remove();

                // Даём бонусные очки
                score += 100;
                updateScoreLabel();

                continue;
            }

            // Если пуля улетела за верхний край экрана — удаляем
            if (bullet.imageView.getLayoutY() < -bullet.imageView.getFitHeight()) {
                gamePane.getChildren().remove(bullet.imageView);
                iterator.remove();
            }
        }
    }

    // ==========================================
    // СТОЛКНОВЕНИЕ ПУЛИ С ВРАГОМ
    // ==========================================
    private boolean checkBulletEnemyCollision(ImageView bullet) {
        double bulletLeft = bullet.getLayoutX();
        double bulletRight = bullet.getLayoutX() + bullet.getFitWidth();
        double bulletTop = bullet.getLayoutY();
        double bulletBottom = bullet.getLayoutY() + bullet.getFitHeight();

        double enemyLeft = enemyImage.getLayoutX();
        double enemyRight = enemyImage.getLayoutX() + enemyImage.getFitWidth();
        double enemyTop = enemyImage.getLayoutY();
        double enemyBottom = enemyImage.getLayoutY() + enemyImage.getFitHeight();

        boolean collisionX = bulletRight > enemyLeft && bulletLeft < enemyRight;
        boolean collisionY = bulletBottom > enemyTop && bulletTop < enemyBottom;

        return collisionX && collisionY;
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
        if (leftPressed && !rightPressed) {
            velocityX -= acceleration;
            if (velocityX < -maxSpeed) velocityX = -maxSpeed;
        } else if (rightPressed && !leftPressed) {
            velocityX += acceleration;
            if (velocityX > maxSpeed) velocityX = maxSpeed;
        } else {
            velocityX *= friction;
            if (Math.abs(velocityX) < 0.1) velocityX = 0;
        }

        double newX = cube.getLayoutX() + velocityX;

        if (newX < -cube.getFitWidth()) {
            newX = gamePane.getWidth();
        } else if (newX > gamePane.getWidth()) {
            newX = -cube.getFitWidth();
        }

        cube.setLayoutX(newX);
    }

    private void falling() {
        double oldY = cube.getLayoutY();
        velocityY += gravity;

        if (velocityY > maxVelocityY) velocityY = maxVelocityY;
        if (velocityY < -maxVelocityY) velocityY = -maxVelocityY;

        double newY = oldY + velocityY;

        isGrounded = false;

        if (velocityY > 0) {
            for (ImageView platform : obstacles) {
                double platformTop = platform.getLayoutY();
                double platformLeft = platform.getLayoutX();
                double platformRight = platform.getLayoutX() + platform.getFitWidth();
                double cubeLeft = cube.getLayoutX();
                double cubeRight = cube.getLayoutX() + cube.getFitWidth();

                if (cubeRight > platformLeft && cubeLeft < platformRight) {
                    double oldCubeBottom = oldY + cube.getFitHeight();
                    double newCubeBottom = newY + cube.getFitHeight();

                    if (oldCubeBottom <= platformTop + 10 && newCubeBottom >= platformTop) {
                        newY = platformTop - 59;
                        velocityY = 0;
                        isGrounded = true;
                        break;
                    }
                }
            }
        }

        if (newY < scrollThreshold) {
            double deltaY = scrollThreshold - newY;
            newY = scrollThreshold;

            for (ImageView platform : obstacles) {
                platform.setLayoutY(platform.getLayoutY() + deltaY);
            }

            if (enemyActive) {
                enemyImage.setLayoutY(enemyImage.getLayoutY() + deltaY);
            }

            for (Bullet bullet : bullets) {
                bullet.imageView.setLayoutY(bullet.imageView.getLayoutY() + deltaY);
            }

            totalScrolled += deltaY;

            int newScore = (int) (totalScrolled / 5);
            if (newScore > score) {
                score = newScore;
                updateScoreLabel();

                if (score >= scoreToSpawnEnemy && !enemyActive) {
                    spawnEnemy();
                }
            }
        }

        cube.setLayoutY(newY);

        recyclePlatforms();

        if (cube.getLayoutY() > gamePane.getHeight()) {
            gameOver();
        }
    }

    private void recyclePlatforms() {
        double highestPlatformY = Double.MAX_VALUE;
        for (ImageView p : obstacles) {
            if (p.getLayoutY() < highestPlatformY) {
                highestPlatformY = p.getLayoutY();
            }
        }

        for (ImageView platform : obstacles) {
            if (platform.getLayoutY() > gamePane.getHeight()) {
                double verticalGap = 70 + random.nextDouble() * 50;
                platform.setLayoutY(highestPlatformY - verticalGap);

                double paneWidth = gamePane.getWidth() > 0 ? gamePane.getWidth() : 600;
                double maxPositionX = paneWidth - platform.getFitWidth();
                platform.setLayoutX(random.nextDouble() * maxPositionX);

                highestPlatformY = platform.getLayoutY();
            }
        }
    }

    private void spawnEnemy() {
        if (enemyActive) return;

        // Враг появляется ВЫШЕ экрана (над верхней границей)
        double spawnY = -enemyImage.getFitHeight() - random.nextDouble() * 100;

        // Случайная позиция по X
        double spawnX = random.nextDouble() * (gamePane.getWidth() - enemyImage.getFitWidth());

        enemyImage.setLayoutX(spawnX);
        enemyImage.setLayoutY(spawnY);
        enemyImage.setVisible(true);

        // Случайное направление при появлении
        enemyVelocityX = random.nextBoolean() ? 2 : -2;

        // Устанавливаем правильную ориентацию изображения в зависимости от направления
        if (enemyVelocityX > 0) {
            enemyImage.setScaleX(1);  // Летим вправо - нормальное изображение
        } else {
            enemyImage.setScaleX(-1); // Летим влево - отзеркаленное изображение
        }

        enemyActive = true;
    }

    private void updateEnemy() {
        if (!enemyActive) return;

        // Двигаем врага влево-вправо по всему экрану
        double newEnemyX = enemyImage.getLayoutX() + enemyVelocityX;

        // Разворачиваем у краёв экрана
        if (newEnemyX >= gamePane.getWidth() - enemyImage.getFitWidth()) {
            newEnemyX = gamePane.getWidth() - enemyImage.getFitWidth();
            enemyVelocityX = -Math.abs(enemyVelocityX); // Летим влево

            // Инверсируем изображение (отзеркаливаем влево)
            enemyImage.setScaleX(-1);
        } else if (newEnemyX <= 0) {
            newEnemyX = 0;
            enemyVelocityX = Math.abs(enemyVelocityX); // Летим вправо

            // Возвращаем нормальное изображение (вправо)
            enemyImage.setScaleX(1);
        }

        enemyImage.setLayoutX(newEnemyX);

        // Проверяем столкновение с игроком
        checkEnemyCollision();

        // Если враг ушёл за экран вниз — деактивируем
        if (enemyImage.getLayoutY() > gamePane.getHeight()) {
            enemyActive = false;
            enemyImage.setVisible(false);
        }
    }

    private void checkEnemyCollision() {
        if (!enemyActive) return;

        double cubeLeft   = cube.getLayoutX();
        double cubeRight  = cube.getLayoutX() + cube.getFitWidth();
        double cubeTop    = cube.getLayoutY();
        double cubeBottom = cube.getLayoutY() + cube.getFitHeight();

        double enemyLeft   = enemyImage.getLayoutX();
        double enemyRight  = enemyImage.getLayoutX() + enemyImage.getFitWidth();
        double enemyTop    = enemyImage.getLayoutY();
        double enemyBottom = enemyImage.getLayoutY() + enemyImage.getFitHeight();

        boolean collisionX = cubeRight > enemyLeft && cubeLeft < enemyRight;
        boolean collisionY = cubeBottom > enemyTop && cubeTop < enemyBottom;

        if (collisionX && collisionY) {
            gameOver();
        }
    }

    private void gameOver() {
        if (gameLoop != null) gameLoop.stop();

        RecordManager.saveRecord(score);
        int record = RecordManager.getRecord();

        escapeBackgroundImage.toFront();
        escapeBackgroundImage.setVisible(true);
        loseLabel.toFront();
        loseLabel.setVisible(true);

        loseLabel.setText("Вы проиграли!\nСчёт: " + score + "\nРекорд: " + record);

        backBtn.toFront();
        backBtn.setVisible(true);
        backBtn.setDisable(false);
        escape = false;
    }

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
            loseLabel.setVisible(false);
            escape = true;
            startGameLoop();
        }
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateMovement();
                falling();
                updateEnemy();
                updateBullets();
            }
        };

        gameLoop.start();
    }

    @FXML
    private void backToHello() {
        if (gameLoop != null) gameLoop.stop();
        sceneManager.showHelloScene();
    }

    @FXML
    void initialize() {
        obstacles.add(platformImage1);
        obstacles.add(platformImage2);
        obstacles.add(platformImage3);
        obstacles.add(platformImage4);
        obstacles.add(platformImage5);
        obstacles.add(platformImage6);
        obstacles.add(platformImage7);
        obstacles.add(platformImage8);

        cube.setLayoutX(250);
        cube.setLayoutY(400);

        enemyImage.setVisible(false);
        enemyActive = false;

        bulletImage.setVisible(false);

        Platform.runLater(() -> {
            double startY = 500;
            for (ImageView platform : obstacles) {
                platform.setLayoutY(startY);
                double maxPositionX = gamePane.getWidth() - platform.getFitWidth();
                platform.setLayoutX(random.nextDouble() * maxPositionX);
                startY -= (80 + random.nextDouble() * 40);
            }
            obstacles.get(0).setLayoutX(250);
            obstacles.get(0).setLayoutY(480);
        });

        score = 0;
        totalScrolled = 0;
        scoreLabel.setText("Score: 0");

        startGameLoop();
    }
}