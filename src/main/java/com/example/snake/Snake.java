package com.example.snake;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Snake extends GameApplication {

    private static final int TILE_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 20;

    private final List<Point2D> snake = new LinkedList<>();
    private Point2D direction = new Point2D(1, 0);
    private Point2D food;

    private boolean gameOver = false;
    private int score = 0;

    private double speed = 0.15; ////////////////////////////////

    private Point2D bonus;
    private boolean bonusVisible = false;


    private void spawnbonus() {
        Random random = new Random();

        bonus = new Point2D(
                random.nextInt(GRID_WIDTH),
                random.nextInt(GRID_HEIGHT)
        );

        bonusVisible = true;

        int time = 5 + random.nextInt(1);

        FXGL.getGameTimer().runOnceAfter(() -> {
            bonusVisible = false;
        }, Duration.seconds(time));
    }

    private void checkbonus() {
        if (bonusVisible && snake.get(0).equals(bonus)) {
            snake.add(snake.get(snake.size() - 1));
            score += 3;

            bonusVisible = false;
        }
    }


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(GRID_WIDTH * TILE_SIZE);
        settings.setHeight(GRID_HEIGHT * TILE_SIZE);
        settings.setTitle("Snake FXGL");
        settings.setVersion("2.0");
    }

    @Override
    protected void initGame() {
        FXGL.getGameScene().setBackgroundColor(Color.BLACK);

        snake.clear();
        snake.add(new Point2D(10, 10));
        snake.add(new Point2D(9, 10));
        snake.add(new Point2D(8, 10));

        direction = new Point2D(1, 0);
        score = 0;
        gameOver = false;

        spawnFood();
        spawnbonus();

        startGameLoop();
    }
//////////////////////
    private void startGameLoop() {
        FXGL.getGameTimer().clear();
        FXGL.getGameTimer().runAtInterval(this::updateGame, Duration.seconds(speed));
    }
/// ////////////////////////
    @Override
    protected void initInput() {

        FXGL.onKeyDown(KeyCode.UP, () -> {
            if (!direction.equals(new Point2D(0, 1)))
                direction = new Point2D(0, -1);
        });

        FXGL.onKeyDown(KeyCode.DOWN, () -> {
            if (!direction.equals(new Point2D(0, -1)))
                direction = new Point2D(0, 1);
        });

        FXGL.onKeyDown(KeyCode.LEFT, () -> {
            if (!direction.equals(new Point2D(1, 0)))
                direction = new Point2D(-1, 0);
        });

        FXGL.onKeyDown(KeyCode.RIGHT, () -> {
            if (!direction.equals(new Point2D(-1, 0)))
                direction = new Point2D(1, 0);
        });

         FXGL.onKeyDown(KeyCode.Q, () -> {
            var stage = FXGL.getPrimaryStage();

            if (stage.getWidth() < 1000) {
                stage.setWidth(1200);
                stage.setHeight(800);
            } else {
                stage.setWidth(800);
                stage.setHeight(600);
            }
        });

        FXGL.onKeyDown(KeyCode.ENTER, () -> {
            if (gameOver) {
                FXGL.getGameController().startNewGame();
            }
        });
    }

    private void updateGame() {
        if (!gameOver) {
            moveSnake();
            checkFood();
            checkbonus();
            checkCollision();
        }
        draw();
    }

    private void moveSnake() {
        Point2D newHead = snake.get(0).add(direction);
        snake.add(0, newHead);
        snake.remove(snake.size() - 1);
    }
/// /////////////////////////////////////
    private void checkFood() {
        if (snake.get(0).equals(food)) {
            snake.add(snake.get(snake.size() - 1));
            score++;

            if (score % 2 == 0) {
                spawnbonus();
            }

            spawnFood();
        }
    }
/// /////////////////////////////////////////////////////////////
    private void checkCollision() {
        Point2D head = snake.get(0);

        if (head.getX() < 0 || head.getX() >= GRID_WIDTH || head.getY() < 0 || head.getY() >= GRID_HEIGHT) {
            gameOver = true;
        }

        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                gameOver = true;
            }
        }
    }

    private void spawnFood() {
        Random random = new Random();
        food = new Point2D(
                random.nextInt(GRID_WIDTH),
                random.nextInt(GRID_HEIGHT)
        );
    }

    private void draw() {
        FXGL.getGameScene().clearUINodes();

        for (int i = 0; i < snake.size(); i++) {
            Point2D p = snake.get(i);
            Rectangle r = new Rectangle(
                    p.getX() * TILE_SIZE,
                    p.getY() * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
            );
            r.setFill(i == 0 ? Color.LIMEGREEN : Color.DODGERBLUE);
            FXGL.getGameScene().addUINode(r);
        }

        Circle f = new Circle(
                food.getX() * TILE_SIZE + TILE_SIZE / 2.0,
                food.getY() * TILE_SIZE + TILE_SIZE / 2.0,
                TILE_SIZE / 2.0
        );
        f.setFill(Color.RED);
        FXGL.getGameScene().addUINode(f);

        if (bonusVisible) {
            Circle b = new Circle(
                    bonus.getX() * TILE_SIZE + TILE_SIZE / 2.0,
                    bonus.getY() * TILE_SIZE + TILE_SIZE / 2.0,
                    TILE_SIZE / 2.0
            );
            b.setFill(Color.GOLD);
            FXGL.getGameScene().addUINode(b);
        }

        Text scoreText = new Text("Score: " + score);
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(Font.font(18));
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(20);
        FXGL.getGameScene().addUINode(scoreText);

        if (gameOver) {
            Rectangle overlay = new Rectangle(
                    GRID_WIDTH * TILE_SIZE,
                    GRID_HEIGHT * TILE_SIZE
            );
            overlay.setFill(Color.color(0, 0, 0, 0.75));

            Text over = new Text("GAME OVER");
            over.setFill(Color.RED);
            over.setFont(Font.font(48));
            over.setTranslateX(170);
            over.setTranslateY(240);

            Text restart = new Text("Press ENTER to Restart");
            restart.setFill(Color.WHITE);
            restart.setFont(Font.font(20));
            restart.setTranslateX(185);
            restart.setTranslateY(300);

            FXGL.getGameScene().addUINode(overlay);
            FXGL.getGameScene().addUINode(over);
            FXGL.getGameScene().addUINode(restart);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
