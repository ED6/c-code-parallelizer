import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Generate the parallel C code from sections and measure its execution time
 */
public class Generator {
    private static ArrayList<String> Sections = new ArrayList<>();     //Formatted sections
    private static ArrayList<ArrayList<Integer>> ExecutionTable =      //Table with section execution order
            new ArrayList<>();
    private static ArrayList<String> Variables;                        //List of Variables with no duplicates

    /**
     * Get variables from Main tokens
     *
     * @param tokens Main tokens
     * @return ArrayList of detected variables
     */
    private ArrayList<String> getVars(ArrayList<String> tokens) {
        ArrayList<String> vars = new ArrayList<>();      //store vars here
        for (String string : tokens) {
            if (string.charAt(0) == '$') {               //if is a variable
                if (string.contains(".")) {              //if contains dot
                    String fP = string.split("\\.")[0];  //part before dot
                    vars.add(fP.substring(1));           //add only part before dot without dollar sign to Variables
                } else {
                    vars.add(string.substring(1));       //add the Var without dollar sign to the Var list
                }
            }
        }
        return vars;
    }

    /**
     * Get formatted sections from Main tokens
     *
     * @param tokens   Main tokens
     * @param varTypes HashMap Variable:Type (types of all the variables in the program) from Harvester
     * @return ArrayList of formatted sections
     */
    private ArrayList<String> getSections(ArrayList<String> tokens, HashMap<String, String> varTypes) {
        ArrayList<String> sections = new ArrayList<>();        //store formatted sections here
        StringBuilder stringBuilder = new StringBuilder();
        //int index = 0;                                       //keep index of tokens for look-ahead
        for (String string : tokens) {
            if (!string.equals("@")) {                         //if a token is not an "@" (section separator)
                if (string.charAt(0) == '$') {                 //if is a variable
                    if (string.contains(".")) {                //if contains dot
                        String fP = string.split("\\.")[0];    //part before dot
                        String sP = string.split("\\.")[1];    //part after dot
                        stringBuilder.append(fP.replace("$", "(*")).append(")").append(".").
                                append(sP).append(" ");        //add in format (*var).field for structs
                    } else if (varTypes.containsKey(string.replace("$", ""))
                            && varTypes.get(string.replace("$", ""))
                            .contains("%")) {                  //if current var is an array
                        stringBuilder.append(string.           //append in format (var)
                                replace("$", "(")).append(") ");
                    } else {                                   //else append in format (*var)
                        stringBuilder.append(string.
                                replace("$", "(*")).append(") ");
                    }
                } else if (string.charAt(0) == '?'
                        && (string.length() > 1))              //if is a function, remove function marker ("?")
                    stringBuilder.append(string.replace("?", "")).append(' ');
                else                                           //else just append a token to SB
                    stringBuilder.append(string).append(' ');
            } else {                                           //if we meet an "@"
                sections.add(stringBuilder.toString());        //add the gathered stringBuilder to the list
                stringBuilder.setLength(0);                    //clear the stringBuilder
            }
            //index++;
        }
        return sections;
    }

    /**
     * Get code String from code Tokens (preCode, sec0 or postCode)
     *
     * @param codeTokens Code Tokens
     * @return String with the code
     */
    private String getCode(ArrayList<String> codeTokens) {
        String precode;
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        int index = 0; //token index
        for (String string : codeTokens) {
            if (string.equals("#include")) {                      //if is "#include"
                stringBuilder.append("\n").append(string).append(' ');
                count = 3;                                        //do not use spaces for the next 3 tokens
            } else if ((string.equals("#define") || string.equals("#ifdef")
                    || string.equals("#ifndef")) && codeTokens.get(index + 1).charAt(0) == '?') {
                System.out.println("                    define func detected");
                stringBuilder.append("\n").append(string).append(' ');
                count = 1;
            } else if (string.charAt(0) == '$') {                 //if is a variable, remove $ and append to SB
                stringBuilder.append(string.replace("$", ""));
                if (count == 0)
                    stringBuilder.append(' ');
                else {
                    count--;
                }
            } else if (string.charAt(0) == '?') {                 //if starts with a question mark, remove it (function) and append to SB
                stringBuilder.append(string.replace("?", ""));
                if (count == 0) {
                    stringBuilder.append(' ');
                } else {
                    count--;
                }
            } else {                                              //else just append a token to SB
                if (count == 0)
                    stringBuilder.append(string).append(' ');
                else {
                    stringBuilder.append(string);
                    count--;
                }
            }
            index++;
        }
        precode = stringBuilder.toString();
        stringBuilder.setLength(0);
        return precode;
    }

    /**
     * Generate Struct for all the arguments
     *
     * @param varNum Number of variables in a program
     * @return Generated string with the Struct
     */
    private String generateStruct(int varNum) {
        String introStr = "\n/* Struct for arguments */\ntypedef struct sdata_struct{\n"; //intro string for every Struct
        String innerStr;
        String structStr;
        StringBuilder stringBuilder = new StringBuilder();
        //add as many arguments as needed to stringBuilder
        for (int i = 1; i <= varNum; i++) {
            stringBuilder.append("  void *arg").append(i).append(";").append("\n");
        }
        innerStr = stringBuilder.toString();                     //build the string with arguments
        structStr = introStr + innerStr + "} sdata_struct;\n\n"; //unite first two strings and add finish bracket and alias
        return structStr;
    }

    /**
     * Generate thread functions for sections with no variables
     *
     * @param secNum Number of the section
     * @return String with the code for the corresponding section thread function
     */
    private String generateThreadFunc(int secNum) {
        //intro string that is the same for all sectionThreads with no variables
        String introStr = "/*Section " + secNum + " thread*/\nvoid *sec_" + secNum + "()\n" +
                "{\n";
        String code = "    " + Sections.get(secNum - 1) + "\n"; //get section
        String threadFuncStr = "    return NULL;\n}\n";
        return introStr + code + threadFuncStr;
    }

    /**
     * Generate thread functions for sections containing variables
     *
     * @param secNum   Number of the section
     * @param varTypes HashMap Variable:Type with types of all the variables
     * @param varNames Array of strings with names of variables in that section
     * @return String with the code for the corresponding section thread function
     */
    private String generateThreadFunc(int secNum, HashMap<String, String> varTypes, String... varNames) {
        //intro string that is the same for all sectionThreads
        String introStr = "/*Section " + secNum + " thread*/\nvoid *sec_" + secNum + "(void *void_ptr)\n" +
                "{\n    sdata_struct *data_str_thread = (sdata_struct*) void_ptr;\n";
        String varStr;
        StringBuilder varStrBuilder = new StringBuilder(); //to build as many lines as needed (depends on the amount of variables)
        //generate templateString for all the variables in the section
        for (String varName : varNames) {
            String varStrTemplate = "    #TYPE# *#VAR# = (#TYPE# *)(data_str_thread->arg"
                    + (Variables.indexOf(varName.split("\\.")[0]) + 1) + ");\n"; //get corresponding index of the current var
            varStr = varStrTemplate.replace("#TYPE#", varTypes.get(varName.split("\\.")[0]).replace("%", ""))/*.split("\\.")[0])*/
                    .replace("#VAR#", varName.split("\\.")[0]); //replace #TYPE# by type of variable defined in a section and #VAR# by the variable itself
            varStrBuilder.append(varStr);
        }
        //get corresponding code from section code
        String sectionCode = Sections.get(secNum - 1);
        String code = "    " + sectionCode + "\n";
        String threadFuncStr = "    return NULL;\n}\n";
        return introStr + varStrBuilder.toString() + code + threadFuncStr;
    }

    /**
     * Generate the beginning of the main function, containing definition
     * of all the pthreads and the struct, as well as time measuring struct
     *
     * @return String with the code for the beginning of the main
     */
    private String generateMainStart() {
        String startStr = "\nint main()\n{\n"; //start the Main
        /** String defining timespec structs and getting program start time*/
        String timeStr = "struct timespec start, end;\n" +
                "/* Start execution time measurment */\nclock_gettime(CLOCK_MONOTONIC, &start);\n";
        /* Get Section0 tokens and convert them to String*/
        ArrayList<String> sec0Tokens = programCaller.Section0;
        String sec0Code = getCode(sec0Tokens);
        String threadDefStrs;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < Sections.size(); i++) {
            stringBuilder.append("pthread_t sec" + i + "_thread;\n");
        }
        threadDefStrs = stringBuilder.toString(); //define threads for all the sections
        stringBuilder.setLength(0);               //clear the stringBuilder
        String structDefStr = "\nstruct sdata_struct sdata;\n";
        String structInsStr;
        for (int i = 0; i < Variables.size(); i++) {
            stringBuilder.append("sdata.arg" + (i + 1) + " = " + "&" + Variables.get(i) + ";\n");
        }
        structInsStr = stringBuilder.toString();
        stringBuilder.setLength(0);
        return startStr + timeStr + sec0Code + "\n\n/*Threads for sections*/\n" + threadDefStrs + "\n/*Define our struct for all the variables used*/"
                + structDefStr + structInsStr;
    }

    /**
     * Starts the threads and joins them for a specific execution set dictated by the Execution Table
     *
     * @param row Number of a row of the table
     * @return A string with generated threads and join methods
     */
    private String generateThreads(int row) {
        String threadsStartString;
        StringBuilder stringBuilder = new StringBuilder();
        //For all elements of the first row of the Execution Table (i.e. first set of executing threads)
        for (int i = 0; i < ExecutionTable.get(row).size(); i++) {
            stringBuilder.append("\nif (pthread_create (&sec" + (ExecutionTable.get(row).get(i)) + "_thread, NULL, sec_"
                    + (ExecutionTable.get(row).get(i)) + ", (void *)&sdata))\n")
                    .append("{\n    fprintf(stderr, \"Error creating thread\\n\");\n    return 1;\n} ");
        }
        //Finished with creating pthreads of the row, add joins for sections
        for (int i = 0; i < ExecutionTable.get(row).size(); i++)
            stringBuilder.append("\npthread_join(sec" + ExecutionTable.get(row).get(i) + "_thread, NULL);\n");
        threadsStartString = stringBuilder.toString();
        return "\n" + threadsStartString;
    }


    /**
     * Generate the parallel C code
     *
     * @param mainTokens     Section separated tokens of the main function of the program obtained from LexAnalyser class
     * @param varTypes       HashMap Variable:Type (types of all the variables in the program) from Harvester
     * @param secVars        HashMap SectionNumber:SectionVariables (which section has which variables) from RWrec
     * @param executionTable ArrayList<ArrayList<String>> with execution order of the program from ThreadAssignment
     */
    public void generateCode(ArrayList<String> mainTokens, HashMap<String, String> varTypes,
                             HashMap<Integer, ArrayList<String>> secVars, ArrayList<ArrayList<Integer>> executionTable) {
        /* Get tokens and convert them to Strings */
        ArrayList<String> postCodeTokens = programCaller.PostCode;
        ArrayList<String> preCodeTokens = programCaller.PreCode;
        String postCode = getCode(postCodeTokens);
        String preCode = getCode(preCodeTokens);
        ExecutionTable = executionTable; //generateThreads uses the ExecutionTable, so assign it to the passed param
        mainTokens.add("@"); //add "@" to the end of the Main tokens
        System.out.println("Main tokens: " + mainTokens.toString()); //DEBUG
        ArrayList<String> variables = getVars(mainTokens);
        Sections = getSections(mainTokens, varTypes); //get formatted sections
        System.out.println("Sections: " + Sections);             //DEBUG
        /* Convert list of variables to a HashSet and then back to list to get rid of duplicates */
        Variables = new ArrayList<>(new HashSet<>(variables));
        System.out.println("Variables detected: " + Variables);
        /* Start the code generation */
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("generatedCode.c"));
            writer.write("#include <stdio.h>\n#include <pthread.h>\n#include <time.h>\n#include <inttypes.h>");
            /* The includes and precode here! */
            writer.write(preCode);
            /* Write the method required for execution time measurment*/
            writer.write("\n\n/*Defining function for execution time measurment*/\nint64_t " +
                    "timespecDiff(struct timespec *timeA_p, struct timespec *timeB_p)\n" +
                    "{\n" +
                    "  return ((timeA_p->tv_sec * 1000000000) + timeA_p->tv_nsec) -\n" +
                    "           ((timeB_p->tv_sec * 1000000000) + timeB_p->tv_nsec);\n" +
                    "}\n");
            /* Generate the struct for arguments */
            writer.write(generateStruct(Variables.size()));
            /* Generate the thread functions for sections */
            ArrayList<String> temp = new ArrayList<>(); //storing variables of every section of secVars
            for (int i = 1; i < secVars.size(); i++) {  //For every section
                if (!secVars.get(i).isEmpty()) {        //If section contains variables
                    for (int j = 0; j < secVars.get(i).size(); j++) {      //Fill the temp. arrayList with variables
                        temp.add(secVars.get(i).get(j));
                    }
                    String[] vars = temp.toArray(new String[temp.size()]); //Convert arrayList to Array
                    writer.write(generateThreadFunc(i, varTypes, vars));   //generate corresponding thread function
                    temp.clear();                                          //clear temp arrayList
                } else {   //If section has no variables, call function without variable arguments
                    writer.write(generateThreadFunc(i));
                }
            }
            /* Generate start of main, including section 0, section threads and assignment of vars to struct  */
            writer.write(generateMainStart());
            writer.write("\n/*Creating pthreads*/");
            /* Create Threads and Joins for corresponding rows of the ExecutionTable */
            for (int i = 0; i < executionTable.size(); i++) {
                writer.write(generateThreads(i));
            }
            writer.write("\n/* Finish execution time measurment */\nclock_gettime(CLOCK_MONOTONIC, &end);\n" +
                    "  uint64_t totalTimeElapsed = timespecDiff(&end, &start);\n" +
                    "  printf(\"\\nTime elapsed (in nanoseconds): %\" PRIu64 \"\\n\", totalTimeElapsed);\n" +
                    "  printf(\"Time elapsed (in seconds): %f\",totalTimeElapsed/1000000000.0);");
            writer.write("\n\nreturn 0;");
            writer.write("\n" + postCode);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}