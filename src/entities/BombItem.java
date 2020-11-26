package entities;

import javafx.scene.image.Image;

public class BombItem extends PowerUp {
    public BombItem(int x, int y, Image img) {
        super(x, y, img);
    }

    @Override
    public void grantEffect(Bomber bomber) {
        bomber.increaseMaxBomb();
    }
}
