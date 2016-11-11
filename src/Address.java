/**
 * Created by lukas on 29.10.15.
 */
public class Address {
    int kantenl;
    int mpiNr;
    int x;
    int y;

    public Address(int me, int size) {
        mpiNr = me;
        kantenl = new Double(Math.log(size) / Math.log(2)).intValue();
        x = me % kantenl;
        y = me / kantenl;
    }
    private Address createAddress(int x, int y) throws CloneNotSupportedException {
        Address other = (Address) this.clone();
        other.x = x;
        other.y = y;
        return other;
    }

    public boolean equals(Object o) {
        if (o instanceof Address == false) return false;
        Address other = (Address)o;
        return (x == other.x && y == other.y);
    }
    public String toString() {
        return String.format("<x: %d, y: %d>", x, y);
    }
}
