package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

public class Balloon extends WanderEnemy {
    public Balloon(int x, int y, Image img) {
        super(x, y, img);
        left = new Image[]{
                Sprite.balloom_left1.getFxImage(),
                Sprite.balloom_left2.getFxImage(),
                Sprite.balloom_left3.getFxImage()
        };
        right = new Image[]{
                Sprite.balloom_right1.getFxImage(),
                Sprite.balloom_right2.getFxImage(),
                Sprite.balloom_right3.getFxImage()
        };
        deadSheet[0] = Sprite.balloom_dead.getFxImage();
        speed = 1;
    }
}
