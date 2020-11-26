package entities;

import graphics.Sprite;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class Enemy extends GameCharacter {
    protected List<String> directions;

    public Enemy(int x, int y, Image img) {
        super(x, y, img);
        maxFrames = 15;
        currentFrame = 0;
        moving = true;
        directions = new ArrayList<>(Arrays.asList("up, down, left, right".split(", ")));
        direction = directions.get(new Random().nextInt(4));
        deadSheet = new Image[]{
                null,
                Sprite.mob_dead1.getFxImage(),
                Sprite.mob_dead2.getFxImage(),
                Sprite.mob_dead3.getFxImage()
        };
    }

    @Override
    public void update() {
        if (!markedAsDead) {
            if (currentFrame >= maxFrames * 3) {
                currentFrame = 0;
            }
        } else {
            if (currentFrame >= maxFrames * 4) {
                dead = true;
                currentFrame = 0;
            }
        }

        if (markedAsDead) {
            updateDeadImg();
        } else {
            switch (direction) {
                case "left" -> {
                    x += velocityX;
                    img = left[currentFrame++ / maxFrames];
                }
                case "right" -> {
                    x += velocityX;
                    img = right[currentFrame++ / maxFrames];
                }
                case "up" -> {
                    img = right[currentFrame++ / maxFrames];
                    y += velocityY;
                }
                case "down" -> {
                    img = left[currentFrame++ / maxFrames];
                    y += velocityY;
                }
            }
        }
    }

    protected List<String> findPossibleDirection() {
        List<String> possibleDirections = new ArrayList<>(Arrays.asList("up, down, left, right".split(", ")));
        if (collideLeft) {
            possibleDirections.remove("left");
        }
        if (collideRight) {
            possibleDirections.remove("right");
        }
        if (collideTop) {
            possibleDirections.remove("up");
        }
        if (collideBottom) {
            possibleDirections.remove("down");
        }
        return possibleDirections;
    }

    protected void changeRandomDirection(String oppositeDirection) {
        if (!directions.contains(direction)) {
            direction = directions.get(new Random().nextInt(directions.size()));
        } else {
            directions.remove(oppositeDirection);
            if (direction.equals("left") || direction.equals("right")) {
                if (directions.contains("up") || directions.contains("down")) {
                    direction = directions.get(new Random().nextInt(directions.size()));
                }
            } else {
                if (directions.contains("left") || directions.contains("right")) {
                    direction = directions.get(new Random().nextInt(directions.size()));
                }
            }
        }
    }

    protected boolean cantMoveRandom() {
        directions = findPossibleDirection();

        if (directions.size() == 0) {
            velocityY = 0;
            velocityX = 0;
            return true;
        }

        switch (direction) {
            case "left" -> changeRandomDirection("right");
            case "right" -> changeRandomDirection("left");
            case "up" -> changeRandomDirection("down");
            case "down" -> changeRandomDirection("up");
        }
        return false;
    }

    public void autoMove() {
        switch (direction) {
            case "left" -> {
                velocityX = -speed;
                velocityY = 0;
            }
            case "right" -> {
                velocityX = speed;
                velocityY = 0;
            }
            case "up" -> {
                velocityY = -speed;
                velocityX = 0;
            }
            case "down" -> {
                velocityY = speed;
                velocityX = 0;
            }
        }
    }

    public abstract void move(List<Entity> stillObjects, List<Bomb> bombs, Bomber bomber);
}
