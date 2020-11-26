package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

import java.util.List;

public abstract class ChaseEnemy extends Enemy {
    private final int vision;

    private boolean chasing;

    public ChaseEnemy(int x, int y, Image img) {
        super(x, y, img);
        vision = 6;
        chasing = false;
    }

    @Override
    public void move(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber) {
        chaseIfSeeBomber(stillObjects, bombs, bomber);
        if (!chasing) {
            if (cantMoveRandom()) return;
        }
        autoMove();
    }

    private void chaseIfSeeBomber(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber) {
        int standingJ = x / Sprite.SCALED_SIZE;
        int standingI = y / Sprite.SCALED_SIZE - 2;
        chasing = false;
        if (y % Sprite.SCALED_SIZE == 0) {
            if (bomberInLeftUp(stillObjects, bombs, bomber, standingJ, standingI, "left")) {
                chasing = true;
                direction = "left";
            } else if (bomberInRightDown(stillObjects, bombs, bomber, standingJ, standingI, "right")) {
                direction = "right";
                chasing = true;
            }
        }

        if (x % Sprite.SCALED_SIZE == 0) {
            if (bomberInLeftUp(stillObjects, bombs, bomber, standingI, standingJ, "up")) {
                direction = "up";
                chasing = true;
            } else if (bomberInRightDown(stillObjects, bombs, bomber, standingI, standingJ, "down")) {
                direction = "down";
                chasing = true;
            }
        }
    }

    private boolean bomberInLeftUp(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber, int pos1, int pos2, String direction) {
        int WIDTH = 31;
        for (int i = pos1; i >= Math.max(pos1 - vision, 0); i--) {
            Entity object;
            if (direction.equals("up")) {
                object = stillObjects.get(i * WIDTH + pos2);
            } else {
                object = stillObjects.get(pos2 * WIDTH + i);
            }
            if (object instanceof Wall || object instanceof Brick)
                return false;

            for (Bomb bomb : bombs) {
                if (object.x == bomb.x && object.y == bomb.y) {
                    return false;
                }
            }

            int playerCenterJ = (bomber.x + (Sprite.SCALED_SIZE - 8) / 2) / Sprite.SCALED_SIZE;
            int playerCenterI = (bomber.y + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;

            if (playerCenterJ == object.x / Sprite.SCALED_SIZE
                    && playerCenterI == object.y / Sprite.SCALED_SIZE)
                return true;
        }
        return false;
    }

    private boolean bomberInRightDown(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber, int pos1, int pos2, String direction) {
        int WIDTH = 31;
        int HEIGHT = 13;
        int maxLength;
        if (direction.equals("down")) {
            maxLength = HEIGHT;
        } else {
            maxLength = WIDTH;
        }

        for (int i = pos1; i <= Math.min(pos1 + vision, maxLength - 1); i++) {
            Entity object;
            if (direction.equals("down")) {
                object = stillObjects.get(i * WIDTH + pos2);
            } else {
                object = stillObjects.get(pos2 * WIDTH + i);
            }
            if (object instanceof Wall || object instanceof Brick)
                return false;

            for (Bomb bomb : bombs) {
                if (object.x == bomb.x && object.y == bomb.y) {
                    return false;
                }
            }

            int playerCenterJ = (bomber.x + (Sprite.SCALED_SIZE - 8) / 2) / Sprite.SCALED_SIZE;
            int playerCenterI = (bomber.y + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;

            if (playerCenterJ == object.x / Sprite.SCALED_SIZE
                    && playerCenterI == object.y / Sprite.SCALED_SIZE)
                return true;
        }
        return false;
    }
}
