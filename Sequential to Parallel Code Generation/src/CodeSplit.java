import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class CodeSplit {

    static String keywords[] = {"auto", "double", "int", "struct", "break", "else", "long", "case",
            "enum", "register", "typedef", "char", "extern", "return", "union", "const", "float", "short",
            "unsigned", "continue", "signed", "void", "default", "goto", "sizeof", "volatile", "static",
            "#define", "#include", "main"};

    ArrayList<String> PreCode = new ArrayList<String>();
    ArrayList<String> PostCode = new ArrayList<String>();
    ArrayList<String> MainCode = new ArrayList<String>();
    ArrayList<String> NewKeywords = new ArrayList<String>();
    ArrayList<String> Section0 = new ArrayList<String>();
    ArrayList<String> constants;
    HashMap variables;

    public ArrayList<String> Split(ArrayList<String> tokens) {
        int index = 0;
        while (!tokens.get(index).equals("main")) {
            PreCode.add(tokens.get(index));
            index++;
        }
        while (!tokens.get(index).equals("{")) {
            PreCode.add(tokens.get(index));
            index++;
        }
        PreCode.add(tokens.get(index));
        index++;

        // ; is invalid because only import/define scenario
        while (PreCode.size() != 0 && !PreCode.get(PreCode.size() - 1).equals("main")) {
            PreCode.remove(PreCode.size() - 1);
        }
        while (isKeyword(PreCode.get(PreCode.size() - 1))) {
            PreCode.remove(PreCode.size() - 1);
        }

        //Call harvesting of precode here (save it in a list or something that program caller can get
//        System.out.println("(CodeSplit) Before: " + PreCode);
        NewKeywords = Harvester.getKeywords(PreCode);
        PreCode = (ArrayList) Harvester.CodeTokens.clone();
//        System.out.println("(CodeSplit) After: " + PreCode);
//        variables = Harvester.variables;
//        constants = Harvester.constants;
//        System.out.println(variables);
//        System.out.println(NewKeywords);
//        System.out.println(constants);

        int bracketCount = 1;
        while (bracketCount != 0) {
            if (tokens.get(index).equals("{")) {
                bracketCount++;
            } else if (tokens.get(index).equals("}")) {
                bracketCount--;
            }
            MainCode.add(tokens.get(index));
            index++;
        }
        index--;
        MainCode.remove(MainCode.size() - 1);
        //                                                              do section 0 stuff here: (+remove return????)
        getSection0();
        //System.out.println("LUL: " + Section0);
        NewKeywords = Harvester.getKeywords(Section0);
        Section0 = (ArrayList) Harvester.CodeTokens.clone();

//        System.out.println(variables);
//        System.out.println(NewKeywords);
//        System.out.println(constants);
//        System.out.println(MainCode);

        while (index < tokens.size()) {
            PostCode.add(tokens.get(index));
            index++;
        }
        NewKeywords = Harvester.getKeywords(PostCode);
        PostCode = Harvester.CodeTokens;
        variables = Harvester.variables;
        constants = Harvester.constants;
        //System.out.println(PostCode);
        return MainCode;
    }

    private void getSection0() {
        int index = 0;
        int brackets = 0;
        while (isKeyword(MainCode.get(index))) {
            //if define -> Keyword
            if (MainCode.get(index).equals("#define")) {
                index++;
                while (!isKeyword(MainCode.get(index))) {
                    index++;
                }
            }
            //if variable declaration -> ;
            else {
                while (!MainCode.get(index).equals(";") || brackets != 0) {
                    if (MainCode.get(index).equals("{")) {
                        brackets++;
                    } else if (MainCode.get(index).equals("}")) {
                        brackets--;
                    }
                    index++;
                }
                index++;
            }
        }
        for (String s : MainCode.subList(0, index)) {
            Section0.add(s);
        }
        ArrayList<String> temp = new ArrayList<String>();
        for (String s : MainCode.subList(index, MainCode.size())) {
            temp.add(s);
        }
        MainCode = temp;
//        System.out.println(MainCode);
//        System.out.println(MainCode.size());
//        System.out.println(index);
    }

    //check if a string is part of keywords C89 or custom precoded keywords
    private boolean isKeyword(String str) {
        if (str.charAt(0) == '$') {
            str = str.substring(1);
        }
        boolean testResult = false;
        for (String d : keywords) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        for (String d : NewKeywords) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        return testResult;
    }
}
