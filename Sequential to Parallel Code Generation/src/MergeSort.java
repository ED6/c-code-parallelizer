import org.omg.PortableInterceptor.INACTIVE;

import java.util.ArrayList;

public class MergeSort {
    public static ArrayList<Integer> MergeSort(ArrayList<Integer> unsorted) {


        if (unsorted.size() <= 1) {
            return unsorted;
        }

        // Recursive case. First, divide the list into equal-sized sublists
        // consisting of the even and odd-indexed elements.
        ArrayList<Integer> left = new ArrayList<>();
        ArrayList<Integer> right = new ArrayList<>();

        for (int i = 0; i < unsorted.size(); i++) {
            if (i % 2 == 1) {
                left.add(unsorted.get(i));
            } else {
                right.add(unsorted.get(i));
            }
        }

        // Recursively sort both sublists.
        left = MergeSort(left);
        right = MergeSort(right);

        // Then merge the now-sorted sublists.
        return merge(left, right);

    }

    static ArrayList<Integer> merge(ArrayList<Integer> left, ArrayList<Integer> right) {
        ArrayList<Integer> result = new ArrayList<>();

        while (left.size() != 0 && right.size() != 0) {
            if (left.get(0) <= right.get(0)) {
                result.add(left.get(0));
                left.remove(0);
            } else {
                result.add(right.get(0));
                right.remove(0);
            }
        }
        //get rid of residue numbers
        while (left.size() != 0) {
            result.add(left.get(0));
            left.remove(0);
        }
        while (right.size() != 0) {
            result.add(right.get(0));
            right.remove(0);
        }
        return result;
    }

    public static ArrayList<ArrayList<Integer>> FormatFix(ArrayList<Integer> unformated) {
        ArrayList<ArrayList<Integer>> formated = new ArrayList<ArrayList<Integer>>();
        for (Integer i : unformated) {
            ArrayList<Integer> Column = new ArrayList<>();
            Column.add(i);
            formated.add(Column);
        }
        return formated;
    }

    public static ArrayList<ArrayList<Integer>> addParent(ArrayList<ArrayList<Integer>> threadExecutionTable, int currentIndex) {
        int i = 0;
        while (i < threadExecutionTable.size() && threadExecutionTable.get(i).get(0) < currentIndex) {
            i++;
        }
        ArrayList<Integer> Column = new ArrayList<>();
        Column.add(currentIndex);
        threadExecutionTable.add(i, Column);
        return threadExecutionTable;
    }


}
