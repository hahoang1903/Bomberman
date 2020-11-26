package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

import java.util.List;

public abstract class Destroyable extends Entity {
    protected boolean destroyable;

    protected final String underline;
    protected Image[] sheet;

    protected int maxFrames;
    protected int currentFrame;

    public Destroyable(int x, int y, Image img, String underline) {
        super(x, y, img);
        this.underline = underline;
        destroyable = false;
    }

    @Override
    public void update() {
        if (currentFrame >= maxFrames * 3 - 1) {
            destroyable = true;
            return;
        }
        img = sheet[currentFrame++ / maxFrames];
    }

    public void destroy(List<Entity> stillObjects, int heightDiff, int WIDTH) {
        int i = y / Sprite.SCALED_SIZE;
        int j = x / Sprite.SCALED_SIZE;
        Entity underlineObj = switch (underline) {
            case "portal" -> new Portal(j, i, Sprite.portal.getFxImage());
            case "bomb" -> new BombItem(j, i, Sprite.powerup_bombs.getFxImage());
            case "flame" -> new FlameItem(j, i, Sprite.powerup_flames.getFxImage());
            case "speed" -> new SpeedItem(j, i, Sprite.powerup_speed.getFxImage());
            default -> new Grass(j, i, Sprite.grass.getFxImage());
        };
        stillObjects.set((i - heightDiff) * WIDTH + j, underlineObj);
    }

    public boolean isDestroyable() {
        return destroyable;
    }
}
