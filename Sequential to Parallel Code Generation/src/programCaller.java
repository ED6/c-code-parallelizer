import com.google.common.collect.ImmutableList;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class programCaller {

    //parameters for uploading files
    public static final String BUCKET_NAME = "ed6-cute-bucket"; //"our-bucket-name" without the "gs://" prefix
    //parameters for submitting the job
    private static final String PROJECT_NAME = "sapient-spark-127710";
    private static final String SCOPE = "global";
    private static final String CLUSTER_NAME = "cluster-1";
    private static final String MAIN_JAR_URI = "gs://ed6-cute-bucket/CloudTest.jar";
    public static final String DATA_PATH = "gs://ed6-cute-bucket/data.txt",
            DATATABLE_PATH = "gs://ed6-cute-bucket/datatable.txt",
            OUTPUT_PATH = "gs://ed6-cute-bucket/output.txt";
    private static String sectionNumber; //number of sections in the program (get from RWrec)
    private static List<String> ListOfArgs; //store list of arguments needed for Spark part
    //parameters for downloading file
    private static ArrayList<String> outputFileNames = new ArrayList<>(); //storing names of files in output.txt directory

    private static HashMap<Integer, ArrayList<String>> SecVars =       //HashMap SecNumber:Variables in that section
            new HashMap<>();
    private static ArrayList<ArrayList<Integer>> ExecutionTable =      //Table with section execution order
            new ArrayList<>();
    static ArrayList<String> PreCode;
    static ArrayList<String> PostCode;
    static ArrayList<String> Section0;

    public static void main(String args[]) throws IOException {

        ArrayList<String> Tokens;      //main section 1-end
        ArrayList<String> NewKeywords;
        ArrayList<String> constants;
        HashMap VarTypes;
        LexAnalyser lexAnalyser = new LexAnalyser();
        Tokens = lexAnalyser._scan("Code.c");
        CodeSplit splitter = new CodeSplit();
        Tokens = splitter.Split(Tokens);
        //get new keywords hashlist and constants
        PreCode = splitter.PreCode;
        PostCode = splitter.PostCode;
        NewKeywords = splitter.NewKeywords;
        constants = splitter.constants;
        VarTypes = splitter.variables; //HashMap containing VarTypes here
        System.out.println(VarTypes);
        Section0 = splitter.Section0;

        System.out.println("PRECODE " + PreCode);
        System.out.println("Sec0 " + Section0);
        System.out.println("VarTypes: " + VarTypes);
        System.out.println("POSTCODE " + PostCode);

        //Main Tokens here
        Tokens = SectionSeparator.separate(Tokens, NewKeywords, constants);
        RWrec rw = new RWrec();
        rw.FSM(Tokens);
        System.out.println();

        //get sectionNumber, fill list of Arguments and get SecVars hashMap
        sectionNumber = rw.getSecNum();
        ListOfArgs = ImmutableList.of(DATA_PATH, DATATABLE_PATH,
                sectionNumber, OUTPUT_PATH);
        SecVars = rw.getMapForCodeGeneration(); //HashMap containing SecVars here

        //debug
        System.out.println("SectionVariables: " + SecVars);

        /** Start cloud here*/
        /***********************************/

        System.out.println("Starting the cloud part...");
        long startTime = System.currentTimeMillis();

        //Uploading the input text files
        FileUploader fu = new FileUploader();
        //BucketName, CloudName, path
        fu.uploadFile(BUCKET_NAME, "datatable.txt", "datatable.txt"); //upload datatable
        fu.uploadFile(BUCKET_NAME, "data.txt", "data.txt"); //upload data

        //Submitting the job
        JobSubmitter js = new JobSubmitter();
        try {
            js.submitJob(PROJECT_NAME, SCOPE, CLUSTER_NAME, MAIN_JAR_URI, ListOfArgs);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        //Checking job status
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (JobSubmitter.getJobStatus().equals("RUNNING")) {
                System.out.println("The job is running...");
            } else if (JobSubmitter.getJobStatus().equals("ERROR")) {
                System.out.println("An error has occured. Terminating.");
                System.exit(1);
                /**
                 * Add retry(resubmit) mechanism here
                 * (Shouldn't be necessary)
                 */
            } else if (JobSubmitter.getJobStatus().equals("DONE")) {
                System.out.println("The job has completed successfully. Continuing.");
                break;
            }
        }

        //Downloading the output files
        FileDownloader fd = new FileDownloader();
        //Downloading the output files
        //storing names of files in output.txt directory
        try {
            outputFileNames = fd.getFileNames();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        //For every fileName in the list
        for (String fileName : outputFileNames) {
            System.out.println("Downloading file " + fileName);
            ByteArrayOutputStream out = fd.downloadFile(BUCKET_NAME, fileName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(fileName);
                fileOutputStream.write(out.toByteArray());
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("output.txt directory does not exist");
            }
        }

        //Adding a section number on the top of the first file and merging all the files
        FileMerger fm = new FileMerger();
        try {
            fm.addSecNum(Integer.toString(Integer.valueOf(sectionNumber) - 1)); //modifying secNum for AccessChecker
            fm.mergeFiles(outputFileNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("The cloud part took " + elapsedTime / 1000.0 + " seconds to complete.");
        System.out.println("Cloud part has finished.");

        /***********************************/
        /** End cloud here*/


        /**Get execution table*/
        AccessibilityChecker aChecker = new AccessibilityChecker();
        ExecutionTable = aChecker.getExecutionTable();

        System.out.println("Execution table: " + ExecutionTable);

        /*GENERATE THE CODE*/
        Generator generator = new Generator();
        generator.generateCode(Tokens, VarTypes, SecVars, ExecutionTable);

    }
}
