import entities.*;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import graphics.Sprite;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BombermanGame extends Application {
    public static final int WIDTH = 31;
    public static final int HEIGHT = 15;

    private GraphicsContext gc;
    private static HBox scoreBoardContent;
    private static Canvas canvas;
    private static StartGameMenu startGameMenu;
    private static Tutorial tutorial;
    private static InGameMenu inGameMenu;
    private static final Settings settings = Settings.getInstance();
    private static final List<GameCharacter> characters = new ArrayList<>();
    private static final List<Entity> stillObjects = new ArrayList<>();
    private static final List<Bomb> bombs = new ArrayList<>();

    private static AnimationTimer timer;
    private static int level;
    private static final int maxLevel = 4;
    private static Group root;

    private static boolean pause;

    private static int hotkeyId = -1;

    private static MediaPlayer levelStartMusic;
    private static MediaPlayer inGameMusic;
    private static MediaPlayer levelCompleteMusic;
    private static MediaPlayer playerDeadMusic;
    private static MediaPlayer gameOverMusic;
    private static MediaPlayer endingMusic;

    public static void main(String[] args) {
        Application.launch(BombermanGame.class);
    }

    @Override
    public void start(Stage stage) {
        // Tao Canvas
        canvas = new Canvas(Sprite.SCALED_SIZE * WIDTH, Sprite.SCALED_SIZE * HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // Tao root container
        root = new Group();
        startGameMenu = new StartGameMenu();
        root.getChildren().addAll(startGameMenu);

        // Tao scene
        Scene scene = new Scene(root);

        // Them scene vao stage
        stage.setScene(scene);
        stage.show();

        ArrayList<String> inputs = new ArrayList<>();
        AtomicBoolean canPlaceBomb = new AtomicBoolean(true);
        AtomicBoolean canPause = new AtomicBoolean(true);
        scene.setOnKeyPressed(
                e -> {
                    String code = e.getCode().toString();
                    if (hotkeyId > 0)
                        Settings.setHotKey(hotkeyId, code);
                    if (!inputs.contains(code))
                        inputs.add(code);
                });

        scene.setOnKeyReleased(
                e -> {
                    String code = e.getCode().toString();
                    if (code.equals(Settings.HOTKEYS.get(0).getValue())) {
                        canPlaceBomb.set(true);
                    } else if (code.equals(Settings.HOTKEYS.get(5).getValue())) {
                        canPause.set(true);
                    }
                    inputs.remove(code);
                });

        timer = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                if (hotkeyId < 0)
                    handlePauseGame(inputs, canPause);
                if (!pause) {
                    handlePlayerDie((Bomber) characters.get(0));
                    handlePortal((Bomber) characters.get(0));
                    collision();
                    handleKeyInputs(inputs, canPlaceBomb);
                    render();
                    if (characters.size() != 0 && ((Bomber) characters.get(0)).isTakePowerUp()) {
                        createScoreBoardContent();
                        ((Bomber) characters.get(0)).setTakePowerUp(false);
                    }
                    update();
                }
            }
        };
    }

    private void handlePauseGame(ArrayList<String> inputs, AtomicBoolean canPause) {
        if (!canPause.get()) return;

        if (inputs.contains("ESCAPE")) {
            inputs.remove("ESCAPE");
            canPause.set(false);
            if (!pause) {
                pause = true;
                inGameMenu = new InGameMenu();
                root.getChildren().add(inGameMenu);
            } else {
                resumeFromPause();
            }
        }
    }

    private void handleKeyInputs(ArrayList<String> inputs, AtomicBoolean canPlaceBomb) {
        handleMovement(inputs);
        handlePlaceBomb(inputs, canPlaceBomb);
    }

    private void handleMovement(ArrayList<String> inputs) {
        Bomber player = (Bomber) characters.get(0);
        if (inputs.contains(Settings.HOTKEYS.get(3).getValue())) {
            player.setDirection("left");
            player.setMoving(true);
            if (player.isCollideLeft()) {
                if (player.canSlideUpLeft()) {
                    player.slideUp();
                } else if (player.canSlideDownLeft()) {
                    player.slideDown();
                }
                player.setVelocityX(0);
            } else {
                player.setVelocityX(-player.getSpeed());
            }
        } else if (inputs.contains(Settings.HOTKEYS.get(4).getValue())) {
            player.setDirection("right");
            player.setMoving(true);
            if (player.isCollideRight()) {
                if (player.canSlideUpRight()) {
                    player.slideUp();
                } else if (player.canSlideDownRight()) {
                    player.slideDown();
                }
                player.setVelocityX(0);
            } else {
                player.setVelocityX(player.getSpeed());
            }
        } else if (inputs.contains(Settings.HOTKEYS.get(2).getValue())) {
            player.setDirection("down");
            player.setMoving(true);
            if (player.isCollideBottom()) {
                if (player.canSlideLeftBottom()) {
                    player.slideLeft();
                } else if (player.canSlideRightBottom()) {
                    player.slideRight();
                }
                player.setVelocityY(0);
            } else {
                player.setVelocityY(player.getSpeed());
            }
        } else if (inputs.contains(Settings.HOTKEYS.get(1).getValue())) {
            player.setDirection("up");
            player.setMoving(true);
            if (player.isCollideTop()) {
                if (player.canSlideLeftTop()) {
                    player.slideLeft();
                } else if (player.canSlideRightTop()) {
                    player.slideRight();
                }
                player.setVelocityY(0);
            } else {
                player.setVelocityY(-player.getSpeed());
            }
        } else {
            player.setMoving(false);
            player.setVelocityX(0);
            player.setVelocityY(0);
        }
    }

    private void handlePlaceBomb(ArrayList<String> inputs, AtomicBoolean canPlaceBomb) {
        Bomber player = (Bomber) characters.get(0);
        if (inputs.contains(Settings.HOTKEYS.get(0).getValue())) {
            inputs.remove(Settings.HOTKEYS.get(0).getValue());
            Bomb bomb = null;
            if (canPlaceBomb.get())
                bomb = player.placeBomb(bombs);
            if (bomb != null) {
                bombs.add(bomb);
                canPlaceBomb.set(false);
            }
        }
    }

    private void handlePlayerDie(Bomber bomber) {
        if (!bomber.isDead()) return;

        timer.stop();
        inGameMusic.stop();
        inGameMusic = null;
        playerDeadMusic = new MediaPlayer(new Media(new File("src/sounds/05.mp3").toURI().toString()));
        playerDeadMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (bomber.getLives() < 0) {
            playerDeadMusic.setOnEndOfMedia(() -> {
                gameOverMusic = new MediaPlayer(new Media(new File("src/sounds/06.mp3").toURI().toString()));
                gameOverMusic.setVolume(Settings.VOLUMES.get(0).getValue());
                gameOverMusic.setOnReady(() -> {
                    gameOverMusic.play();
                    createAndShowStageScreen(gameOverMusic.getTotalDuration());
                });
                gameOverMusic.setOnEndOfMedia(() -> {
                    gameOverMusic = null;
                    returnToMenu();
                });
                playerDeadMusic = null;
            });
        } else {
            playerDeadMusic.setOnEndOfMedia(() -> {
                startLevel(level, false, true);
                playerDeadMusic = null;
            });
        }
        playerDeadMusic.play();
    }

    private void handlePortal(Bomber bomber) {
        if (bomber.isStandingOnPortal() && characters.size() == 1) {
            timer.stop();
            inGameMusic.stop();
            inGameMusic = null;
            levelCompleteMusic = new MediaPlayer(new Media(new File("src/sounds/04.mp3").toURI().toString()));
            levelCompleteMusic.setVolume(Settings.VOLUMES.get(0).getValue());
            levelCompleteMusic.play();
            if (++level > maxLevel) {
                levelCompleteMusic.setOnEndOfMedia(() -> {
                    endingMusic = new MediaPlayer(new Media(new File("src/sounds/07.mp3").toURI().toString()));
                    endingMusic.setVolume(Settings.VOLUMES.get(0).getValue());
                    endingMusic.setOnReady(() -> {
                        endingMusic.play();
                        createAndShowStageScreen(endingMusic.getTotalDuration());
                    });
                    endingMusic.setOnEndOfMedia(() -> {
                        endingMusic = null;
                        returnToMenu();
                    });
                    levelCompleteMusic = null;
                });
            } else {
                levelCompleteMusic.setOnEndOfMedia(() -> {
                    startLevel(level, false, true);
                    levelCompleteMusic = null;
                });
            }
        }
    }

    public static void startLevel(int level, boolean resumeFromMenu, boolean resumeInGame) {
        String fileName = resumeFromMenu ? "RecentMap" : "Level" + level;
        createMap("src/levels/" + fileName + ".txt", resumeFromMenu, resumeInGame);

        root.getChildren().clear();

        VBox canvasContainer = new VBox(-Sprite.SCALED_SIZE * 2);
        StackPane scoreBoard = new StackPane();
        scoreBoardContent = new HBox(20);
        createScoreBoardContent();
        scoreBoard.getChildren().addAll(
                new Rectangle(
                        Sprite.SCALED_SIZE * WIDTH,
                        Sprite.SCALED_SIZE * (HEIGHT - 13),
                        Color.color((double) 215 / 255, (double) 215 / 255, (double) 215 / 255)),
                scoreBoardContent
        );
        canvasContainer.getChildren().addAll(scoreBoard, canvas);

        root.getChildren().add(canvasContainer);

        if (pause) pause = false;
        timer.stop();

        if (startGameMenu != null)
            startGameMenu.getMediaPlayer().stop();
        startGameMenu = null;

        levelStartMusic = new MediaPlayer(new Media(new File("src/sounds/02.mp3").toURI().toString()));
        levelStartMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        levelStartMusic.setOnReady(() -> {
            levelStartMusic.play();
            createAndShowStageScreen(levelStartMusic.getTotalDuration());
        });
        levelStartMusic.setOnEndOfMedia(() -> {
            inGameMusic = new MediaPlayer(new Media(new File("src/sounds/03.mp3").toURI().toString()));
            inGameMusic.setVolume(Settings.VOLUMES.get(0).getValue());
            inGameMusic.setCycleCount((int) Double.POSITIVE_INFINITY);
            inGameMusic.play();

            timer.start();
            levelStartMusic = null;
        });
    }

    private static StackPane createScoreBoardItem(Image image, String content) {
        StackPane container = new StackPane();
        container.setTranslateX(500);
        container.setTranslateY(-5);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(25);
        imageView.setFitHeight(25);

        Text text = new Text(content);
        text.setTranslateY(20);

        container.getChildren().addAll(imageView, text);
        return container;
    }

    private static void createScoreBoardContent() {
        scoreBoardContent.getChildren().clear();
        Text text = new Text("Bomberman");
        text.setFont(Font.loadFont(BombermanGame.class.getClassLoader().getResourceAsStream("fonts/logo.ttf"),
                35));
        text.setTranslateX(80);
        text.setTranslateY(20);
        scoreBoardContent.getChildren().addAll(
                text,
                createScoreBoardItem(Sprite.heart.getFxImage(), String.valueOf(((Bomber) characters.get(0)).getLives())),
                createScoreBoardItem(Sprite.powerup_tutorial.getFxImage(), String.valueOf(characters.get(0).getSpeed())),
                createScoreBoardItem(Sprite.tutorial_bomb.getFxImage(), String.valueOf(((Bomber) characters.get(0)).getMaxBombs())),
                createScoreBoardItem(Sprite.flames.getFxImage(), String.valueOf(((Bomber) characters.get(0)).getFlameLength()))
        );
    }

    private static void createAndShowStageScreen(Duration duration) {
        Parent stageNameScreen = stageName(level);
        root.getChildren().add(stageNameScreen);

        FadeTransition ft = new FadeTransition(Duration.seconds(1), stageNameScreen);
        ft.setDelay(duration);
        ft.setToValue(0);
        ft.setOnFinished((e) -> root.getChildren().remove(stageNameScreen));
        ft.play();
    }

    private static Parent stageName(int level) {
        Pane pane = new Pane();
        Rectangle bg = new Rectangle(0, 0, WIDTH * Sprite.SCALED_SIZE, HEIGHT * Sprite.SCALED_SIZE);
        bg.setFill(Color.BLACK);

        String content;
        int justifyX;
        if (level > maxLevel) {
            content = "You have won the game";
            justifyX = 120;
        } else if (((Bomber) (characters.get(0))).getLives() < 0) {
            content = "Game Over";
            justifyX = 50;
        } else {
            content = "Stage " + level;
            justifyX = 40;
        }

        Text text = new Text(content);
        text.setFill(Color.WHITE);
        text.setFont(Font.loadFont(
                StartGameMenu.class.getClassLoader().getResourceAsStream("fonts/6809 chargen.ttf"),
                20));
        text.setTranslateX((double) WIDTH * Sprite.SCALED_SIZE / 2 - justifyX);
        text.setTranslateY((double) HEIGHT * Sprite.SCALED_SIZE / 2);

        pane.getChildren().addAll(bg, text);
        return pane;
    }

    private static void createMap(String filepath, boolean resumeFromMenu, boolean resumeInGame) {
        Bomber bomber = null;
        if (resumeInGame)
            bomber = (Bomber) characters.get(0);
        clearMap();
        try {
            File map = new File(filepath);
            Scanner fileReader = new Scanner(map);

            level = fileReader.nextInt();
            int row = fileReader.nextInt();
            int col = fileReader.nextInt();
            int lives;
            double speed;
            int maxBombs;
            int flameLength;
            if (resumeFromMenu) {
                lives = fileReader.nextInt();
                speed = fileReader.nextDouble();
                maxBombs = fileReader.nextInt();
                flameLength = fileReader.nextInt();
            } else if (resumeInGame) {
                lives = bomber.getLives();
                speed = bomber.getSpeed();
                maxBombs = bomber.getMaxBombs();
                flameLength = bomber.getFlameLength();
            } else {
                lives = 2;
                speed = 1.5;
                maxBombs = 1;
                flameLength = 1;
            }
            fileReader.nextLine(); // skip to next line after reading ints

            int playerX = 0;
            int playerY = 0;
            for (int i = 2; i < row + 2; i++) {
                String line = fileReader.nextLine();
                for (int j = 0; j < col; j++) {
                    Entity stillObject = switch (line.charAt(j)) {
                        case '#' -> new Wall(j, i, Sprite.wall.getFxImage());
                        case '*' -> new Brick(j, i, Sprite.brick.getFxImage(), generateRandomPowerUp("none", resumeFromMenu));
                        case 'x' -> new Brick(j, i, Sprite.brick.getFxImage(), "portal");
                        case 'b' -> new Brick(j, i, Sprite.brick.getFxImage(), generateRandomPowerUp("bomb", resumeFromMenu));
                        case 'f' -> new Brick(j, i, Sprite.brick.getFxImage(), generateRandomPowerUp("flame", resumeFromMenu));
                        case 's' -> new Brick(j, i, Sprite.brick.getFxImage(), generateRandomPowerUp("speed", resumeFromMenu));
                        default -> new Grass(j, i, Sprite.grass.getFxImage());
                    };

                    GameCharacter character;
                    switch (line.charAt(j)) {
                        case 'p' -> {
                            playerX = j;
                            playerY = i;
                            character = null;
                        }
                        case '1' -> character = new Balloon(j, i, Sprite.balloom_right1.getFxImage());
                        case '2' -> character = new Oneal(j, i, Sprite.oneal_right1.getFxImage());
                        case '3' -> character = new Doll(j, i, Sprite.doll_right1.getFxImage());
                        case '4' -> character = new Kondoria(j, i, Sprite.kondoria_right1.getFxImage());
                        default -> character = null;
                    }
                    if (character != null) {
                        characters.add(character);
                    }

                    stillObjects.add(stillObject);
                }
            }
            characters.add(0, new Bomber(playerX, playerY,
                    Sprite.player_right.getFxImage(), lives, speed, maxBombs, flameLength));
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomPowerUp(String powerUp, boolean resumeFromMenu) {
        if (resumeFromMenu) return powerUp;
        if (powerUp.equals("none")) {
            int random = new Random().nextInt(100) + 1;
            if (random <= level) {
                return "speed";
            } else if (random <= level * 2) {
                return "bomb";
            } else if (random <= level * 3) {
                return "flame";
            } else {
                return "none";
            }
        } else {
            return (new Random().nextInt(100) + 1) <= 75 ? powerUp : "none";
        }
    }

    private static void clearMap() {
        stillObjects.clear();
        characters.clear();
        bombs.clear();
    }

    public static void showTutorial() {
        tutorial = new Tutorial();
        root.getChildren().add(tutorial);
    }

    public static void closeTutorial() {
        root.getChildren().remove(tutorial);
        tutorial = null;
    }

    public static void resumeFromPause() {
        pause = false;
        root.getChildren().remove(inGameMenu);
        inGameMenu = null;
    }

    public static void returnToMenu() {
        timer.stop();
        if (inGameMusic != null) {
            inGameMusic.stop();
            inGameMusic = null;
        }
        exportCurrentState();
        root.getChildren().clear();

        startGameMenu = new StartGameMenu();
        root.getChildren().add(startGameMenu);
    }

    public static void showSettings(boolean isOpenFromMainMenu) {
        if (!isOpenFromMainMenu) {
            root.getChildren().remove(inGameMenu);
            inGameMenu = null;
        } else {
            startGameMenu.blurBackground();
        }
        Settings.setOpenFromMainMenu(isOpenFromMainMenu);
        hotkeyId = 0;
        root.getChildren().add(settings);
    }

    public static void closeSettings() {
        root.getChildren().remove(settings);
        hotkeyId = -1;
        if (Settings.isOpenFromMainMenu()) {
            startGameMenu.unBlurBackground();
        } else {
            inGameMenu = new InGameMenu();
            root.getChildren().add(inGameMenu);
        }
    }

    public void update() {
        bombs.forEach(Bomb::update);
        bombs.removeIf(Bomb::isExploded);
        characters.forEach(gameCharacter -> {
            if (gameCharacter instanceof Enemy)
                ((Enemy) gameCharacter).move(stillObjects, bombs, (Bomber) characters.get(0));
        });
        characters.forEach(GameCharacter::update);
        characters.removeIf(GameCharacter::isEnemyAndDead);
    }

    public void collision() {
        characters.forEach(character -> character.collision(stillObjects, bombs, characters));
        bombs.forEach(bomb -> bomb.bombFlameCollision(stillObjects, bombs, characters));
    }

    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        stillObjects.forEach(g -> g.render(gc));
        bombs.forEach(g -> g.render(gc));
        characters.forEach(g -> g.render(gc));
    }

    public static void setHotkeyId(int hotkeyId) {
        BombermanGame.hotkeyId = hotkeyId;
    }

    private static void exportCurrentState() {
        if (stillObjects.isEmpty()) return;

        try (BufferedWriter bw = new BufferedWriter(new PrintWriter("src/levels/RecentMap.txt"))) {
            Bomber bomber = (Bomber) characters.get(0);
            if (bomber.getLives() < 0 || level > maxLevel) return;
            String info = level + " "
                    + (HEIGHT - 2) + " "
                    + WIDTH + " "
                    + bomber.getLives() + " "
                    + bomber.getSpeed() + " "
                    + bomber.getMaxBombs() + " "
                    + bomber.getFlameLength();
            bw.write(info);
            bw.newLine();

            characters.forEach(character -> {
                int i = (character.getY() + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE - 2;
                int j = (character.getX() + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;

                switch (character.getClass().getName()) {
                    case "entities.Bomber" -> stillObjects.set(i * WIDTH + j, new Brick("bomber"));
                    case "entities.Balloon" -> stillObjects.set(i * WIDTH + j, new Brick("balloon"));
                    case "entities.Oneal" -> stillObjects.set(i * WIDTH + j, new Brick("oneal"));
                    case "entities.Doll" -> stillObjects.set(i * WIDTH + j, new Brick("doll"));
                    case "entities.Kondoria" -> stillObjects.set(i * WIDTH + j, new Brick("kondoria"));
                }
            });

            for (int i = 0; i < HEIGHT - 2; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < WIDTH; j++) {
                    Entity object = stillObjects.get(i * WIDTH + j);
                    switch (object.getClass().getName()) {
                        case "entities.Wall" -> row.append("#");
                        case "entities.Grass" -> row.append(" ");
                        case "entities.Brick" -> {
                            switch (((Brick) object).getUnderlineObject()) {
                                case "none" -> row.append("*");
                                case "portal" -> row.append("x");
                                case "flame" -> row.append("f");
                                case "bomb" -> row.append("b");
                                case "speed" -> row.append("s");
                                case "bomber" -> row.append("p");
                                case "balloon" -> row.append("1");
                                case "oneal" -> row.append("2");
                                case "doll" -> row.append("3");
                                case "kondoria" -> row.append("4");
                            }
                        }
                        case "entities.SpeedItem" -> row.append("s");
                        case "entities.FlameItem" -> row.append("f");
                        case "entities.BombItem" -> row.append("b");
                        case "entities.Portal" -> row.append("x");
                    }
                }
                bw.write(String.valueOf(row));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setMusicVolume() {
        if (levelStartMusic != null) levelStartMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (inGameMusic != null) inGameMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (levelCompleteMusic != null) levelCompleteMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (playerDeadMusic != null) playerDeadMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (gameOverMusic != null) gameOverMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (endingMusic != null) endingMusic.setVolume(Settings.VOLUMES.get(0).getValue());
        if (startGameMenu != null) startGameMenu.getMediaPlayer().setVolume(Settings.VOLUMES.get(0).getValue());
    }

    @Override
    public void stop() {
        exportCurrentState();
        Settings.saveSettings();
    }
}
