import java.util.ArrayList;

/**
 * Class that takes the arraylist and adds the @ symbol do distinguish section ends
 */
public class SectionSeparator {
    static ArrayList<String> NewKeywords;
    static ArrayList<String> constants;

    public static ArrayList<String> separate(ArrayList<String> tokens, ArrayList<String> extraKeywords, ArrayList<String> constantsP) {
        NewKeywords = extraKeywords;
        constants = constantsP;
        int index = 0;
        ArrayList<String> newTokens = new ArrayList<String>();
        int count = 0;
        int elim = 0;
        while (index < tokens.size()) {
            newTokens.add(tokens.get(index));
            if (tokens.get(index).equals("{")) {
                count++;
                if (!tokens.get(index - 1).equals(")")) {
                    //index++;
                    while (!tokens.get(index).equals("}")) {
                        //newTokens.add(tokens.get(index));
                        index++;
                        newTokens.add(tokens.get(index));
                    }
                    count--;
                    tokens.set(index, "");
                }
                //System.out.println("Count increase to: " + count);
            } else if (tokens.get(index).equals("}")) {
                count--;
                //System.out.println("Count decrease to: " + count);
            } else if (tokens.get(index).equals("for")) {
                elim = 2;
            }
            try {
                if ((tokens.get(index).equals(";") || tokens.get(index).equals("}")) && count == 0 && !tokens.get(index + 1).equals("else") && elim == 0) {
                    if (!newTokens.get(newTokens.size() - 2).equals("@")) {
                        newTokens.add("@");
                    } else {
                        newTokens.remove(newTokens.size() - 1);
                    }
                }
                if (tokens.get(index).equals(";") && elim != 0) {
                    elim--;
                }
            } catch (Exception e) {
                //System.out.println("End reached");
            }
            index++;
        }
        index = 0;
        while (index < newTokens.size()) {
            if (newTokens.get(index).charAt(0) == '$') {
                String temp = fix(newTokens.get(index));
                if (isKeyword(temp)) {
                    newTokens.set(index, temp);
                }
                //System.out.println(newTokens.get(index));
            }
            index++;
        }
        return newTokens;
    }

    private static boolean isKeyword(String str) {
        boolean testResult = false;
        for (String d : NewKeywords) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        for (String d : constants) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        return testResult;
    }

    static String fix(String s) {
        if (s.charAt(0) == '$') {
            s = s.substring(1);
        }
        return s;
    }
}
