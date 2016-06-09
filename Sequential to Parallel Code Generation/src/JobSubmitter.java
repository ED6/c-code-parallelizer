import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.dataproc.Dataproc;
import com.google.api.services.dataproc.DataprocScopes;
import com.google.api.services.dataproc.model.*;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;


/* Class responsible for submitting a Spark job to the cluster */

public class JobSubmitter {
    private static String jobID;
    private static Job ourjob;
    private static Dataproc dataprocService;
    private static JsonFactory jf = new JacksonFactory();
    public static final String DATA_PATH = "gs://ed6-cute-bucket/data.txt",
            DATATABLE_PATH = "gs://ed6-cute-bucket/datatable.txt",
            SECTION_NUMBER = "5", OUTPUT_PATH = "gs://ed6-cute-bucket/output.txt";


    public static Dataproc getDataprocService() throws IOException, GeneralSecurityException {
        if (null == dataprocService) {
            GoogleCredential credential = GoogleCredential.getApplicationDefault();
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(DataprocScopes.all());
            }
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataprocService = new Dataproc.Builder(httpTransport, jf, credential).
                    setApplicationName("DepFinder").build();
        }
        return dataprocService;
    }

    /**
     * Main for testing
     *
     * @param args
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void main(String args[]) throws IOException, GeneralSecurityException {

        //Creating a random job ID (for querying the submission results later)
        String curJobId = UUID.randomUUID().toString();
        jobID = curJobId;

        Dataproc dataproc = getDataprocService();
        dataproc.projects().regions().jobs().submit(
                "sapient-spark-127710", "global", new SubmitJobRequest()
                        .setJob(new Job()
                                .setReference(new JobReference()
                                        .setJobId(curJobId))
                                .setPlacement(new JobPlacement()
                                        .setClusterName("cluster-1"))
                                .setSparkJob(new SparkJob()
                                        .setMainJarFileUri("gs://ed6-cute-bucket/CloudTest.jar")
                                        .setArgs(ImmutableList.of(
                                                DATA_PATH, DATATABLE_PATH, SECTION_NUMBER, OUTPUT_PATH)))))
                .execute();

        System.out.println("Job has been submitted for execution");

        //Checking job status

        while (true) {
            // System.out.println(getJobStatus());
            //String currentStatus = getJobStatus();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (getJobStatus().equals("RUNNING")) {
                System.out.println("The job is running...");
                // currentStatus = "RUNNING";
            } else if (getJobStatus().equals("ERROR")) {
                System.out.println("An error has occured");
                break;
            } else if (getJobStatus().equals("DONE")) {
                System.out.println("The job has completed successfully");
                break;
            }
        }

    }

    /**
     * Checks the submitted job status
     *
     * @return Status of the job (PENDING, RUNNING, ERROR, DONE, etc.)
     */
    public static String getJobStatus() {
        Dataproc dataproc = null;
        try {
            dataproc = getDataprocService();
            ourjob = dataproc.projects().regions().jobs().get("sapient-spark-127710", "global", jobID).execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        String jobState = ourjob.getStatus().getState();
        return jobState;
    }


    /**
     * Submits a job to the cluster
     *
     * @param projectName The name of the Google Cloud project
     * @param scope       The scope of the project (use "global")
     * @param clusterName The name of the cluster to submit the job to
     * @param mainJarUri  URI of the Main Jar File on the cloud ("gs://bucket-name/JarName.jar")
     * @param args        List of arguments to pass to the main class of the job JAR
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void submitJob(String projectName, String scope, String clusterName,
                          String mainJarUri, List args) throws IOException, GeneralSecurityException {

        //Creating a random job ID (for querying the submission results later)
        String curJobId = UUID.randomUUID().toString();
        jobID = curJobId;
        Dataproc dataproc = getDataprocService();
        dataproc.projects().regions().jobs().submit(
                projectName, scope, new SubmitJobRequest()
                        .setJob(new Job()
                                .setReference(new JobReference()
                                        .setJobId(curJobId))
                                .setPlacement(new JobPlacement()
                                        .setClusterName(clusterName))
                                .setSparkJob(new SparkJob()
                                        .setMainJarFileUri(mainJarUri)
                                        .setArgs(args))))
                .execute();

        System.out.println("Job has been submitted for execution");
    }

}
