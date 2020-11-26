import entities.Entity;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Settings extends StackPane {
    public static final List<Pair<Integer, String>> HOTKEYS = new ArrayList<>();
    public static final List<Pair<Integer, Double>> VOLUMES = new ArrayList<>();
    private static Settings instance;

    private boolean openFromMainMenu;
    private final Rectangle bg;
    private final VBox settingsBox;
    private HotkeySettings hotkeySettings;
    private SoundSettings soundSettings;

    private Settings() {
        loadSettings();

        MenuItem.setActive(1);
        bg = new Rectangle(0, 0, 992, 480);
        bg.setFill(Color.web("black", 0.75));

        settingsBox = new VBox();
        settingsBox.setTranslateY(100);
        settingsBox.setTranslateX(200);

        addBackButton();
        addSettingsMenu();

        hotkeySettings = new HotkeySettings();
        settingsBox.getChildren().add(hotkeySettings);
        getChildren().addAll(bg, settingsBox);
    }

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null)
                    instance = new Settings();
            }
        }
        return instance;
    }

    private static void loadSettings() {
        try {
            File settings = new File("src/settings/settings.txt");
            Scanner fileReader = new Scanner(settings);

            for (int i = 1; i <= 6; i++) {
                String key = fileReader.nextLine();
                HOTKEYS.add(new Pair<>(i, key));
            }

            for (int i = 1; i < 3; i++) {
                double volume = Double.parseDouble(fileReader.nextLine());
                VOLUMES.add(new Pair<>(i, volume));
            }
            Entity.SFX = VOLUMES.get(1).getValue();

            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveSettings() {
        try (BufferedWriter bw = new BufferedWriter(new PrintWriter("src/settings/settings.txt"))) {
            for (Pair<Integer, String> hotkey : HOTKEYS) {
                bw.write(hotkey.getValue());
                bw.newLine();
            }
            for (Pair<Integer, Double> volume : VOLUMES) {
                bw.write(String.valueOf(volume.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setOpenFromMainMenu(boolean openFromMainMenu) {
        instance.openFromMainMenu = openFromMainMenu;
        setBackground(openFromMainMenu);
    }

    public static boolean isOpenFromMainMenu() {
        return instance.openFromMainMenu;
    }

    private static void setBackground(boolean openFromMainMenu) {
        if (openFromMainMenu)
            instance.bg.setFill(Color.web("black", 0.3));
        else
            instance.bg.setFill(Color.web("black", 0.75));
    }

    public static void setHotKey(int id, String key) {
        Settings.HotkeySettings.HotkeyItem.setHotkey(id, key);
    }

    private Parent addBackButton() {
        StackPane container = new StackPane();
        VBox arrow = new VBox(-3);
        Line upper = new Line(0, 5, 8, 0);
        upper.setStroke(Color.WHITE);
        upper.setStrokeWidth(2.2);

        Line lower = new Line(0, 5, 8, 10);
        lower.setStroke(Color.WHITE);
        lower.setStrokeWidth(2.2);

        arrow.setTranslateY(15);
        arrow.setTranslateX(18);
        arrow.getChildren().addAll(upper, lower);

        TranslateTransition tt = new TranslateTransition(Duration.seconds(1), arrow);
        tt.setToX(25);
        tt.setFromX(15);
        tt.setCycleCount((int) Double.POSITIVE_INFINITY);
        tt.setAutoReverse(true);
        container.setOnMouseEntered(e -> tt.playFromStart());
        container.setOnMouseExited(e -> {
            tt.stop();
            arrow.setTranslateX(18);
        });
        container.setOnMouseClicked(e -> BombermanGame.closeSettings());

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.2, Color.web("black", 0.75)),
                new Stop(1.0, Color.web("black", 0.3))
        );
        Rectangle bg0 = new Rectangle(50, 40, gradient);
        Rectangle bg1 = new Rectangle(50, 40, Color.web("black", 0.2));
        container.getChildren().addAll(bg0, bg1, arrow);
        container.setTranslateY(1);

        return container;
    }

    private void addSettingsMenu() {
        HBox menu = new HBox();

        menu.getChildren().addAll(
                addBackButton(),
                new MenuItem("HOTKEYS", () -> {
                    if (MenuItem.active == 1) return;
                    MenuItem.setActive(1);
                    menu.getChildren().forEach(item -> {
                        if (item instanceof MenuItem)
                            ((MenuItem) item).update();
                    });
                    settingsBox.getChildren().remove(soundSettings);
                    soundSettings = null;
                    hotkeySettings = new HotkeySettings();
                    settingsBox.getChildren().add(hotkeySettings);
                }, 1),
                new MenuItem("SOUND", () -> {
                    if (MenuItem.active == 2) return;

                    MenuItem.setActive(2);
                    menu.getChildren().forEach(item -> {
                        if (item instanceof MenuItem)
                            ((MenuItem) item).update();
                    });
                    settingsBox.getChildren().remove(hotkeySettings);
                    hotkeySettings = null;
                    soundSettings = new SoundSettings();
                    settingsBox.getChildren().add(soundSettings);
                }, 2));
        settingsBox.getChildren().add(menu);
    }

    private static class MenuItem extends VBox {
        private static int active;
        private final Rectangle line;
        private final Text text;
        private final int id;

        public static void setActive(int active) {
            MenuItem.active = active;
        }

        public MenuItem(String name, Runnable action, int id) {
            this.id = id;
            LinearGradient gradient = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.2, Color.web("black", 0.75)),
                    new Stop(1.0, Color.web("black", 0.3))
            );
            Rectangle bg0 = new Rectangle(200, 40, gradient);
            Rectangle bg1 = new Rectangle(200, 40, Color.web("black", 0.2));

            line = new Rectangle(200, 3);
            line.setFill(id == active ? Color.YELLOW : Color.GRAY);

            text = new Text(name);
            text.setFont(Font.font(20));
            text.effectProperty().bind(
                    Bindings.when(hoverProperty())
                            .then(new Glow(0.8))
                            .otherwise(new Glow(0))
            );
            setOnMouseEntered(e -> text.setFill(Color.WHITE));
            setOnMouseExited(e -> {
                if (id != active) text.setFill(Color.GRAY);
            });
            text.setFill(id == active ? Color.WHITE : Color.GRAY);

            setOnMouseClicked(e -> {
                action.run();
                ((MenuItem) e.getSource()).update();
            });

            StackPane box = new StackPane(bg0, bg1, text);

            getChildren().addAll(line, box);
        }

        private void update() {
            line.setFill(id == active ? Color.YELLOW : Color.GRAY);
            text.setFill(id == active ? Color.WHITE : Color.GRAY);
        }
    }

    private static class HotkeySettings extends VBox {
        public HotkeySettings() {
            addKeys();
        }

        private void addKeys() {
            getChildren().addAll(
                    new HotkeyItem(0, "COMMAND", "KEY", null),
                    new HotkeyItem(1, "Plant Bomb", HOTKEYS.get(0).getValue(), () -> BombermanGame.setHotkeyId(1)),
                    new HotkeyItem(2, "Move Up", HOTKEYS.get(1).getValue(), () -> BombermanGame.setHotkeyId(2)),
                    new HotkeyItem(3, "Move Down", HOTKEYS.get(2).getValue(), () -> BombermanGame.setHotkeyId(3)),
                    new HotkeyItem(4, "Move Left", HOTKEYS.get(3).getValue(), () -> BombermanGame.setHotkeyId(4)),
                    new HotkeyItem(5, "Move Right", HOTKEYS.get(4).getValue(), () -> BombermanGame.setHotkeyId(5)),
                    new HotkeyItem(6, "Pause", HOTKEYS.get(5).getValue(), () -> BombermanGame.setHotkeyId(6))
            );
        }

        private static class HotkeyItem extends HBox {
            private static final List<HotkeyItem> hotkeys = new ArrayList<>();
            private final int id;
            private final Text keyText;

            public HotkeyItem(int id, String description, String key, Runnable action) {
                hotkeys.add(this);
                this.id = id;
                StackPane descriptionContainer = new StackPane();
                Text descriptionText = new Text(description);
                descriptionText.setFill(Color.WHITE);
                descriptionText.setFont(Font.font(14));
                descriptionContainer.getChildren().addAll(
                        new Rectangle(300, 40, Color.web("black", 0.75)),
                        descriptionText
                );

                StackPane keyContainer = new StackPane();
                keyText = new Text(key);
                if (id != 0) {
                    keyText.fillProperty().bind(
                            Bindings.when(hoverProperty())
                                    .then(Color.WHITE)
                                    .otherwise(Color.GRAY)
                    );
                    keyContainer.setOnMouseClicked(e -> {
                        action.run();
                        hotkeys.forEach(item -> {
                            if (item.id != 0)
                                item.keyText.setText(HOTKEYS.get(item.id - 1).getValue());
                        });
                        keyText.setText("PRESS TO ASSIGN NEW KEY");
                    });
                } else {
                    keyText.setFill(Color.WHITE);
                }
                keyText.setFont(Font.font(14));
                keyContainer.getChildren().addAll(
                        new Rectangle(300, 40, Color.web("black", 0.75)),
                        keyText
                );

                if (id == 0) {
                    String style = "-fx-border-insets: 0;"
                            + "-fx-border-color: rgba(172,172,172,0.75);"
                            + "-fx-border-width: 0 0 1 0;"
                            + "-fx-border-style: hidden hidden solid hidden;";
                    descriptionContainer.setStyle(style);
                    keyContainer.setStyle(style);
                }

                getChildren().addAll(descriptionContainer, keyContainer);
            }

            public static void setHotkey(int id, String key) {
                for (HotkeyItem item : hotkeys) {
                    if (item.id == id) {
                        BombermanGame.setHotkeyId(0);
                        HOTKEYS.set(id - 1, new Pair<>(id, key));
                        item.keyText.setText(key);
                        break;
                    }
                }
            }
        }
    }

    private static class SoundSettings extends VBox {
        public SoundSettings() {
            addSliders();
        }

        private void addSliders() {
            getChildren().addAll(
                    new SoundItem(1, "Music"),
                    new SoundItem(2, "SFX")
            );
        }

        private static class SoundItem extends HBox {
            public SoundItem(int id, String name) {
                StackPane descriptionContainer = new StackPane();
                Text descriptionText = new Text(name);
                descriptionText.setFill(Color.WHITE);
                descriptionText.setFont(Font.font(16));
                descriptionContainer.getChildren().addAll(
                        new Rectangle(300, 60, Color.web("black", 0.75)),
                        descriptionText
                );

                StackPane sliderContainer = new StackPane();
                Slider slider = new Slider(0, 1, VOLUMES.get(id - 1).getValue());
                slider.setShowTickMarks(true);
                slider.setShowTickLabels(true);
                slider.setMajorTickUnit(0.25f);
                slider.setMaxWidth(250);
                sliderContainer.getChildren().addAll(
                        new Rectangle(300, 60, Color.web("black", 0.75)),
                        slider
                );
                slider.valueProperty().addListener(
                        (observable, oldValue, newValue) -> VOLUMES.forEach(volume -> {
                            if (volume.getKey() == id) {
                                VOLUMES.set(id - 1, new Pair<>(id, (Double) newValue));
                                if (id == 1) BombermanGame.setMusicVolume();
                                if (id == 2) Entity.SFX = (double) newValue;
                            }
                        }));
                getChildren().addAll(descriptionContainer, sliderContainer);
            }
        }
    }
}

