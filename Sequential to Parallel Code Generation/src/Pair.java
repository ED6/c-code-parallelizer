
public class Pair {
    int x;
    int y;

    Pair(int px, int py) {
        x = px;
        y = py;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return " ( " + getX() + " , " + getY() + " ) ";
    }
}
