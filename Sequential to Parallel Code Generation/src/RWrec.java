import java.io.*;
import java.util.*;


public class RWrec {

    // Start in State 1
    static private int state = 1;
    static private int secnum = 1;

    // Common writing operators
    static private ArrayList<String> wrtops = new ArrayList<String>(
            Arrays.asList("=",/* "++", "--",*/ "+=", "-=", "*=", "/=", "%=", "<<=", ">>=", "&=", "^=", "|="));

    // Stack for nester arrays
    static private Deque<String> stack = new ArrayDeque<String>();

    static private ArrayList<Tuple<String, Integer>> fulldata = new ArrayList<>();
    static private ArrayList<ArrayList<String>> fulldatatable = new ArrayList<>();


    public static void FSM(ArrayList<String> scode) throws IOException {


        String temp1 = "";
        int i = 0;
        fulldatatable.add(new ArrayList<String>());
        fulldatatable.get(0).add("RESERVED");

        fulldatatable.add(new ArrayList<String>());

        // State machine itself: see diagram
        while (i < scode.size()) {
            switch (state) {
                case 1:

                    // System.out.println(stack.peek());
                    // System.out.println(Arrays.toString(stack.toArray())+" ");
                    //System.out.println("State 1 " + scode.get(i));
                    if (isvar(scode.get(i))) {
                        temp1 = scode.get(i);
                        state = 4;
                        i++;
                    } else if (scode.get(i).equals("]")) { //Do we need it?
                        temp1 = stack.peek();
                        stack.pop();
                        state = 4;
                        i++;
                    } else if (scode.get(i).equals("++") || scode.get(i).equals("--")) {
                        state = 5;
                        i++;
                    } else if (scode.get(i).equals("@")) {
                        secnum++;
                        state = 1;
                        i++;
                    } else {
                        state = 1; // no need to reassign the value, is done for comprehension
                        i++;
                    }
                    break;

                case 2:
                    //System.out.println("State 2 " + scode.get(i));
                    if (isvar(scode.get(i))) {
                        temp1 = scode.get(i);
                        state = 4;
                        i++;
                    } else if (scode.get(i).equals("]")) {
                        state = 4;
                        i++;
                    } else if (scode.get(i).equals("++") || scode.get(i).equals("--")) {
                        state = 5;
                        i++;
                    } else {
                        state = 2;
                        i++;
                    }
                    break;

                case 3:
                    //System.out.println("State 3 " + scode.get(i));
                    if (!temp1.equals("")) {
                        if (!isinlist(new Tuple(temp1, secnum), fulldata)) {
                            // System.out.println(temp1 + " " + secnum + " at " + fulldata.size());
                            fulldata.add(new Tuple(temp1, secnum));
                        }


                        // if (secnum>= fulldatatable.size()) fulldatatable.add(new ArrayList<String>());
                        //   while (secnum - fulldatatable.size() >= 0) fulldatatable.add(new ArrayList<String>());
                        if (!isstrongreading(temp1 + "_r", fulldatatable.get(secnum)))
                            fulldatatable.get(secnum).add(temp1 + "_r");
                    }

                    if (scode.get(i).equals("]")) {
                        temp1 = stack.peek();
                        stack.pop();
                        state = 4;
                        i++;
                    } else {
                        state = 1;
                        // i++;
                    }
                    break;

                case 4:
                    //System.out.println("State 4 " + scode.get(i));
                    if (iswriting(scode.get(i))) {
                        state = 6;
                        i++;
                    } else if (scode.get(i).equals("[")) {
                        stack.push(temp1);
                        state = 2;
                        i++;
                    } else if (scode.get(i).equals(")")) {
                        state = 4;
                        i++;
                    } else if (scode.get(i).equals("++") || scode.get(i).equals("--")) {
                        if (scode.get(i + 1).equals("(") || isvar(scode.get(i + 1))) {
                            state = 3;
                            //i++;
                        } else {
                            state = 6;
                            i++;
                        }
                    } else {
                        state = 3;
                    }

                    break;

                case 5:
                    //System.out.println("State 5 " + scode.get(i));
                    if (isvar(scode.get(i))) {
                        temp1 = scode.get(i);
                        state = 6;
                        i++;
                    } else if (scode.get(i).equals("(")) {
                        state = 5;
                        i++;
                    }

                    break;

                case 6:
                    //System.out.println("State 6 " + scode.get(i));
                    if (!temp1.equals("")) {
                        if (!isinlist(new Tuple(temp1, secnum), fulldata)) {
                            // System.out.println(temp1 + " " + secnum + " at " + fulldata.size());
                            fulldata.add(new Tuple(temp1, secnum));
                        }
                        // if (secnum>= fulldatatable.size()) fulldatatable.add(new ArrayList<String>());
                        // while (secnum - fulldatatable.size() >= 0) fulldatatable.add(new ArrayList<String>());
                        if (!isstrongwriting(temp1 + "_w", fulldatatable.get(secnum)))
                            fulldatatable.get(secnum).add(temp1 + "_w");
                    }

                    if (scode.get(i).equals("[")) {
                        stack.push(temp1);
                        state = 2;
                        i++;
                    } else state = 1;
                    break;
            }
            while (secnum - fulldatatable.size() >= 0) fulldatatable.add(new ArrayList<String>());
        }
        removeReturns();
        writetofiles();
    }

    // Check if a token is a variable
    private static boolean isvar(String token) {
        if (token.substring(0, 1).equals("$")) return true;
        else return false;
    }

    // Check if a token is a writing operator
    private static boolean iswriting(String token) {
        for (String element : wrtops) {
            if (token.equals(element)) return true;
        }
        return false;
    }

    // For fulldata elements to be unique
    private static boolean isinlist(Tuple tuple, ArrayList<Tuple<String, Integer>> varsec) { // for fulldata elements to be unique
        for (int i = 0; i < varsec.size(); i++) {
            if (varsec.get(i).get1().equals(tuple.get1()) && varsec.get(i).get2().equals(tuple.get2())) return true;
        }
        return false;
    }

    // Strongest access check in fulldatatable
    private static boolean isstrongreading(String var, ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).substring(0, list.get(i).length() - 1).equals(var.substring(0, var.length() - 1))) {
                if (list.get(i).substring(list.get(i).length() - 1).equals("r") ||
                        list.get(i).substring(list.get(i).length() - 1).equals("w")) return true;
            }
        }
        return false;
    }

    // Strongest access check in fulldatatable
    private static boolean isstrongwriting(String var, ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).substring(0, list.get(i).length() - 1).equals(var.substring(0, var.length() - 1))) {
                if (list.get(i).substring(list.get(i).length() - 1).equals("r")) {
                    list.remove(i);
                    return false;
                } else if (list.get(i).substring(list.get(i).length() - 1).equals("w")) return true;
            }
        }
        return false;
    }

    // Writing to files for Spark with minor remarks
    private static void writetofiles() throws IOException {
        BufferedWriter writer1 = new BufferedWriter(new FileWriter("data.txt"));
        for (int h = 0; h < fulldata.size() - 1; h++) {

            writer1.write(structCheck(fulldata.get(h).get1(), false) + " " + fulldata.get(h).get2());
            writer1.newLine();
            writer1.flush();
        }

        writer1.write(structCheck(fulldata.get(fulldata.size() - 1).get1(), false) + " " + fulldata.get(fulldata.size() - 1).get2());
        writer1.close();


        BufferedWriter writer2 = new BufferedWriter(new FileWriter("datatable.txt"));
        writer2.write("0:RESERVED"); // Do we really need it everywhere?
        writer2.newLine();
        for (int i = 1; i < fulldatatable.size(); i++) {
            if (!fulldatatable.get(i).isEmpty()) {

                writer2.write(i + ":");

                for (int j = 0; j < fulldatatable.get(i).size() - 1; j++) {
                    writer2.write(structCheck(fulldatatable.get(i).get(j), true) + " ");
                }

                writer2.write(structCheck(fulldatatable.get(i).get(fulldatatable.get(i).size() - 1), true));

                if (i < fulldatatable.size() - 1) writer2.newLine();
            }
        }
        writer2.close();
    }

    // Checking if a variable is struct field
    private static String structCheck(String var, boolean accessEnabled) {
        if (var.contains(".")) {
            String temp = var.split("\\.")[0];
            if (accessEnabled) return temp + var.substring(var.length() - 2, var.length()); // handling struct variable
            else return temp;
        }
        return var;  // return initial variable if not a struct field
    }

    // Removing elements with returned variables, so returns are not sections
    private static void removeReturns(){
        int elemToRemove = fulldatatable.get(fulldatatable.size()-1).size();
        int initSize = fulldata.size();
        for (int i = 0; i < elemToRemove; i++){
            fulldata.remove(initSize-1-i);
        }
        fulldatatable.remove(fulldatatable.size()-1);
    }


    // Giving data to Code Generator
    public HashMap<Integer, ArrayList<String>> getMapForCodeGeneration() {

        HashMap<Integer, ArrayList<String>> map = new HashMap<>();
        map.put(0, new ArrayList(Arrays.asList("RESERVED")));
        for (int i = 1; i < fulldatatable.size(); i++) {
            ArrayList<String> tempList = new ArrayList();
            for (int j = 0; j < fulldatatable.get(i).size(); j++) {
                String tempVar = fulldatatable.get(i).get(j);
                tempList.add(tempVar.substring(1, tempVar.length() - 2));
            }
            map.put(i, tempList);
        }
        return map;
    }

    public ArrayList<Tuple<String, Integer>> getFullData() {
        return fulldata;
    }

    public ArrayList<ArrayList<String>> getFullDataTable() {
        return fulldatatable;
    }

    public String getSecNum() {
        return Integer.toString(secnum);
    }

}