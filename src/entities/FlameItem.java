package entities;

import javafx.scene.image.Image;

public class FlameItem extends PowerUp {
    public FlameItem(int x, int y, Image img) {
        super(x, y, img);
    }

    @Override
    public void grantEffect(Bomber bomber) {
        bomber.increaseFlameLength();
    }
}
