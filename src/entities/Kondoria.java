package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

public class Kondoria extends ChaseEnemy {
    public Kondoria(int x, int y, Image img) {
        super(x, y, img);
        left = new Image[]{
                Sprite.kondoria_left1.getFxImage(),
                Sprite.kondoria_left2.getFxImage(),
                Sprite.kondoria_left3.getFxImage()
        };
        right = new Image[]{
                Sprite.kondoria_right1.getFxImage(),
                Sprite.kondoria_right2.getFxImage(),
                Sprite.kondoria_right3.getFxImage()
        };
        deadSheet[0] = Sprite.kondoria_dead.getFxImage();
        speed = 1;
    }
}
