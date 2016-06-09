
/**
 * Class that takes pairs of independent relations and translates it into data type with all possible parallelizations
 */

import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;


public class AccessibilityChecker {

    public ArrayList<ArrayList<Integer>> getExecutionTable() {
        ArrayList<Pair> IndepPairs = new ArrayList<Pair>();
        int size = 0;
        //read pairs from .txt file (output.txt for now)
        try {
            int num1;
            int num2;
            Scanner sc = new Scanner(new File("output.txt/merged_file"));
            size = sc.nextInt();
            //System.out.println(size);
            //read 2 ints order them and add them

            while (sc.hasNext()) {
                String str = sc.next();
                str = str.substring(1, str.length() - 1);
                String[] parts = str.split(",");
                num1 = Integer.parseInt(parts[0]);
                num2 = Integer.parseInt(parts[1]);
                if (num1 > num2) {
                    //avoiding a temp might sound nice in theory
                    num1 += num2;
                    num2 = num1 - num2;
                    num1 -= num2;
                    //not sure in practice tho
                }
                IndepPairs.add(new Pair(num1, num2));
            }
        } catch (Exception e) {
            System.out.println("Error reading file in Thread Management");
            System.exit(1);
        }
//        System.out.println(IndepPairs);


        //order by 1st num, then second column
        Sort2D sorter = new Sort2D();
        IndepPairs = sorter._Sort2D(IndepPairs);

//        for (Pair p : IndepPairs){
//            System.out.println(p);
//        }

        ArrayList<ArrayList<Integer>> left2right = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> temp = new ArrayList<Integer>();
        int i = 0;
        int sect = 1;
        while (sect <= size) {
            while (i < IndepPairs.size() && IndepPairs.get(i).getX() == sect) {
                temp.add(IndepPairs.get(i).getY());
                //System.out.println("Helo:"+IndepPairs.get(i).getX());
                i++;
            }
            left2right.add(temp);
            temp = new ArrayList<Integer>();
            sect++;
//            System.out.println("New sect" + sect);
        }

//        for (ArrayList<Integer> a : left2right){
//            System.out.println(a);
//        }

        //Iterate though list to check backward accessibility
        int sizeA = left2right.size();
        int sizeB;

        for (int k = 0; k < sizeA; k++) {
            sizeB = left2right.get(k).size();
            for (int j = 0; j < sizeB; j++) {
                if (left2right.get(k).get(j) == k + 2 || left2right.get(k + 1).contains(left2right.get(k).get(j))) {
                    //successful
//                    System.out.println("passed "+ left2right.get(k).get(j));
                } else {
//                    System.out.println("removing "+left2right.get(k).get(j));
                    int tempIndex = k - 1;
                    while (tempIndex >= 0) {
                        left2right.get(tempIndex).remove(left2right.get(k).get(j));
                        tempIndex--;
                    }
                    left2right.get(k).remove(left2right.get(k).get(j));
                    j--;
                    sizeB--;
                }
            }
//            System.out.println("done section:" + k);
        }


        ArrayList<ArrayList<Integer>> right2left = new ArrayList<ArrayList<Integer>>();
        temp = new ArrayList<Integer>();
        i = 0;
        sect = 1;
        IndepPairs = sorter._Sort2DInv(IndepPairs);     //reorder array to fit dimension change

        while (sect <= size) {
            while (i < IndepPairs.size() && IndepPairs.get(i).getY() == sect) {
                temp.add(IndepPairs.get(i).getX());
                //System.out.println("Helo:"+IndepPairs.get(i).getX());
                i++;
            }
            right2left.add(temp);
            temp = new ArrayList<Integer>();
            sect++;
//            System.out.println("New sect" + sect);
        }

        //Iterate backward to check throw forward threads
        sizeA = right2left.size();
        for (int k = sizeA - 1; k >= 0; k--) {
            sizeB = right2left.get(k).size();
            for (int j = 0; j < sizeB; j++) {
                if (right2left.get(k).get(j) == k || right2left.get(k - 1).contains(right2left.get(k).get(j))) {
                    //successful
//                    System.out.println("passed "+ right2left.get(k).get(j));
                } else {
//                    System.out.println("removing "+right2left.get(k).get(j));
                    int tempIndex = k + 1;
                    while (tempIndex < sizeA) {
                        right2left.get(tempIndex).remove(right2left.get(k).get(j));
                        tempIndex++;
                    }
                    right2left.get(k).remove(right2left.get(k).get(j));
                    j--;
                    sizeB--;
                }
            }
//            System.out.println("done section:" + k);
        }

        //If ConcurrentModificationException wasnt a thing, then this would be nice
//        for (ArrayList<Integer> a : left2right){
//            for (int num : a){
//                if (num == index + 1 || left2right.get(index+1).contains(num)){
//                    //successfull check until now, leave be
//                }else{
//                    a.remove(num);
//                    int tempIndex = index-1;
//                    while (tempIndex>0){
//                        left2right.get(tempIndex).remove(num);
//                        tempIndex--;
//                    }
//                }
//            }
//            index++;
//        }

        //Now get union of left2right and right2left to see full possibility map
        ArrayList<ArrayList<Integer>> union = new ArrayList<ArrayList<Integer>>(right2left);
        int index = 0;
        for (ArrayList<Integer> a : left2right) {
            union.get(index).addAll(a);
            index++;
        }

//        System.out.println();
//        for (ArrayList<Integer> a : union){
//            System.out.println(a);
//        }
//        System.out.println();

        ThreadAssignment ta = new ThreadAssignment();
        return ta.ThreadDistribute(union); //returns Execution Table
    }

    public static void main(String args[]) {
        ArrayList<Pair> IndepPairs = new ArrayList<Pair>();
        int size = 0;
        //read pairs from .txt file (output.txt for now)
        try {
            int num1;
            int num2;
            Scanner sc = new Scanner(new File("output.txt/merged_file"));
            size = sc.nextInt();
            //System.out.println(size);
            //read 2 ints order them and add them

            while (sc.hasNext()) {
                String str = sc.next();
                str = str.substring(1, str.length() - 1);
                String[] parts = str.split(",");
                num1 = Integer.parseInt(parts[0]);
                num2 = Integer.parseInt(parts[1]);
                if (num1 > num2) {
                    //avoiding a temp might sound nice in theory
                    num1 += num2;
                    num2 = num1 - num2;
                    num1 -= num2;
                    //not sure in practice tho
                }
                IndepPairs.add(new Pair(num1, num2));
            }
        } catch (Exception e) {
            System.out.println("Error reading file in Thread Management");
            System.exit(1);
        }
//        System.out.println(IndepPairs);


        //order by 1st num, then second column
        Sort2D sorter = new Sort2D();
        IndepPairs = sorter._Sort2D(IndepPairs);

//        for (Pair p : IndepPairs){
//            System.out.println(p);
//        }

        ArrayList<ArrayList<Integer>> left2right = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> temp = new ArrayList<Integer>();
        int i = 0;
        int sect = 1;
        while (sect <= size) {
            while (i < IndepPairs.size() && IndepPairs.get(i).getX() == sect) {
                temp.add(IndepPairs.get(i).getY());
                //System.out.println("Helo:"+IndepPairs.get(i).getX());
                i++;
            }
            left2right.add(temp);
            temp = new ArrayList<Integer>();
            sect++;
//            System.out.println("New sect" + sect);
        }

//        for (ArrayList<Integer> a : left2right){
//            System.out.println(a);
//        }

        //Iterate though list to check backward accessibility
        int sizeA = left2right.size();
        int sizeB;

        for (int k = 0; k < sizeA; k++) {
            sizeB = left2right.get(k).size();
            for (int j = 0; j < sizeB; j++) {
                if (left2right.get(k).get(j) == k + 2 || left2right.get(k + 1).contains(left2right.get(k).get(j))) {
                    //successful
//                    System.out.println("passed "+ left2right.get(k).get(j));
                } else {
//                    System.out.println("removing "+left2right.get(k).get(j));
                    int tempIndex = k - 1;
                    while (tempIndex >= 0) {
                        left2right.get(tempIndex).remove(left2right.get(k).get(j));
                        tempIndex--;
                    }
                    left2right.get(k).remove(left2right.get(k).get(j));
                    j--;
                    sizeB--;
                }
            }
//            System.out.println("done section:" + k);
        }


        ArrayList<ArrayList<Integer>> right2left = new ArrayList<ArrayList<Integer>>();
        temp = new ArrayList<Integer>();
        i = 0;
        sect = 1;
        IndepPairs = sorter._Sort2DInv(IndepPairs);     //reorder array to fit dimension change

        while (sect <= size) {
            while (i < IndepPairs.size() && IndepPairs.get(i).getY() == sect) {
                temp.add(IndepPairs.get(i).getX());
                //System.out.println("Helo:"+IndepPairs.get(i).getX());
                i++;
            }
            right2left.add(temp);
            temp = new ArrayList<Integer>();
            sect++;
//            System.out.println("New sect" + sect);
        }

        //Iterate backward to check throw forward threads
        sizeA = right2left.size();
        for (int k = sizeA - 1; k >= 0; k--) {
            sizeB = right2left.get(k).size();
            for (int j = 0; j < sizeB; j++) {
                if (right2left.get(k).get(j) == k || right2left.get(k - 1).contains(right2left.get(k).get(j))) {
                    //successful
//                    System.out.println("passed "+ right2left.get(k).get(j));
                } else {
//                    System.out.println("removing "+right2left.get(k).get(j));
                    int tempIndex = k + 1;
                    while (tempIndex < sizeA) {
                        right2left.get(tempIndex).remove(right2left.get(k).get(j));
                        tempIndex++;
                    }
                    right2left.get(k).remove(right2left.get(k).get(j));
                    j--;
                    sizeB--;
                }
            }
//            System.out.println("done section:" + k);
        }

        //If ConcurrentModificationException wasnt a thing, then this would be nice
//        for (ArrayList<Integer> a : left2right){
//            for (int num : a){
//                if (num == index + 1 || left2right.get(index+1).contains(num)){
//                    //successfull check until now, leave be
//                }else{
//                    a.remove(num);
//                    int tempIndex = index-1;
//                    while (tempIndex>0){
//                        left2right.get(tempIndex).remove(num);
//                        tempIndex--;
//                    }
//                }
//            }
//            index++;
//        }

        //Now get union of left2right and right2left to see full possibility map
        ArrayList<ArrayList<Integer>> union = new ArrayList<ArrayList<Integer>>(right2left);
        int index = 0;
        for (ArrayList<Integer> a : left2right) {
            union.get(index).addAll(a);
            index++;
        }

//        System.out.println();
//        for (ArrayList<Integer> a : union){
//            System.out.println(a);
//        }
//        System.out.println();
        ThreadAssignment ta = new ThreadAssignment();
        ta.ThreadDistribute(union); //Accessibility list and thread number!

    }
}
