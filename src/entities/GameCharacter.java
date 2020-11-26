package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

import java.util.List;

public abstract class GameCharacter extends Entity {
    protected Image[] left;
    protected Image[] right;
    protected Image[] up;
    protected Image[] down;
    protected Image[] deadSheet;

    protected int maxFrames;
    protected int currentFrame;

    protected String direction;
    protected boolean moving;

    protected double speed;
    protected double velocityX;
    protected double velocityY;

    protected boolean collideLeft;
    protected boolean collideRight;
    protected boolean collideTop;
    protected boolean collideBottom;

    protected boolean slideUpLeft;
    protected boolean slideUpRight;
    protected boolean slideDownLeft;
    protected boolean slideDownRight;
    protected boolean slideLeftTop;
    protected boolean slideLeftBottom;
    protected boolean slideRightTop;
    protected boolean slideRightBottom;

    protected boolean markedAsDead;
    protected boolean dead;

    public GameCharacter(int x, int y, Image img) {
        super(x, y, img);
        collideLeft = false;
        collideRight = false;
        collideTop = false;
        collideBottom = false;
        slideUpLeft = false;
        slideUpRight = false;
        slideDownLeft = false;
        slideDownRight = false;
        slideLeftTop = false;
        slideLeftBottom = false;
        slideRightTop = false;
        slideRightBottom = false;
    }

    protected void stillCollision(List<Entity> stillObjects, List<Bomb> bombs) {
        if (!moving) return;

        collideLeft = horizontalStillCollision(stillObjects, bombs, "left");
        collideRight = horizontalStillCollision(stillObjects, bombs, "right");
        collideTop = verticalStillCollision(stillObjects, bombs, "up");
        collideBottom = verticalStillCollision(stillObjects, bombs, "down");
    }

    private boolean horizontalStillCollision(List<Entity> stillObjects, List<Bomb> bombs, String direction) {
        int spriteSize = Sprite.SCALED_SIZE;
        int characterWidth;
        if (this instanceof Bomber) {
            characterWidth = spriteSize - 8;
        } else {
            characterWidth = spriteSize;
        }
        int top = y / spriteSize - 2;
        int left = x / spriteSize;

        final int WIDTH = 31;
        final int HEIGHT = 13;

        if (!(this instanceof Bomber) || ((Bomber) this).notJustPlacedBomb()) {
            for (Bomb bomb : bombs) {
                if (yCollision(direction, spriteSize, characterWidth, bomb)) return true;
            }
        }

        for (int i = Math.max(top - 1, 0); i <= Math.min(top + 1, HEIGHT - 1); i++) {
            Entity object;
            if (direction.equals("left")) {
                object = stillObjects.get(i * WIDTH + Math.max(left - 1, 0));
            } else {
                object = stillObjects.get(i * WIDTH + Math.min(left + 1, WIDTH - 1));
            }

            if (!(object instanceof Wall) && (!(object instanceof Brick) || this instanceof Kondoria)) {
                continue;
            }

            if (yCollision(direction, spriteSize, characterWidth, object)) {
                if (direction.equals("left")) {
                    slideUpLeft = object.y < y + spriteSize && y + spriteSize <= object.y + spriteSize / 3;
                    slideDownLeft = object.y + spriteSize * 2 / 3 <= y && y < object.y + spriteSize;
                } else {
                    slideUpRight = object.y < y + spriteSize && y + spriteSize <= object.y + spriteSize / 3;
                    slideDownRight = object.y + spriteSize * 2 / 3 <= y && y < object.y + spriteSize;
                }
                return true;
            } else {
                if (direction.equals("left")) {
                    slideUpLeft = false;
                    slideDownLeft = false;
                } else {
                    slideUpRight = false;
                    slideDownRight = false;
                }
            }
        }
        return false;
    }

    private boolean yCollision(String direction, int spriteSize, int characterWidth, Entity object) {
        boolean y_collide = ((y < object.y + spriteSize && object.y + spriteSize < y + spriteSize)
                || (y <= object.y && object.y < y + spriteSize)
                || (y >= object.y && y + spriteSize <= object.y + spriteSize));

        if (direction.equals("left")) {
            return (x - Math.ceil(speed) < object.x + spriteSize && object.x + spriteSize < x - Math.ceil(speed) + characterWidth)
                    && y_collide;
        } else {
            return (x + Math.ceil(speed) < object.x && object.x < x + Math.ceil(speed) + characterWidth)
                    && y_collide;
        }
    }

    private boolean verticalStillCollision(List<Entity> stillObjects, List<Bomb> bombs, String direction) {
        int spriteSize = Sprite.SCALED_SIZE;
        int characterWidth;
        if (this instanceof Bomber) {
            characterWidth = spriteSize - 8;
        } else {
            characterWidth = spriteSize;
        }
        int top = y / spriteSize - 2;
        int left = x / spriteSize;

        final int WIDTH = 31;
        final int HEIGHT = 13;

        if (!(this instanceof Bomber) || ((Bomber) this).notJustPlacedBomb()) {
            for (Bomb bomb : bombs) {
                if (xCollision(direction, spriteSize, characterWidth, bomb)) return true;
            }
        }

        for (int j = Math.max(left - 1, 0); j <= Math.min(left + 1, WIDTH - 1); j++) {
            Entity object;
            if (direction.equals("up")) {
                object = stillObjects.get(Math.max(top - 1, 0) * WIDTH + j);
            } else {
                object = stillObjects.get(Math.min(top + 1, HEIGHT - 1) * WIDTH + j);
            }

            if (!(object instanceof Wall) && (!(object instanceof Brick) || this instanceof Kondoria)) {
                continue;
            }

            if (xCollision(direction, spriteSize, characterWidth, object)) {
                if (direction.equals("up")) {
                    slideLeftTop = object.x < x + characterWidth && x + characterWidth <= object.x + spriteSize / 3;
                    slideRightTop = object.x + spriteSize * 2 / 3 <= x && x < object.x + spriteSize;
                } else {
                    slideLeftBottom = object.x < x + characterWidth && x + characterWidth <= object.x + spriteSize / 3;
                    slideRightBottom = object.x + spriteSize * 2 / 3 <= x && x < object.x + spriteSize;
                }
                return true;
            } else {
                if (direction.equals("up")) {
                    slideLeftTop = false;
                    slideRightTop = false;
                } else {
                    slideLeftBottom = false;
                    slideRightBottom = false;
                }
            }
        }
        return false;
    }

    private boolean xCollision(String direction, int spriteSize, int characterWidth, Entity object) {
        boolean x_collide = ((x < object.x + spriteSize && object.x + spriteSize < x + characterWidth)
                || (x <= object.x && object.x < x + characterWidth)
                || (x >= object.x && x + characterWidth <= object.x + spriteSize));

        if (direction.equals("up")) {
            return (y - Math.ceil(speed) < object.y + spriteSize && object.y + spriteSize < y - Math.ceil(speed) + spriteSize)
                    && x_collide;
        } else {
            return (y + Math.ceil(speed) < object.y && object.y < y + Math.ceil(speed) + spriteSize)
                    && x_collide;
        }
    }

    @Override
    public abstract void update();

    public void markAsDead() {
        currentFrame = 0;
        markedAsDead = true;
    }

    protected void updateDeadImg() {
        if (currentFrame < maxFrames * deadSheet.length)
            img = deadSheet[currentFrame++ / maxFrames];
    }

    public boolean isEnemyAndDead() {
        return this instanceof Enemy && dead;
    }

    public boolean isDead() {
        return dead;
    }

    public void collision(List<Entity> stillObjects, List<Bomb> bombs, List<GameCharacter> characters) {
        stillCollision(stillObjects, bombs);
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getSpeed() {
        return speed;
    }
}
