import java.util.ArrayList;
import java.util.Collections;

public class Sort2D {
    ArrayList<Pair> _Sort2D(ArrayList<Pair> arr) {
        int i = 0;
        //order by Second number
        while (i < arr.size() - 1) {
            if (arr.get(i).getY() > arr.get(i + 1).getY()) {
                Collections.swap(arr, i, i + 1);
                i = 0;
                //System.out.println("swap at: " + i);
            }
            i++;
        }
        i = 0;
        while (i < arr.size() - 1) {
            if (arr.get(i).getX() > arr.get(i + 1).getX()) {
                Collections.swap(arr, i, i + 1);
                i = 0;
                //System.out.println("swap at: " + i);
            }
            i++;
        }
        return arr;
    }

    ArrayList<Pair> _Sort2DInv(ArrayList<Pair> arr) {
        int i = 0;
        while (i < arr.size() - 1) {
            if (arr.get(i).getX() > arr.get(i + 1).getX()) {
                Collections.swap(arr, i, i + 1);
                i = 0;
                //System.out.println("swap at: " + i);
            }
            i++;
        }
        i = 0;
        while (i < arr.size() - 1) {
            if (arr.get(i).getY() > arr.get(i + 1).getY()) {
                Collections.swap(arr, i, i + 1);
                i = 0;
                //System.out.println("swap at: " + i);
            }
            i++;
        }
        return arr;
    }
}
