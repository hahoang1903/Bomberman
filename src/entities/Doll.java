package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

public class Doll extends WanderEnemy{
    public Doll(int x, int y, Image img) {
        super(x, y, img);
        left = new Image[]{
                Sprite.doll_left1.getFxImage(),
                Sprite.doll_left2.getFxImage(),
                Sprite.doll_left3.getFxImage()
        };
        right = new Image[]{
                Sprite.doll_right1.getFxImage(),
                Sprite.doll_right2.getFxImage(),
                Sprite.doll_right3.getFxImage()
        };
        deadSheet[0] = Sprite.doll_dead.getFxImage();
        speed = 2;
    }
}
