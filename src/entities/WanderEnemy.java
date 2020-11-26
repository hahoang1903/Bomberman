package entities;

import javafx.scene.image.Image;

import java.util.List;

public abstract class WanderEnemy extends Enemy {
    public WanderEnemy(int x, int y, Image img) {
        super(x, y, img);
    }

    @Override
    public void move(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber) {
        if (cantMoveRandom()) return;
        autoMove();
    }
}