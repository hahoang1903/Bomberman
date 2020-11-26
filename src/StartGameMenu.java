import graphics.Sprite;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class StartGameMenu extends Pane {
    private final List<Pair<String, Runnable>> menuData = Arrays.asList(
            new Pair<String, Runnable>("Start New Game",
                    () -> BombermanGame.startLevel(1, false, false)),
            new Pair<String, Runnable>("Resume Game",
                    () -> BombermanGame.startLevel(0, true, false)),
            new Pair<String, Runnable>("How to Play",
                    BombermanGame::showTutorial),
            new Pair<String, Runnable>("Game Settings", () ->
                    BombermanGame.showSettings(true)),
            new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
    );
    private final VBox menuBox = new VBox(15);
    private ImageView imageView;
    private final MediaPlayer mediaPlayer;

    public StartGameMenu() {
        Media sound = new Media(new File("src/sounds/01.mp3").toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setCycleCount((int) Double.POSITIVE_INFINITY);
        mediaPlayer.setVolume(Settings.VOLUMES.get(0).getValue());
        addBackground();
        addMenu();
        startAnimation();
        mediaPlayer.play();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void blurBackground() {
        imageView.setEffect(new GaussianBlur());
        menuBox.setEffect(new GaussianBlur());
    }

    public void unBlurBackground() {
        imageView.setEffect(null);
        menuBox.setEffect(null);
    }

    private void addBackground() {
        imageView = new ImageView(new Image(getClass().getResource("/textures/background.png").toExternalForm()));
        imageView.setFitWidth(BombermanGame.WIDTH * Sprite.SCALED_SIZE);
        imageView.setFitHeight(BombermanGame.HEIGHT * Sprite.SCALED_SIZE);

        getChildren().add(imageView);
    }

    private void startAnimation() {
        for (int i = 0; i < menuBox.getChildren().size(); i++) {
            Node n = menuBox.getChildren().get(i);
            TranslateTransition tt = new TranslateTransition(Duration.seconds(1.1 + i * 0.2), n);
            tt.setToX(-i * 20);
            tt.play();
        }
    }

    private void addMenu() {
        menuBox.setTranslateY(220);
        menuData.forEach(data -> {
            boolean disable = false;
            if (menuData.indexOf(data) == 1) {
                File map = new File("src/levels/RecentMap.txt");
                if (!map.exists()) {
                    disable = true;
                } else {
                    try {
                        Scanner reader = new Scanner(map);
                        if (!reader.hasNext()) disable = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            GameMenuItem item = new GameMenuItem(data.getKey(), data.getValue(), disable);
            item.setItem(menuData.size() - 1 - menuData.indexOf(data));
            item.setTranslateX(-700);
            menuBox.getChildren().addAll(item);
        });

        getChildren().add(menuBox);
    }

    private static class GameMenuItem extends StackPane {
        private final String name;
        private final boolean disable;

        public GameMenuItem(String name, Runnable action, boolean disable) {
            this.name = name;
            this.disable = disable;

            if (!disable) {
                setOnMouseClicked(e -> action.run());
            }
        }

        public void setItem(int index) {
            Polygon bg = new Polygon(
                    0, 0,
                    index * 40 + 200, 0,
                    index * 40 + 215, 15,
                    index * 40 + 200, 30,
                    0, 30
            );
            bg.setTranslateX(0);

            if (!disable) {
                bg.fillProperty().bind(
                        Bindings.when(hoverProperty())
                                .then(Color.color((double) 229 / 255, (double) 83 / 255, (double) 15 / 255, 0.9))
                                .otherwise(Color.color((double) 165 / 255, (double) 66 / 255, (double) 46 / 255, 0.8))
                );

                bg.effectProperty().bind(
                        Bindings.when(pressedProperty())
                                .then(new InnerShadow(10, Color.BLACK))
                                .otherwise(new InnerShadow(0, Color.BLACK))
                );
            } else {
                bg.setFill(Color.color((double) 99 / 255, (double) 50 / 255, (double) 42 / 255, 0.75));
            }

            Text text = new Text(name);
            text.setTranslateX(index * 20);
            text.setFont(Font.loadFont(
                    StartGameMenu.class.getClassLoader().getResourceAsStream("fonts/PlayMeGames-Demo.otf"),
                    13));

            if (!disable) {
                text.fillProperty().bind(
                        Bindings.when(hoverProperty())
                                .then(Color.WHITE)
                                .otherwise(Color.color((double) 226 / 255, (double) 226 / 255, (double) 226 / 255, 1))
                );
            } else {
                text.setFill(Color.color((double) 160 / 255, (double) 160 / 255, (double) 160 / 255, 0.75));
            }

            getChildren().addAll(bg, text);
        }
    }
}