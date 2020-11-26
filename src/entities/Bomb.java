package entities;

import graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Bomb extends Entity {
    private int maxFrames;
    private int currentFrame;

    private final Image[] bombSheet;
    private final Image[] explodeSheet;

    private final int explodeTime;
    private int timer;

    private boolean exploded;
    private boolean exploding;

    private final List<Flame> flames;

    private final Bomber bomber;
    private MediaPlayer explosionSFX;

    public Bomb(int x, int y, Image img, Bomber bomber) {
        super(x, y, img);
        this.bomber = bomber;
        bombSheet = new Image[]{
                Sprite.bomb.getFxImage(),
                Sprite.bomb_1.getFxImage(),
                Sprite.bomb_2.getFxImage()
        };
        explodeSheet = new Image[]{
                Sprite.bomb_exploded.getFxImage(),
                Sprite.bomb_exploded1.getFxImage(),
                Sprite.bomb_exploded2.getFxImage(),
                Sprite.bomb_exploded2.getFxImage(),
                Sprite.bomb_exploded1.getFxImage(),
                Sprite.bomb_exploded.getFxImage()
        };
        maxFrames = 15;
        currentFrame = 0;
        explodeTime = 120;
        timer = 0;
        exploded = false;
        exploding = false;
        flames = new ArrayList<>();
        flames.add(new Flame(
                x - bomber.getFlameLength(), y,
                null, bomber.getFlameLength(),
                "left", this));
        flames.add(new Flame(
                x + bomber.getFlameLength(), y,
                null, bomber.getFlameLength(),
                "right", this));
        flames.add(new Flame(
                x, y - bomber.getFlameLength(),
                null, bomber.getFlameLength(),
                "up", this));
        flames.add(new Flame(
                x, y + bomber.getFlameLength(),
                null, bomber.getFlameLength(),
                "down", this));
    }

    @Override
    public void update() {
        if (timer <= explodeTime) {
            if (currentFrame >= maxFrames * 3) {
                currentFrame = 0;
            }
        } else {
            if (explosionSFX == null) {
                explosionSFX = new MediaPlayer(new Media(new File("src/sounds/SFX6.mp3").toURI().toString()));
                explosionSFX.setVolume(Entity.SFX);
                explosionSFX.play();
            }
            if (currentFrame >= maxFrames * 6 - 1) {
                explosionSFX = null;
                exploded = true;
                bomber.increaseAvailableBomb();
            }
        }

        if (timer < explodeTime) {
            img = bombSheet[currentFrame++ / maxFrames];
        } else if (timer == explodeTime) {
            currentFrame = 0;
            maxFrames = 5;
            exploding = true;
        } else {
            img = explodeSheet[currentFrame++ / maxFrames];
            flames.forEach(Flame::update);
        }
        timer++;
    }

    public void boom() {
        if (!exploding)
            currentFrame = 0;
        maxFrames = 5;
        exploding = true;
        timer = explodeTime + 1;
    }

    public void bombFlameCollision(List<Entity> stillObjects, List<Bomb> bombs, List<GameCharacter> characters, int heightDiff, int WIDTH) {
        flames.forEach(flame -> flame.collision(stillObjects, bombs, characters, heightDiff, WIDTH));
    }

    public void bombFlameCollision(List<Entity> stillObjects, List<Bomb> bombs, List<GameCharacter> characters) {
        bombFlameCollision(stillObjects, bombs, characters, 2, 31);
    }

    @Override
    public void render(GraphicsContext gc) {
        super.render(gc);
        flames.forEach(flame -> flame.render(gc));
    }

    public boolean isExploded() {
        return exploded;
    }

    public boolean isExploding() {
        return exploding;
    }
}
