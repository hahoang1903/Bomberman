package entities;

import graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.List;

public class Flame extends Entity {
    private final String direction;
    private final int length;
    private int lengthReduce;

    private final int maxFrames;
    private int currentFrame;

    private Image flamePart;
    private final Image[] flameLastSheet;
    private final Image[] flameSheet;

    private final Bomb owner;

    public Flame(int x, int y, Image img, int length, String direction, Bomb owner) {
        super(x, y, img);
        this.owner = owner;
        this.length = length;
        lengthReduce = length;
        maxFrames = 5;
        currentFrame = 0;

        this.direction = direction;
        flameLastSheet = switch (direction) {
            case "left" -> new Image[]{
                    Sprite.explosion_horizontal_left_last.getFxImage(),
                    Sprite.explosion_horizontal_left_last1.getFxImage(),
                    Sprite.explosion_horizontal_left_last2.getFxImage(),
                    Sprite.explosion_horizontal_left_last2.getFxImage(),
                    Sprite.explosion_horizontal_left_last1.getFxImage(),
                    Sprite.explosion_horizontal_left_last.getFxImage()
            };
            case "right" -> new Image[]{
                    Sprite.explosion_horizontal_right_last.getFxImage(),
                    Sprite.explosion_horizontal_right_last1.getFxImage(),
                    Sprite.explosion_horizontal_right_last2.getFxImage(),
                    Sprite.explosion_horizontal_right_last2.getFxImage(),
                    Sprite.explosion_horizontal_right_last1.getFxImage(),
                    Sprite.explosion_horizontal_right_last.getFxImage()
            };
            case "up" -> new Image[]{
                    Sprite.explosion_vertical_top_last.getFxImage(),
                    Sprite.explosion_vertical_top_last1.getFxImage(),
                    Sprite.explosion_vertical_top_last2.getFxImage(),
                    Sprite.explosion_vertical_top_last2.getFxImage(),
                    Sprite.explosion_vertical_top_last1.getFxImage(),
                    Sprite.explosion_vertical_top_last.getFxImage()
            };
            case "down" -> new Image[]{
                    Sprite.explosion_vertical_down_last.getFxImage(),
                    Sprite.explosion_vertical_down_last1.getFxImage(),
                    Sprite.explosion_vertical_down_last2.getFxImage(),
                    Sprite.explosion_vertical_down_last2.getFxImage(),
                    Sprite.explosion_vertical_down_last1.getFxImage(),
                    Sprite.explosion_vertical_down_last.getFxImage()
            };
            default -> null;
        };

        flameSheet = switch (direction) {
            case "left", "right" -> new Image[]{
                    Sprite.explosion_horizontal.getFxImage(),
                    Sprite.explosion_horizontal1.getFxImage(),
                    Sprite.explosion_horizontal2.getFxImage(),
                    Sprite.explosion_horizontal2.getFxImage(),
                    Sprite.explosion_horizontal1.getFxImage(),
                    Sprite.explosion_horizontal.getFxImage()
            };
            case "up", "down" -> new Image[]{
                    Sprite.explosion_vertical.getFxImage(),
                    Sprite.explosion_vertical1.getFxImage(),
                    Sprite.explosion_vertical2.getFxImage(),
                    Sprite.explosion_vertical2.getFxImage(),
                    Sprite.explosion_vertical1.getFxImage(),
                    Sprite.explosion_vertical1.getFxImage()
            };
            default -> null;
        };
        flamePart = null;
    }

    @Override
    public void update() {
        if (lengthReduce == length) {
            currentFrame++;
            return;
        }

        if (currentFrame < maxFrames * 6 - 1) {
            img = flameLastSheet[currentFrame / maxFrames];
            flamePart = flameSheet[currentFrame / maxFrames];
            currentFrame++;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (lengthReduce == length) return;

        if (lengthReduce == 0) {
            super.render(gc);
        } else {
            lengthReduce--;
        }
        int xPos = 0;
        int yPos = 0;

        for (int i = 1; i < length - lengthReduce; i++) {
            switch (direction) {
                case "left" -> {
                    xPos = x + (length - i) * Sprite.SCALED_SIZE;
                    yPos = y;
                }
                case "right" -> {
                    xPos = x - (length - i) * Sprite.SCALED_SIZE;
                    yPos = y;
                }
                case "up" -> {
                    xPos = x;
                    yPos = y + (length - i) * Sprite.SCALED_SIZE;
                }
                case "down" -> {
                    xPos = x;
                    yPos = y - (length - i) * Sprite.SCALED_SIZE;
                }
            }
            gc.drawImage(flamePart, xPos, yPos);
        }
    }

    public void collision(List<Entity> stillObjects, List<Bomb> bombs, List<GameCharacter> characters, int heightDiff, int WIDTH) {
        int left = x / Sprite.SCALED_SIZE;
        int top = y / Sprite.SCALED_SIZE - heightDiff;
        switch (direction) {
            case "left" -> leftUpCollision(stillObjects, bombs, left, top, heightDiff, WIDTH, "left");
            case "right" -> rightDownCollision(stillObjects, bombs, left, top, heightDiff, WIDTH, "right");
            case "up" -> leftUpCollision(stillObjects, bombs, top, left, heightDiff, WIDTH, "up");
            case "down" -> rightDownCollision(stillObjects, bombs, top, left, heightDiff, WIDTH, "down");
        }

        if (!owner.isExploding())
            return;

        for (GameCharacter character : characters) {
            if (character.markedAsDead)
                continue;

            int characterWidth;
            if (character instanceof Bomber) {
                characterWidth = Sprite.SCALED_SIZE - 8;
            } else {
                characterWidth = Sprite.SCALED_SIZE;
            }
            switch (direction) {
                case "left" -> {
                    if (character.x + characterWidth / 2 > x + lengthReduce * Sprite.SCALED_SIZE
                            && character.x < x + (length + 1) * Sprite.SCALED_SIZE
                            && character.y + Sprite.SCALED_SIZE / 2 > y
                            && character.y + Sprite.SCALED_SIZE / 2 < y + Sprite.SCALED_SIZE)
                        character.markAsDead();
                }
                case "right" -> {
                    if (character.x + characterWidth > x - length * Sprite.SCALED_SIZE
                            && character.x + characterWidth / 2 < x + (1 - lengthReduce) * Sprite.SCALED_SIZE
                            && character.y + Sprite.SCALED_SIZE / 2 > y
                            && character.y + Sprite.SCALED_SIZE / 2 < y + Sprite.SCALED_SIZE)
                        character.markAsDead();
                }
                case "up" -> {
                    if (character.y + Sprite.SCALED_SIZE / 2 > y + lengthReduce * Sprite.SCALED_SIZE
                            && character.y < y + length * Sprite.SCALED_SIZE
                            && character.x + characterWidth / 2 > x
                            && character.x + characterWidth / 2 < x + Sprite.SCALED_SIZE)
                        character.markAsDead();
                }
                case "down" -> {
                    if (character.y + Sprite.SCALED_SIZE > y - length * Sprite.SCALED_SIZE
                            && character.y + Sprite.SCALED_SIZE / 2 < y + (1 - lengthReduce) * Sprite.SCALED_SIZE
                            && character.x + characterWidth / 2 > x
                            && character.x + characterWidth / 2 < x + Sprite.SCALED_SIZE)
                        character.markAsDead();
                }
            }
        }

    }

    private void leftUpCollision(List<Entity> stillObjects, List<Bomb> bombs, int position1, int position2, int heightDiff, int WIDTH, String direction) {
        lengthReduce = length;
        for (int i = position1 + length - 1; i >= position1; i--) {
            Entity object;
            if (direction.equals("left")) {
                object = stillObjects.get(position2 * WIDTH + i);
            } else {
                object = stillObjects.get(i * WIDTH + position2);
            }

            if (object instanceof Wall) {
                break;
            }
            if (object instanceof Brick) {
                if (currentFrame >= maxFrames * 3) {
                    object.update();
                }
                if (((Brick) object).isDestroyable() || currentFrame >= maxFrames * 6 - 1) {
                    ((Brick) object).destroy(stillObjects, heightDiff, WIDTH);
                }
                break;
            }
            if (owner.isExploding()) {
                for (Bomb bomb : bombs) {
                    if (bomb.x == object.x && bomb.y == object.y && !bomb.isExploding())
                        bomb.boom();
                }
            }

            lengthReduce--;
        }
    }

    private void rightDownCollision(List<Entity> stillObjects, List<Bomb> bombs, int position1, int position2, int heightDiff, int WIDTH, String direction) {
        lengthReduce = length;
        for (int i = position1 - length + 1; i <= position1; i++) {
            Entity object;
            if (direction.equals("right")) {
                object = stillObjects.get(position2 * WIDTH + i);
            } else {
                object = stillObjects.get(i * WIDTH + position2);
            }

            if (object instanceof Wall) {
                break;
            }
            if (object instanceof Brick) {
                if (currentFrame >= maxFrames * 3) {
                    object.update();
                }
                if (((Brick) object).isDestroyable() || currentFrame >= maxFrames * 6 - 1) {
                    ((Brick) object).destroy(stillObjects, heightDiff, WIDTH);
                }
                break;
            }

            if (owner.isExploding()) {
                for (Bomb bomb : bombs) {
                    if (bomb.x == object.x && bomb.y == object.y && !bomb.isExploding())
                        bomb.boom();
                }
            }

            lengthReduce--;
        }
    }
}
