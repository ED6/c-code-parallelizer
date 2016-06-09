import java.util.ArrayList;
import java.util.HashMap;

public class Harvester {
    static HashMap variables = new HashMap();
    static String keywords[] = {"auto", "double", "int", "struct", "break", "else", "long", "switch", "case",
            "enum", "register", "typedef", "char", "extern", "return", "union", "const", "float", "short",
            "unsigned", "continue", "for", "signed", "void", "default", "goto", "sizeof", "volatile", "do",
            "if", "static", "while", "#define", "#include", "main"};
    static ArrayList<String> NewKeywords = new ArrayList<String>();
    static ArrayList<String> constants = new ArrayList<String>();
    static ArrayList<String> CodeTokens = new ArrayList<String>();

    public static ArrayList<String> getKeywords(ArrayList<String> preCodeTokens) {
        //System.out.println(preCodeTokens);

        int index = 0;
        while (index < preCodeTokens.size()) {
            if (preCodeTokens.get(index).equals("#include")) {
                index = index + 4;
            }
            //define keyword extraction if not a function
            else if (preCodeTokens.get(index).equals("#define") && preCodeTokens.get(index + 1).charAt(0) != '?') {
                preCodeTokens.add(index,"\n");
                index++;
                index++;
                uniqueAdd(preCodeTokens.get(index));
                index = index + 2;
            }

            //if typedefinition is happening, either with declaration or without
            else if (preCodeTokens.get(index).equals("typedef")) {
                preCodeTokens.add(index, "\n");
                index++;
                index++;

                //enum declaration and typedef
                if (preCodeTokens.get(index).equals("enum") && preCodeTokens.get(index + 2).equals("{")) {
                    index++;
                    uniqueAdd(preCodeTokens.get(index));
                    index = index + 2;
                    //uniqueAdd(preCodeTokens.get(index));
                    constants.add(fix(preCodeTokens.get(index)));
                    index++;
                    boolean condition = true;
                    while (condition) {
                        if (preCodeTokens.get(index).equals(",")) {
                            index++;
                            //uniqueAdd(preCodeTokens.get(index));
                            constants.add(fix(preCodeTokens.get(index)));
                            index++;
                        } else if (preCodeTokens.get(index).equals("}")) {
                            condition = false;
                            index++;
                        } else {
                            index++;
                        }
                    }
                    while (!preCodeTokens.get(index).equals(";")) {
                        if (!preCodeTokens.get(index).equals(",")) {
                            uniqueAdd(preCodeTokens.get(index));
                        }
                        index++;
                    }
                } else if ((preCodeTokens.get(index).equals("struct") || preCodeTokens.get(index).equals("union")) && preCodeTokens.get(index + 2).equals("{")) {
                    int brackets = 1;
                    index++;
                    uniqueAdd(preCodeTokens.get(index));
                    index = index + 2;                            //skip the {
                    while (brackets != 0) {
                        if ((preCodeTokens.get(index).equals("struct") || preCodeTokens.get(index).equals("union")) && preCodeTokens.get(index + 2).equals("{")) {
                            brackets++;
                            index++;
                            uniqueAdd(preCodeTokens.get(index));
                            index = index + 2;
                        } else if (preCodeTokens.get(index).equals("}")) {
                            brackets--;
                            index++;
                        } else {
                            index++;
                        }
                    }
                    while (!preCodeTokens.get(index).equals(";")) {
                        if (!preCodeTokens.get(index).equals(",")) {
                            uniqueAdd(preCodeTokens.get(index));
                        }
                        index++;
                    }
                } else if (isKeyword(preCodeTokens.get(index))) {
                    index++;
                    while (!preCodeTokens.get(index).equals(";")) {
                        if (!preCodeTokens.get(index).equals(",")) {
                            uniqueAdd(preCodeTokens.get(index));
                        }
                        index++;
                    }
                }
            }

            //non typedef enum
            else if (preCodeTokens.get(index).equals("enum") && preCodeTokens.get(index + 2).equals("{")) {
                preCodeTokens.add(index, "\n");
                index++;
                index++;
                uniqueAdd(preCodeTokens.get(index));
                index = index + 2;
                //uniqueAdd(preCodeTokens.get(index));
                constants.add(fix(preCodeTokens.get(index)));
                index++;
                boolean condition = true;
                while (condition) {
                    if (preCodeTokens.get(index).equals(",")) {
                        index++;
                        //uniqueAdd(preCodeTokens.get(index));
                        constants.add(fix(preCodeTokens.get(index)));
                        index++;
                    } else if (preCodeTokens.get(index).equals("}")) {
                        condition = false;
                        index++;
                    } else {
                        index++;
                    }
                }
                while (!preCodeTokens.get(index).equals(";")) {
                    //do not add aliases even if there
                    index++;
                }
            }

            //nontypedef struct/union
            else if ((preCodeTokens.get(index).equals("struct") || preCodeTokens.get(index).equals("union")) && preCodeTokens.get(index + 2).equals("{")) {
                int brackets = 1;
                preCodeTokens.add(index, "\n");
                index++;
                index++;
                uniqueAdd(preCodeTokens.get(index));
                index = index + 2;                            //skip the {
                while (brackets != 0) {
                    if ((preCodeTokens.get(index).equals("struct") || preCodeTokens.get(index).equals("union")) && preCodeTokens.get(index + 2).equals("{")) {
                        brackets++;
                        index++;
                        uniqueAdd(preCodeTokens.get(index));
                        index = index + 2;
                    } else if (preCodeTokens.get(index).equals("}")) {
                        brackets--;
                        index++;
                    } else {
                        index++;
                    }
                }
                while (!preCodeTokens.get(index).equals(";")) {
                    index++;
                }
            }

            //non main global variable
            else if (isKeyword(preCodeTokens.get(index))) {
//                System.out.println(preCodeTokens.get(index));
                String str = fix(preCodeTokens.get(index));
                preCodeTokens.add(index, "\n");
                index++;
                index++;
                while (isKeyword(preCodeTokens.get(index))) {
                    str = str + " " + fix(preCodeTokens.get(index));
                    index++;
                }
                //System.out.println(preCodeTokens.get(index));
                if (preCodeTokens.get(index).charAt(0) == '?') {
                    //skip the whole funciton
                    while (!preCodeTokens.get(index).equals(")")) {
                        index++;
                    }
                    index++;
                    if (preCodeTokens.get(index).equals("{")) {
                        int brackets = 1;
                        index++;
                        while (brackets != 0) {
                            if (preCodeTokens.get(index).equals("{")) {
                                brackets++;
//                                System.out.println("incr");
                                index++;
                            } else if (preCodeTokens.get(index).equals("}")) {
                                brackets--;
//                                System.out.println("decr");
                                index++;
                            } else {
                                index++;
                            }
                        }
                        //System.out.println("done looper");
                    }
                }
                //main detected
                else if (preCodeTokens.get(index).equals("(")) {
                    index++;
                    while (!preCodeTokens.get(index).equals(")")) {
                        str = fix(preCodeTokens.get(index));
                        index++;
                        while (isKeyword(preCodeTokens.get(index)) || preCodeTokens.get(index).equals("*")) {
                            str = str + " " + fix(preCodeTokens.get(index));
                            index++;
                        }
                        String pointers = "";
                        Boolean condition = true;
                        int tempIndex = index;
                        while (condition) {
                            if (preCodeTokens.get(tempIndex + 1).equals("[")) {
                                tempIndex++;
                                pointers = pointers + "*";
                                while (!preCodeTokens.get(tempIndex).equals("]")) {
                                    tempIndex++;
                                }
                            } else {
                                condition = false;
                            }
                        }
                        if (!pointers.equals("")){
                            str = str+"%";
                            pointers = "";
                        }
//                        System.out.println(preCodeTokens.get(index));
//                        System.out.println("Adding gloal var: " + preCodeTokens.get(index));
//                        System.out.println("Its is stored as a: " + str);
                        variables.put(fix(preCodeTokens.get(index)), str);
                        while (!isKeyword(preCodeTokens.get(index)) && index < preCodeTokens.size() - 2) {
                            index++;
                        }
                    }
                } else {
                    String pointers = "";
                    Boolean condition = true;
                    int tempIndex = index;
                    while (condition) {
                        if (preCodeTokens.get(tempIndex + 1).equals("[")) {
                            tempIndex++;
                            pointers = pointers + "*";
                            while (!preCodeTokens.get(tempIndex).equals("]")) {
                                tempIndex++;
                            }
                        } else {
                            condition = false;
                        }
                    }
                    if (!pointers.equals("")){
                        str = str+"%";
                        pointers = "";
                    }
                    //System.out.println(preCodeTokens.get(index));
                    variables.put(fix(preCodeTokens.get(index)), str);
//                    System.out.println("Adding gloal var: " + preCodeTokens.get(index));
//                    System.out.println("Its is stored as a: " + str);
                    index++;
                    while (!preCodeTokens.get(index).equals(";")) {
                        pointers = "";
                        //in case there is a struct or union it will have , without being multiple variables!
                        if (preCodeTokens.get(index).equals("{")) {
                            //skip till end of this datatype
                            int bracketcount = 1;
                            index++;
                            while (bracketcount != 0) {
                                if (preCodeTokens.get(index).equals("{")) {
                                    bracketcount++;
                                } else if (preCodeTokens.get(index).equals("}")) {
                                    bracketcount--;
                                }
                                index++;
                            }
                            index--;
                        } else if (preCodeTokens.get(index).equals(",")) {
                            index++;
                            condition = true;
                            tempIndex = index;
                            while (condition) {
                                if (preCodeTokens.get(tempIndex + 1).equals("[")) {
                                    tempIndex++;
                                    pointers = pointers + "*";
                                    while (!preCodeTokens.get(tempIndex).equals("]")) {
                                        tempIndex++;
                                    }
                                } else {
                                    condition = false;
                                }
                            }
                            if (!pointers.equals("")){
                                str = str+"%";
                                pointers = "";
                            }
                            variables.put(fix(preCodeTokens.get(index)), str);
//                            System.out.println("Adding gloal var: " + preCodeTokens.get(index));
//                            System.out.println("Its is stored as a: " + str);
                        }
                        index++;
                    }
                }
            }


            //other token
            else {
                index++;
                //System.out.println("else at index: " + index);
            }
        }
        CodeTokens.clear();
        CodeTokens = preCodeTokens;
        //System.out.println(preCodeTokens);
        //System.out.println(constants);
        //System.out.println(variables);
        return NewKeywords;
    }

    static String fix(String s) {
        if (s.charAt(0) == '$') {
            s = s.substring(1);
        }
        return s;
    }

    static void uniqueAdd(String str) {
        str = fix(str);
        if (!NewKeywords.contains(str) && !isKeyword(str)) {
            NewKeywords.add(str);
        }
    }

    static boolean isKeyword(String str) {
        //wish java had a auto-generate list like haskell does: this comment is obsolete tho...
        str = fix(str);
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