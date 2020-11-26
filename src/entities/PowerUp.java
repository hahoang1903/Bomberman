package entities;

import javafx.scene.image.Image;

public abstract class PowerUp extends Destroyable {
    public PowerUp(int x, int y, Image img) {
        super(x, y, img, "none");
        destroyable = true;
    }

    public abstract void grantEffect(Bomber bomber);
}
