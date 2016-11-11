package numbershifter;

import com.rits.cloning.Cloner;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by lukas on 12.11.15.
 */
public class NumShifter implements Serializable, Cloneable {

    public class InvalidActionException extends Exception {};

    int x_width;

    int[] data;

    public int get(int y, int x) {
        return data[y*x_width + x];
    }

    public void shift(Direction d) throws InvalidActionException {
        int zeroPos = -1;
        for (int i = 0; i < data.length; i++) if (data[i] == 0) {zeroPos = i; break;}

        int x = zeroPos % x_width;
        int y = zeroPos / x_width;
        int y_width = data.length / x_width;
        int nx = x;
        int ny = y;

        switch (d) {
            case UP:
                if (y == 0) throw new InvalidActionException();
                ny--;
                break;
            case RIGHT:
                if (x == x_width-1) throw new InvalidActionException();
                nx++;
                break;
            case DOWN:
                if (y == y_width-1) throw new InvalidActionException();
                ny++;
                break;
            case LEFT:
                if (x == 0) throw new InvalidActionException();
                nx--;
                break;
        }

        data[y*x_width+x] = data[ny*x_width+nx];
        data[ny*x_width+nx] = 0;
    }

    public boolean isSolved() {
        for (int i = 0; i < data.length; i++)
            if (i != data[i]) return false;
        return true;
    }

    public NumShifter(int size) {
        this.data = new int[size];
        for (int i = 0; i < size; i++)
            this.data[i] = i;
        this.x_width = (int) Math.sqrt(size);
    }

    public NumShifter(int[] _data) {
        this(_data.length);
        this.data = _data;
    }

    public NumShifter(int[] _data, int _x_width) {
        this.data = _data;
        this.x_width = _x_width;

    }
    public static NumShifter random(int size, int shifts) {
        NumShifter rand = new NumShifter(size);

        Direction lastShift = null;
        while (shifts > 0) {
            try {
                Direction d = Direction.getRandom();
                if (d == lastShift) continue;
                rand.shift(d);
                System.out.println(d);
                lastShift = d.opposite();
                shifts--;
            } catch (InvalidActionException e) {}
        }

        return rand;
    }
    public static NumShifter random(int size) {
        return random(size, 400);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int y = 0; y < data.length/x_width; y++) {
            for (int x = 0; x < x_width; x++) {
                if (data[y*x_width+x] != 0)
                    s.append(String.format("%2d ", get(y, x)));
                else
                    s.append("   ");
            }
            s.append("\n");
        }
        return s.toString();
    }

    public NumShifter clone() {
        Cloner c = new Cloner();
        return c.deepClone(this);
    }
}
