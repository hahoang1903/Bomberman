package entities;

import javafx.scene.image.Image;

public class SpeedItem extends PowerUp {
    public SpeedItem(int x, int y, Image img) {
        super(x, y, img);
    }

    @Override
    public void grantEffect(Bomber bomber) {
        bomber.increaseSpeed();
    }
}
