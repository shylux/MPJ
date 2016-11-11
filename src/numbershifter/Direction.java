package numbershifter;

import java.util.List;

/**
 * Created by lukas on 18.11.15.
 */
public enum Direction {
    UP, RIGHT, DOWN, LEFT;

    public static Direction getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
    public Direction opposite() {
        switch (this) {
            case UP:
                return DOWN;
            case RIGHT:
                return LEFT;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            default:
                throw new Error();
        }
    }
}
