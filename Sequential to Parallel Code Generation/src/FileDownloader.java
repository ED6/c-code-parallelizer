import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;


//Class responsible for getting output file from the Cloud Storage when the Spark Job is completed

public class FileDownloader {

    //private static ArrayList<String> fileNames = new ArrayList<>(); //ArrayList for storing names of files to download

    private static final boolean IS_APP_ENGINE = false;
    private static final String BUCKET_NAME = "ed6-cute-bucket"; //"our-bucket-name" without the "gs://" prefix
    private static final String CLOUDFILE_NAME = "output.txt/part-00000"; //name of the file in the storage
    private static Storage storageService;

    //getter for fileNames ArrayList


    public static void downloadToOutputStream(Storage storage, String bucketName, String objectName,
                                              OutputStream data) throws IOException {
        Storage.Objects.Get getObject = storage.objects().get(bucketName, objectName);

//        /**Modify the code below to get all the output.txt folder contents  */
//        Storage.Objects.List listObjects = storage.objects().list("ed6-cute-bucket");
//        listObjects.setPrefix("output.txt/p").execute();
//        Objects objects;
//        do {
//            objects = listObjects.execute();
//            for (StorageObject object : objects.getItems()) {
//                fileNames.add(object.getName()); //fill ArrayList with names of the files to download
//                System.out.println(object.getName());
//            }
//            listObjects.setPageToken(objects.getNextPageToken());
//        } while (null != objects.getNextPageToken());
//        System.out.println("listObject = " + listObjects);
//        /** */

        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(!IS_APP_ENGINE);
        getObject.executeMediaAndDownloadTo(data);
    }

    /**
     * Get names of all output files from the corresponding cloud directory
     *
     * @return ArrayList with all output file names
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public ArrayList<String> getFileNames() throws IOException, GeneralSecurityException {
        ArrayList<String> fileNames = new ArrayList<>(); //for keeping file names
        Storage storage = getStorageService();
        /**Get all the output.txt folder contents  */
        Storage.Objects.List listObjects = storage.objects().list(programCaller.BUCKET_NAME);
        listObjects.setPrefix("output.txt/p").execute();
        Objects objects;
        do {
            objects = listObjects.execute();
            for (StorageObject object : objects.getItems()) {
                fileNames.add(object.getName()); //fill ArrayList with names of the files to download
                System.out.println(object.getName());
            }
            listObjects.setPageToken(objects.getNextPageToken());
        } while (null != objects.getNextPageToken());
        System.out.println("listObject = " + listObjects);
        return fileNames;
        /** */
    }

    public static Storage getStorageService() throws IOException, GeneralSecurityException {
        if (null == storageService) {
            GoogleCredential credential = GoogleCredential.getApplicationDefault();
            if (credential.createScopedRequired()) {
                credential = credential.createScoped(StorageScopes.all());
            }
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jf = JacksonFactory.getDefaultInstance();
            storageService = new Storage.Builder(httpTransport, jf, credential).
                    setApplicationName("DepFinder").build();

        }
        return storageService;
    }

    /**
     * Main for testing
     *
     * @param args
     */
    public static void main(String[] args) {
        Storage storage = null;
        try {
            storage = getStorageService();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            downloadToOutputStream(storage, BUCKET_NAME, CLOUDFILE_NAME, out);
        } catch (IOException e) {
            System.out.println("File does not exist or something went wrong");
            System.exit(1);
        }
        try {
            new FileDownloader().getFileNames();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(new FileDownloader().getFileNames());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        System.out.println("Downloaded " + out.toByteArray().length + " bytes");
        //Write the output stream contents into the file

        // The following two lines work only in case the downloaded files are .txt;
        // likely not the case for us
//        BufferedWriter writer = new BufferedWriter( new FileWriter( "DownloadedFile.txt"));
//        writer.write(out.toString());

        //So, use this instead (for application/octet-stream type of files):
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(CLOUDFILE_NAME);
            fileOutputStream.write(out.toByteArray());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Downloads file from cloud bucket
     *
     * @param bucketName  The name of the bucket to download the file from
     * @param storageName The name of the file on the storage
     * @return ByteArrayOutputStream with the contents of the downloaded file
     */

    public ByteArrayOutputStream downloadFile(String bucketName, String storageName) {
        Storage storage = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            storage = getStorageService();
            downloadToOutputStream(storage, bucketName, storageName, out);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            //(Probably not gonna need the next lines since this method is not called until the job is complete)
//            System.out.println("File does not exist or something went wrong. Exiting");
//            System.exit(1);
        }

        System.out.println("Downloaded " + out.toByteArray().length + " bytes");
        return out;

    }

}
