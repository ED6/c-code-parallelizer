/**
 * LexAnalyser class goes thorugh a .txt file with code and tokenizes into a list
 * TODO: automatic keyword adding: functions, structs, enums, defines
 * merge transition states into state transitions 5 15 16 14
 * isKeyword, ... implementation with outside data
 */

import java.util.ArrayList;
import java.io.*;

public class LexAnalyser {

    String tripleOps[] = {"<<=", ">>="};
    String constOps[] = {"+-", "-+", "=+", "=-", "*+", "*-", "/+", "/+", "|+", "|*", "&-", "&+", "%+", "%-", "<+",
            "<-", "^+", "^-", "!+", "!-", "~+", "~-"};
    String doubleOps[] = {"++", "--", "==", "||", "&&", "+=", "-=", "*=", "/=", "&=", "|=", "%=", "!=", "<=",
            ">=", "^=", "~=", "<<", ">>"};
    String keywords[] = {"auto", "double", "int", "struct", "break", "else", "long", "switch", "case",
            "enum", "register", "typedef", "char", "extern", "return", "union", "const", "float", "short",
            "unsigned", "continue", "for", "signed", "void", "default", "goto", "sizeof", "volatile", "do",
            "if", "static", "while", "#define", "#include", "main"};

    public ArrayList<String> _scan(String fileNameParam) {
        ArrayList<String> lexic = new ArrayList<String>();
        String fileName = fileNameParam;
        String code = "";
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            char readChar;
            int notyetChar; //temporary holder for read char in int format
            while ((notyetChar = bufferedReader.read()) != -1) {
                readChar = (char) notyetChar;
                code = code + readChar;
                //System.out.println(readChar);
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        } catch (IOException ex) {
            System.out.println("Another error reading file '" + fileName + "'");
        }
        code = code + "\n";
        //System.out.println(code)
        //iterate through chars and tokenize
        String temp = "";
        int index = 0;
        int state = 1;
        boolean hex = false;
        while (index < code.length()) {
            //System.out.print(state + ": ");
            //System.out.println(code.charAt(index) + " " +index);
            //System.out.println(code.charAt(index));
            switch (state) {
                case 1:
                    //Add to token if possible
                    if (!temp.equals("")) {
                        lexic.add(temp);
                        temp = "";
                    }
                    hex = false;
                    /*TRANSITIONS 1*/
                    //If whitespace skip char. UNICODE "WSpace=Y"
                    if (Character.isWhitespace(code.charAt(index))) {
                        index++;
                    }

                    //if lower case letter examine if it might be a keyword eventually. UNICODE "Ll" Type General Category = 2 (
                    else if (Character.toString(code.charAt(index)).matches("[a-z#]")) {
                        state = 2;
                    }

                    //if upercase or _ it cannot be keyword withing grammar subset, thus go to variable state. UNICODE "Lu" Type General Category = 3
                    else if (Character.toString(code.charAt(index)).matches("[A-Z_]")) {
                        state = 3;
                    }

                    //constant string start UNICODE "Po" Type category = 24 (only a subset!!)
                    else if (code.charAt(index) == '"') {
                        state = 6;
                    }

                    //constant char start UNICODE "Po" Type category = 24 (only a subset!!)
                    else if (code.charAt(index) == '\'') {
                        state = 8;
                    }

                    //if it is special symbol (Special Symbol: [ ] ( ) { } , ; ? :) UNICODE "Ps" "Pe" "Po" (only subset of them!!)
                    else if (Character.toString(code.charAt(index)).matches("[(){},;:\\?\\[\\]]")) {
                        state = 9;
                    }

                    //if it is an operator (+ - | & * / % < > = ! ^ ~)
                    else if (Character.toString(code.charAt(index)).matches("[\\+\\-|&*/%<>=!^~\\\\]")) {
                        state = 10;
                    }

                    //if it is a constant number
                    else if (Character.toString(code.charAt(index)).matches("[0-9]")) {
                        state = 17;
                    } else {
                        System.out.println("Error in transition from state 1 with value: " + code.charAt(index));
                        System.exit(0);
                    }
                    break;

                case 2:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;

                    /*TRANSITIONS 2*/
                    //if still string that could be a keyword
                    if (Character.toString(code.charAt(index)).matches("[a-z]")) {
                        //stay in state
                    }

                    //if upercase, digit or _ it cannot be keyword withing grammar subset, thus go to variable state
                    else if (Character.toString(code.charAt(index)).matches("[A-Z_0-9.]")) {
                        state = 3;
                    }

                    //if any other char, done with token
                    else {
                        state = 4;
                    }
                    break;

                case 3:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;

                    /*TRANSITIONS 3*/
                    //if its continued variable
                    if (Character.toString(code.charAt(index)).matches("[a-zA-Z_0-9.]")) {
                        //remain in state
                    }

                    //if any other char, end token
                    else {
                        state = 5;
                    }
                    break;

                case 4:
                    /*TRANSITIONS 4*/
                    //if a match with a keyword is found
                    if (isKeyword(temp)) {
                        state = 1;
                    }
                    //if no match was found
                    else {
                        state = 5;
                    }
                    break;

                //transition state for special char implementation
                case 5:
                    int i = 0;
                    while (Character.isWhitespace(code.charAt(index + i))) {
                        i++;
                    }
                    if (code.charAt(index + i) == '(') {
                        temp = "?" + temp; //function marker
                    } else {
                        temp = "$" + temp;
                    }
                    state = 1;
                    break;

                case 6:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;
                    //System.out.println("case 6: " + code.charAt(index));
                    /*TRANSITIONS 6*/
                    //if end of string constant
                    if (code.charAt(index) == '"') {
                        state = 1;
                        temp = temp + code.charAt(index);
                        index++;
                    }

                    //if escape symbol
                    else if (code.charAt(index) == '\\') {
                        state = 7;
                    }

                    //not end of string
                    else {
                        //remain in state
                    }
                    break;

                case 7:
                    //System.out.println("case 7");
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;
                    //escaped char adding to temp
                    temp = temp + code.charAt(index);
                    index++;

                    /*TRANSITIONS 7*/
                    //if end of constant string
                    if (code.charAt(index) == '"') {
                        state = 1;
                        temp = temp + code.charAt(index);
                        index++;
                    }
                    //if another excape char
                    else if (code.charAt(index) == '\\') {
                        //remain in state for another escape char
                    }
                    //if not ending and not escape
                    else {
                        state = 6;
                    }
                    break;

                case 8:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;

                    /*TRANSITIONS 8*/
                    //assumes correct format to avoid extra state!
                    //2 char char <- ...
                    if (code.charAt(index) == '\\') {
                        temp = temp + code.charAt(index);
                        index++;
                        temp = temp + code.charAt(index);
                        index++;
                        temp = temp + code.charAt(index);
                        index++;
                        state = 1;
                    }
                    //1 char char
                    else {
                        temp = temp + code.charAt(index);
                        index++;
                        temp = temp + code.charAt(index);
                        index++;
                        state = 1;
                    }
                    break;

                case 9:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;
                    //maybe unnecessary?
                    /*int iter = 1;
                    while (Character.isWhitespace(code.charAt(index-iter))){
                        iter++;
                    }*/
                    //up to here replace iter with 1
                    if ((code.charAt(index) == '+' || code.charAt(index) == '-')
                            && ((code.charAt(index - 1) == '(' && code.charAt(index + 1) != code.charAt(index)) || code.charAt(index - 1) == '[')
                            && (code.charAt(index+1) != '+' && code.charAt(index+1) != '-')) {
                        state = 16;
                    } else {
                        state = 1;
                    }
                    break;

                case 10:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;

                    int iterD = 1;
                    while (Character.isWhitespace(code.charAt(index - iterD))) {
                        iterD++;
                    }
                    int iterU = 0;
                    while (Character.isWhitespace(code.charAt(index + iterU))) {
                        iterU++;
                    }
                    int iterDD = iterD + 1;
                    while (index - iterDD > 0 && Character.isWhitespace(code.charAt(index - iterDD))) {
                        iterDD++;
                    }

                    String OPchecker2 = temp + code.charAt(index+iterU);

                    /*TRANSITIONS 10*/
                    if (Character.toString(code.charAt(index + iterU)).matches("[0-9']")
                            && (code.charAt(index - iterD) == '-' || code.charAt(index - iterD) == '+')
                            && (code.charAt(index - iterDD) == '(' || code.charAt(index - iterDD) == '[')) {
                        state = 17;
                    }
                    //if its a double operator
                    else if (isDoubleOperator(OPchecker2)) {
                        state = 11;
                    }
                    //if its a constant operator
                    else if (isConstOperator(OPchecker2)) {
                        state = 16;
                        index = index + iterU;
                    }
                    //if its single line comment
                    else if (OPchecker2.equals("//")) {
                        state = 12;
                    }
                    //if its multi-line comment
                    else if (OPchecker2.equals("/*")) {
                        //System.out.println("comment happening");
                        state = 13;
                    }
                    //token done
                    else {
                        //System.out.println("OPToken: " + code.charAt(index-1));
                        state = 1;
                    }
                    break;

                case 11:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    String OPchecker3const = "" + code.charAt(index);
                    while (Character.isWhitespace(code.charAt(index + 1))) {        //must eliminate intermediate whitespaces!
                        index++;
                    }
                    OPchecker3const = OPchecker3const + code.charAt(index + 1);
                    //System.out.println("Constant debugging: " + OPchecker3const);
                    index++;
                    String OPchecker3 = temp + code.charAt(index);

                    /*TRANSITIONS 11*/
                    //if its a triple operator
                    if (isTripleOperator(OPchecker3)) {
                        state = 15;
                    }
                    //if its a const operator non +- or -+ to avoid conflict in y = x++ + 5
                    else if (isConstOperator(OPchecker3const) && !OPchecker3const.equals("+-") && !OPchecker3const.equals("-+")) {
                        state = 16;
                    } else {
                        state = 1;
                    }
                    break;

                case 12:
                    temp = "";
                    index++;

                    /*TRANSITIONS 12*/
                    //end of comment
                    if (code.charAt(index) == '\n') {
                        state = 1;
                    }
                    //comment not ended yet
                    else {
                        //remain in state
                    }
                    break;

                case 13:
                    temp = "";
                    index++;
                    /*TRANSITIONS 13*/
                    //maybe end of comment
                    if (code.charAt(index) == '*') {
                        state = 14;
                    }
                    //not end of comment
                    else {
                        //remain in state
                    }
                    break;

                case 14:
                    index++;
                    /*TRANSITIONS 14*/
                    //end of comment
                    if (code.charAt(index) == '/') {
                        //System.out.println(temp + code.charAt(index) + code.charAt(index+1));
                        state = 1;
                        index++;
                    } else if (code.charAt(index) == '*') {
                        //stay in state
                    }
                    //nevermind false alarm
                    else {
                        state = 13;
                    }
                    break;

                //closing operator transition state
                case 15:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;
                    break;

                case 16:
                    if (!temp.equals("")) {
                        lexic.add(temp);
                        temp = "";
                    }
                    state = 17;
                    break;

                case 17:
                    //add current char to temp, move to next
                    temp = temp + code.charAt(index);
                    index++;

                    /*TRANSITIONS 17*/
                    //if continuing digit assuming correct format
                    if (Character.toString(code.charAt(index)).matches("[a-zA-Z0-9.]")) {
                        if (code.charAt(index) == 'x' || code.charAt(index) == 'X') {
                            hex = true;
                        }
                        state = 17;
                    } else if (code.charAt(index - 1) == 'e' && (code.charAt(index) == '+' || code.charAt(index) == '-') && !hex) {
                        //stay in state
                    } else {
                        state = 1;
                    }
                    break;
            }
        }

        //System.out.print(lexic.toString());
        return lexic;
    }


    //Checker if there is a keyword matching
    boolean isKeyword(String str) {
        //wish java had a auto-generate list like haskell does: this comment is obsolete tho...
        boolean testResult = false;
        for (String d : keywords) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        return testResult;
    }

    //checker if it matches a double operator
    boolean isDoubleOperator(String str) {
        boolean testResult = false;
        for (String d : doubleOps) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        return testResult;
    }

    //checker if it is a const operator
    boolean isConstOperator(String str) {
        boolean testResult = false;
        for (String d : constOps) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        return testResult;
    }

    //checker if its a triple operator
    boolean isTripleOperator(String str) {
        boolean testResult = false;
        for (String d : tripleOps) {
            if (str.equals(d)) {
                testResult = true;
            }
        }
        return testResult;
    }

}