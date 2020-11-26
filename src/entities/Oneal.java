package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Random;

public class Oneal extends ChaseEnemy {
    private int randomSpeedAfterFrames;
    private int randomSpeedFrameCount;

    public Oneal(int x, int y, Image img) {
        super(x, y, img);
        left = new Image[]{
                Sprite.oneal_left1.getFxImage(),
                Sprite.oneal_left2.getFxImage(),
                Sprite.oneal_left3.getFxImage()
        };
        right = new Image[]{
                Sprite.oneal_right1.getFxImage(),
                Sprite.oneal_right2.getFxImage(),
                Sprite.oneal_right3.getFxImage()
        };
        deadSheet[0] = Sprite.oneal_dead.getFxImage();
        speed = 1;
        randomSpeedAfterFrames = generateRandomSpeedFrame();
        randomSpeedFrameCount = 0;
    }

    @Override
    public void move(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber) {
        changeSpeedRandom();
        super.move(stillObjects, bombs, bomber);
    }

    private void changeSpeedRandom() {
        if (randomSpeedFrameCount != randomSpeedAfterFrames) {
            randomSpeedFrameCount++;
            return;
        }

        randomSpeedFrameCount = 0;
        randomSpeedAfterFrames = generateRandomSpeedFrame();
        switch (direction) {
            case "left" -> {
                if (isOdd(x)) x++;
            }
            case "right" -> {
                if (isOdd(x)) x--;
            }
            case "up" -> {
                if (isOdd(y)) y++;
            }
            case "down" -> {
                if (isOdd(y)) y--;
            }
        }
        speed = new Random().nextInt(2) + 1;
    }

    private boolean isOdd(int coordinate) {
        return coordinate % 2 == 1;
    }

    private int generateRandomSpeedFrame() {
        int lowerBound = speed == 2 ? 45 : 70;
        int upperBound = speed == 2 ? 90 : 150;
        return new Random().nextInt(upperBound) + lowerBound;
    }
}
