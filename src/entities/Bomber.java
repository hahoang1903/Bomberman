package entities;

import graphics.Sprite;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.List;

public class Bomber extends GameCharacter {
    private int maxBombs;
    private int availableBombs;
    private int flameLength;
    private int lives;
    private boolean justPlacedBomb;
    private boolean standingOnPortal;
    private double speedReducer;
    private String previousDirection;
    private boolean takePowerUp;

    private MediaPlayer playerDieSFX;

    public Bomber(int x, int y, Image img, int lives, double speed, int maxBombs, int flameLength) {
        super(x, y, img);
        left = new Image[]{
                Sprite.player_left.getFxImage(),
                Sprite.player_left_1.getFxImage(),
                Sprite.player_left.getFxImage(),
                Sprite.player_left_2.getFxImage()
        };
        right = new Image[]{
                Sprite.player_right.getFxImage(),
                Sprite.player_right_1.getFxImage(),
                Sprite.player_right.getFxImage(),
                Sprite.player_right_2.getFxImage()
        };
        up = new Image[]{
                Sprite.player_up.getFxImage(),
                Sprite.player_up_1.getFxImage(),
                Sprite.player_up.getFxImage(),
                Sprite.player_up_2.getFxImage()
        };
        down = new Image[]{
                Sprite.player_down.getFxImage(),
                Sprite.player_down_1.getFxImage(),
                Sprite.player_down.getFxImage(),
                Sprite.player_down_2.getFxImage()
        };
        deadSheet = new Image[]{
                Sprite.player_dead1.getFxImage(),
                Sprite.player_dead2.getFxImage(),
                Sprite.player_dead3.getFxImage()
        };
        direction = "right";
        previousDirection = "none";
        moving = false;
        maxFrames = 8;
        currentFrame = 0;
        this.lives = lives;
        this.speed = speed;
        this.flameLength = flameLength;
        this.maxBombs = maxBombs;
        availableBombs = maxBombs;
        justPlacedBomb = false;
        speedReducer = 0;
        standingOnPortal = false;
        takePowerUp = false;
    }

    @Override
    public void update() {
        if (!markedAsDead) {
            if (currentFrame >= maxFrames * 4) {
                currentFrame = 0;
            }
        } else {
            if (playerDieSFX == null) {
                playerDieSFX = new MediaPlayer(new Media(new File("src/sounds/SFX5.mp3").toURI().toString()));
                playerDieSFX.setVolume(Entity.SFX);
                playerDieSFX.play();
            }
            if (currentFrame >= maxFrames * 3) {
                if (lives >= 0 && !dead)
                    lives--;
                playerDieSFX = null;
                dead = true;
            }
        }

        if (markedAsDead) {
            updateDeadImg();
        } else {
            if (!previousDirection.equals(direction))
                speedReducer = 0;
            previousDirection = direction;

            switch (direction) {
                case "left":
                    if (moving) {
                        if (currentFrame % 9 == 0 && currentFrame != 0) {
                            MediaPlayer mp = new MediaPlayer(new Media(new File("src/sounds/SFX1.mp3").toURI().toString()));
                            mp.setVolume(Entity.SFX);
                            mp.play();
                        }
                        speedReducer += velocityX;
                        x += Math.floor(speedReducer);
                        speedReducer -= Math.floor(speedReducer);
                        img = left[currentFrame++ / maxFrames];
                    } else {
                        img = Sprite.player_left.getFxImage();
                    }
                    break;
                case "right":
                    if (moving) {
                        if (currentFrame % 9 == 0 && currentFrame != 0) {
                            MediaPlayer mp = new MediaPlayer(new Media(new File("src/sounds/SFX1.mp3").toURI().toString()));
                            mp.setVolume(Entity.SFX);
                            mp.play();
                        }
                        speedReducer += velocityX;
                        x += Math.floor(speedReducer);
                        speedReducer -= Math.floor(speedReducer);
                        img = right[currentFrame++ / maxFrames];
                    } else {
                        img = Sprite.player_right.getFxImage();
                    }
                    break;
                case "up":
                    if (moving) {
                        if (currentFrame % 9 == 0 && currentFrame != 0) {
                            MediaPlayer mp = new MediaPlayer(new Media(new File("src/sounds/SFX2.mp3").toURI().toString()));
                            mp.setVolume(Entity.SFX);
                            mp.play();
                        }
                        speedReducer += velocityY;
                        y += Math.floor(speedReducer);
                        speedReducer -= Math.floor(speedReducer);
                        img = up[currentFrame++ / maxFrames];
                    } else {
                        img = Sprite.player_up.getFxImage();
                    }
                    break;
                case "down":
                    if (moving) {
                        if (currentFrame % 9 == 0 && currentFrame != 0) {
                            MediaPlayer mp = new MediaPlayer(new Media(new File("src/sounds/SFX2.mp3").toURI().toString()));
                            mp.setVolume(Entity.SFX);
                            mp.play();
                        }
                        speedReducer += velocityY;
                        y += Math.floor(speedReducer);
                        speedReducer -= Math.floor(speedReducer);
                        img = down[currentFrame++ / maxFrames];
                    } else {
                        img = Sprite.player_down.getFxImage();
                    }
                    break;
            }
        }
    }

    public Bomb placeBomb(List<Bomb> bombs) {
        if (availableBombs == 0 || markedAsDead) return null;

        int i = (y + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;
        int j = (x + Sprite.SCALED_SIZE / 2) / Sprite.SCALED_SIZE;

        for (Bomb bomb : bombs) {
            if (i * Sprite.SCALED_SIZE == bomb.y && j * Sprite.SCALED_SIZE == bomb.x)
                return null;
        }

        justPlacedBomb = true;
        availableBombs--;
        MediaPlayer mp = new MediaPlayer(new Media(new File("src/sounds/SFX3.mp3").toURI().toString()));
        mp.setVolume(Entity.SFX);
        mp.play();
        return new Bomb(j, i, Sprite.bomb.getFxImage(), this);
    }

    public void collision(List<Entity> stillObjects, List<Bomb> bombs, List<GameCharacter> characters) {
        collision(stillObjects, bombs, characters, 2, 31);
    }

    public void collision(List<Entity> stillObjects, List<Bomb> bombs, List<GameCharacter> characters, int heightDiff, int WIDTH) {
        if (justPlacedBomb) {
            int size = Sprite.SCALED_SIZE;
            justPlacedBomb = false;
            for (Bomb bomb : bombs) {
                if (bomb.x / size == (x + size / 2) / size && bomb.y / size == (y + size / 2) / size) {
                    justPlacedBomb = true;
                    break;
                }
            }
        }
        stillCollision(stillObjects, bombs);
        specialObjectCollision(stillObjects, heightDiff, WIDTH);
        enemyCollision(characters);
    }

    public void specialObjectCollision(List<Entity> stillObjects, int heightDiff, int WIDTH) {
        int i, j;
        if (direction.equals("left") || direction.equals("up")) {
            i = (y + Sprite.SCALED_SIZE / 3) / Sprite.SCALED_SIZE - heightDiff;
            j = (x + Sprite.SCALED_SIZE / 3) / Sprite.SCALED_SIZE;
        } else {
            i = (y + 2 * (Sprite.SCALED_SIZE / 3)) / Sprite.SCALED_SIZE - heightDiff;
            j = (x + 2 * (Sprite.SCALED_SIZE / 3)) / Sprite.SCALED_SIZE;
        }

        Entity object = stillObjects.get(i * WIDTH + j);
        if (object instanceof PowerUp) {
            MediaPlayer mp = new MediaPlayer(new Media(new File("src/sounds/SFX4.mp3").toURI().toString()));
            mp.setVolume(Entity.SFX);
            mp.play();
            ((PowerUp) object).grantEffect(this);
            ((PowerUp) stillObjects.get(i * WIDTH + j)).destroy(stillObjects, heightDiff, WIDTH);
            takePowerUp = true;
        }
        standingOnPortal = object instanceof Portal;
    }

    private void enemyCollision(List<GameCharacter> characters) {
        if (markedAsDead) return;

        int playerWidth = Sprite.SCALED_SIZE - 8;
        for (GameCharacter character : characters) {
            if (!(character instanceof Enemy) || character.markedAsDead) continue;

            if (x + playerWidth > character.x && x < character.x + Sprite.SCALED_SIZE
                    && y < character.y + Sprite.SCALED_SIZE && y + Sprite.SCALED_SIZE > character.y) {
                markAsDead();
                break;
            }
        }
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public int getLives() {
        return lives;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public int getFlameLength() {
        return flameLength;
    }

    public boolean isStandingOnPortal() {
        return standingOnPortal;
    }

    public boolean isCollideLeft() {
        return collideLeft;
    }

    public boolean isCollideRight() {
        return collideRight;
    }

    public boolean isCollideTop() {
        return collideTop;
    }

    public boolean isCollideBottom() {
        return collideBottom;
    }

    public boolean canSlideUpLeft() {
        return slideUpLeft;
    }

    public boolean canSlideUpRight() {
        return slideUpRight;
    }

    public boolean canSlideDownLeft() {
        return slideDownLeft;
    }

    public boolean canSlideDownRight() {
        return slideDownRight;
    }

    public boolean canSlideLeftTop() {
        return slideLeftTop;
    }

    public boolean canSlideLeftBottom() {
        return slideLeftBottom;
    }

    public boolean canSlideRightTop() {
        return slideRightTop;
    }

    public boolean canSlideRightBottom() {
        return slideRightBottom;
    }

    public void slideUp() {
        y--;
    }

    public void slideDown() {
        y++;
    }

    public void slideLeft() {
        x--;
    }

    public void slideRight() {
        x++;
    }

    public void increaseMaxBomb() {
        maxBombs++;
        increaseAvailableBomb();
    }

    public void increaseFlameLength() {
        flameLength++;
    }

    public void increaseSpeed() {
        speed += 0.5;
    }

    public void increaseAvailableBomb() {
        availableBombs++;
    }

    public boolean notJustPlacedBomb() {
        return !justPlacedBomb;
    }

    public boolean hasAvailableBombs() {
        return availableBombs > 0;
    }

    public boolean isTakePowerUp() {
        return takePowerUp;
    }

    public void setTakePowerUp(boolean takePowerUp) {
        this.takePowerUp = takePowerUp;
    }
}
