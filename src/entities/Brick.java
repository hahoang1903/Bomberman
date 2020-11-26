package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

public class Brick extends Destroyable {
    public Brick(int x, int y, Image img, String underline) {
        super(x, y, img, underline);
        maxFrames = 5;
        currentFrame = 0;
        sheet = new Image[]{
                Sprite.brick_exploded.getFxImage(),
                Sprite.brick_exploded1.getFxImage(),
                Sprite.brick_exploded2.getFxImage()
        };
    }

    public Brick(String underline) {
        this(0, 0, null, underline);
    }

    public String getUnderlineObject() {
        return underline;
    }
}
