import entities.*;
import graphics.Sprite;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tutorial extends Pane {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final AnimationTimer timer;
    private final VBox iconBox;
    private final VBox menuBoxContent;
    private final VBox descriptionBox;
    private final StackPane menuBox;

    private final List<Entity> stillObjects;
    private Bomber bomber;
    private Enemy enemy;
    private Bomb bomb;

    private final List<Pair<Pair<String, Image>, Runnable>> items;

    private int stepDone;

    private int active;

    private final int delayFrames;
    private int currentDelayFrames;

    public Tutorial() {
        Sprite.setScaledSize(3);

        active = -1;

        iconBox = new VBox(50);
        descriptionBox = new VBox(30);
        descriptionBox.setStyle("-fx-border-insets: 0 30 0 30;"
                + "-fx-border-color: rgba(172,172,172,0.75);"
                + "-fx-border-width: 1;"
                + "-fx-border-style: solid none none none;");
        menuBox = new StackPane();
        menuBoxContent = new VBox(40);
        menuBoxContent.setTranslateY(30);

        canvas = new Canvas(Sprite.SCALED_SIZE * BombermanGame.WIDTH, Sprite.SCALED_SIZE * BombermanGame.HEIGHT);
        canvas.setOpacity(0);
        gc = canvas.getGraphicsContext2D();

        stillObjects = new ArrayList<>();
        for (int i = 0; i < 24; i++) stillObjects.add(null);
        bomber = null;
        enemy = null;
        bomb = null;

        timer = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                collision();
                render();
                update();
                showAllTutorials();
            }
        };

        items = Arrays.asList(
                new Pair<Pair<String, Image>, Runnable>(new Pair<>("Bomb", Sprite.tutorial_bomb.getFxImage()), () -> switchTutorial(0)),
                new Pair<Pair<String, Image>, Runnable>(new Pair<>("Power-Up", Sprite.powerup_tutorial.getFxImage()), () -> switchTutorial(1)),
                new Pair<Pair<String, Image>, Runnable>(new Pair<>("Enemy", Sprite.doll_right1.getFxImage()), () -> switchTutorial(2)),
                new Pair<Pair<String, Image>, Runnable>(new Pair<>("Portal", Sprite.portal.getFxImage()), () -> switchTutorial(3))
        );

        getChildren().add(canvas);
        addBackButton();
        addItem();
        setDescription();
        menuBoxContent.getChildren().add(descriptionBox);
        addMenuBox();

        switchTutorial(0);
        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), canvas);
        ft.setToValue(1);
        ft.play();

        delayFrames = 60;
        currentDelayFrames = 0;
    }

    private void switchTutorial(int active) {
        if (this.active == active) return;

        timer.stop();
        this.active = active;
        AtomicBoolean activeToSet = new AtomicBoolean(false);
        iconBox.getChildren().forEach(row -> {
            activeToSet.set(iconBox.getChildren().indexOf(row) == active / 2);
            ((HBox) row).getChildren().forEach(item -> {
                ((TutorialItem) item).setActive(activeToSet.get() && ((HBox) row).getChildren().indexOf(item) == active % 2);
                ((TutorialItem) item).updateBorder();
            });
        });
        resetTutorial(active);
        setDescription();
        timer.start();
    }

    private void resetTutorial(int tutorial_number) {
        stepDone = 0;
        currentDelayFrames = 0;
        createTutorialMap("src/tutorials/tutorial" + (tutorial_number + 1) + ".txt");
    }

    private void showAllTutorials() {
        if (stepDone < 1) {
            bomber.setDirection("down");
            if (currentDelayFrames != delayFrames) currentDelayFrames++;
            else {
                if (active == 2) {
                    enemy.setDirection("right");
                    enemy.setVelocityX(enemy.getSpeed());
                    enemy.autoMove();
                }
                bomber.setVelocityY(bomber.getSpeed());
                bomber.setMoving(true);
                if (bomber.getY() == 238 && bomber.getX() == 432) {
                    currentDelayFrames = 0;
                    stepDone = 1;
                }
            }
        } else if (stepDone < 2) {
            bomber.setDirection("right");
            bomber.setVelocityX(bomber.getSpeed());
            bomber.setMoving(true);
            if (active == 2 && enemy.getX() == 624) {
                enemy.setVelocityX(0);
            }
            if (bomber.getY() == 240 & bomber.getX() == 580) stepDone = 2;
        } else if (stepDone < 3) {
            if (bomber.hasAvailableBombs())
                bomb = bomber.placeBomb(new ArrayList<>());
            bomber.setDirection("left");
            bomber.setVelocityX(-bomber.getSpeed());
            bomber.setMoving(true);
            if (bomber.getY() == 240 && bomber.getX() == 482) {
                bomber.setMoving(false);
                bomber.setVelocityX(0);
                bomber.setVelocityY(0);
                stepDone = 3;
            }
        } else if (stepDone < 4) {
            if (bomb != null && bomb.isExploded()) {
                bomb = null;
                if (active != 2)
                    stepDone = 4;
            }
            if (active == 2) {
                if (enemy != null && enemy.isDead()) {
                    enemy = null;
                    stepDone = 4;
                }
            }
        } else if (stepDone < 5 && active != 0 && active != 2) {
            bomber.setDirection("right");
            bomber.setVelocityX(bomber.getSpeed());
            bomber.setMoving(true);
            if (bomber.getY() == 240 & bomber.getX() >= 628) {
                bomber.setMoving(false);
                bomber.setVelocityX(0);
                bomber.setVelocityY(0);
                stepDone = 5;
            }
        } else {
            if (currentDelayFrames != delayFrames) currentDelayFrames++;
            else resetTutorial(active);
        }
    }

    private void createTutorialMap(String path) {
        clearMap();
        try {
            File map = new File(path);
            Scanner fileReader = new Scanner(map);

            int row = fileReader.nextInt();
            int col = fileReader.nextInt();
            fileReader.nextLine(); // skip to next line after reading ints

            for (int i = 0; i < row; i++) {
                String line = fileReader.nextLine();
                for (int j = 0; j < col; j++) {
                    Entity stillObject = switch (line.charAt(j)) {
                        case '#' -> new Wall(j, i, Sprite.wall.getFxImage());
                        case '*' -> new Brick(j, i, Sprite.brick.getFxImage(), "none");
                        case 'x' -> new Brick(j, i, Sprite.brick.getFxImage(), "portal");
                        case 'b' -> new Brick(j, i, Sprite.brick.getFxImage(), "bomb");
                        case 'f' -> new Brick(j, i, Sprite.brick.getFxImage(), "flame");
                        case 's' -> new Brick(j, i, Sprite.brick.getFxImage(), "speed");
                        default -> new Grass(j, i, Sprite.grass.getFxImage());
                    };

                    switch (line.charAt(j)) {
                        case 'p' -> bomber = new Bomber(
                                j, i,
                                Sprite.player_right.getFxImage(),
                                1, 2, 1, 1
                        );
                        case '1' -> enemy = new Balloon(j, i, Sprite.balloom_right1.getFxImage());
                    }
                    stillObjects.add(stillObject);
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clearMap() {
        stillObjects.clear();
        bomber = null;
        enemy = null;
        bomb = null;
    }

    private void update() {
        bomber.update();
        if (enemy != null)
            enemy.update();
        if (bomb != null)
            bomb.update();
    }

    private void collision() {
        bomber.specialObjectCollision(stillObjects, 0, 21);
        if (bomb != null) {
            if (enemy != null)
                bomb.bombFlameCollision(stillObjects, new ArrayList<>(), new ArrayList<>(Collections.singletonList(enemy)), 0, 21);
            else
                bomb.bombFlameCollision(stillObjects, new ArrayList<>(), new ArrayList<>(), 0, 21);
        }
    }

    private void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        stillObjects.forEach(o -> o.render(gc));
        if (enemy != null)
            enemy.render(gc);
        if (bomb != null)
            bomb.render(gc);
        bomber.render(gc);
    }

    private void addMenuBox() {
        Rectangle bg = new Rectangle(0, 0, 336, 480);
        bg.setFill(Color.color(0, 0, 0, 0.9));

        menuBox.getChildren().addAll(bg, menuBoxContent);

        getChildren().add(menuBox);
    }

    private void addBackButton() {
        HBox container = new HBox(50);
        VBox arrow = new VBox(-3);
        Line upper = new Line(0, 5, 8, 0);
        upper.setStroke(Color.WHITE);
        upper.setStrokeWidth(2.2);

        Line lower = new Line(0, 5, 8, 10);
        lower.setStroke(Color.WHITE);
        lower.setStrokeWidth(2.2);

        arrow.setTranslateX(40);
        arrow.setTranslateY(-2);
        arrow.getChildren().addAll(upper, lower);

        Text text = new Text("Back To Menu");
        text.setFill(Color.WHITE);
        text.setFont(Font.loadFont(
                Tutorial.class.getClassLoader().getResourceAsStream("fonts/PlayMeGames-Demo.otf"),
                14));

        TranslateTransition tt = new TranslateTransition(Duration.seconds(0.8), arrow);
        tt.setToX(45);
        tt.setCycleCount((int) Double.POSITIVE_INFINITY);
        tt.setAutoReverse(true);
        container.setOnMouseEntered(e -> {
            tt.playFromStart();
        });
        container.setOnMouseExited(e -> {
            tt.stop();
            arrow.setTranslateX(40);
        });
        container.setOnMouseClicked(e -> {
            timer.stop();
            BombermanGame.closeTutorial();
            Sprite.setScaledSize(2);
        });
        container.getChildren().addAll(arrow, text);
        menuBoxContent.getChildren().add(container);
    }

    private void addItem() {
        for (int i = 0; i < 2; i++) {
            HBox row = new HBox(100);
            for (int j = 0; j < 2; j++) {
                TutorialItem item = new TutorialItem(
                        items.get(i * 2 + j).getKey().getKey(),
                        items.get(i * 2 + j).getKey().getValue(),
                        items.get(i * 2 + j).getValue()
                );
                row.getChildren().add(item);
            }
            iconBox.getChildren().add(row);
            iconBox.setTranslateX(65);
        }
        menuBoxContent.getChildren().add(iconBox);
    }

    private void setDescription() {
        descriptionBox.getChildren().clear();
        Text heading = new Text();
        heading.setFill(Color.WHITE);
        heading.setTranslateY(15);
        heading.setTranslateX(30);
        heading.setFont(Font.font("Avenir Next LT Pro", FontWeight.BOLD, FontPosture.REGULAR, 18));
        switch (active) {
            case 0, -1 -> heading.setText("PLANT BOMBS");
            case 1 -> heading.setText("TAKE POWER-UPS");
            case 2 -> heading.setText("KILL ENEMIES");
            case 3 -> heading.setText("FIND PORTALS");
        }

        Text description = new Text();
        description.setFill(Color.WHITE);
        description.setTranslateX(30);
        description.setWrappingWidth(230);
        switch (active) {
            case 0, -1 -> description.setText("Bomber can plant a number of bombs at a time "
                    + "(start at 1). Use bombs to destroy brick and kill enemies. "
                    + "And remember to stay away from the flames");
            case 1 -> description.setText("Power-ups can be randomly found under bricks. "
                    + "Taking power-ups will help increase one of bomber's stats "
                    + "depending on the type of that power-up");
            case 2 -> description.setText("Enemies will try to kill bomberman. "
                    + "Different types of enemy need different strategies. "
                    + "A bomb explosion will eliminate them");
            case 3 -> description.setText("Portals are like power-ups (hidden under bricks). "
                    + "Find a portal to proceed to next stage when all enemies are eliminated");
        }
        descriptionBox.getChildren().addAll(heading, description);
    }

    private static class TutorialItem extends StackPane {
        private boolean active;
        private final Rectangle border;

        public TutorialItem(String name, Image image, Runnable action) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setOnMouseClicked(e -> action.run());

            imageView.effectProperty().bind(
                    Bindings.when(hoverProperty())
                            .then(new Glow(0.6))
                            .otherwise(new Glow(0))
            );

            border = new Rectangle(54, 54);
            border.setFill(Color.color((double) 140 / 255, (double) 140 / 255, (double) 140 / 255, 1));

            Rectangle clip = new Rectangle(50, 50);
            clip.setFill(Color.color(0, 0, 0, 0.75));

            Text text = new Text(name);
            text.setTranslateY(40);
            text.setFill(Color.WHITE);

            getChildren().addAll(border, clip, imageView, text);
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void updateBorder() {
            if (active) {
                border.setFill(Color.color(1, (double) 251 / 255, 0, 1));
                border.setEffect(new Glow(0.2));
            } else {
                border.setFill(Color.color((double) 140 / 255, (double) 140 / 255, (double) 140 / 255, 1));
                border.setEffect(null);
            }
        }
    }
}
