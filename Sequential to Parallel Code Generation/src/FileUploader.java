import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;

import java.io.*;
import java.security.GeneralSecurityException;

//Class responsible for uploading output files from RWRec to Cloud Storage for processing by Google Cloud Dataproc

public class FileUploader {

    private static final String BUCKET_NAME = "ed6-cute-bucket"; //"our-bucket-name" without the "gs://" prefix
    private static final String OBJECT_NAME = "datatable.txt"; //name of the file that will appear in the storage
    private static final String FILE_NAME = "/home/eon/Desktop/datatable.txt"; //path to the file we want to upload
    private static Storage storageService;

    public static StorageObject uploadSimple(Storage storage, String bucketName, String objectName,
                                             File data) throws FileNotFoundException, IOException {
        return uploadSimple(storage, bucketName, objectName, new FileInputStream(data),
                "text/plain"); //or "application/octet-stream" / "application/x-java-archive" (upload file type here)
    }

    public static StorageObject uploadSimple(Storage storage, String bucketName, String objectName,
                                             InputStream data, String contentType) throws IOException {
        InputStreamContent mediaContent = new InputStreamContent(contentType, data);
        Storage.Objects.Insert insertObject = storage.objects().insert(bucketName, null, mediaContent)
                .setName(objectName);
        // The media uploader gzips content by default, and alters the Content-Encoding accordingly.
        // GCS dutifully stores content as-uploaded. This line disables the media uploader behavior,
        // so the service stores exactly what is in the InputStream, without transformation.
        insertObject.getMediaHttpUploader().setDisableGZipContent(true);
        return insertObject.execute();
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
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        Storage storage = getStorageService();
        StorageObject object = uploadSimple(storage, BUCKET_NAME, OBJECT_NAME, new File(FILE_NAME));
        System.out.println("Uploaded file " + object.getName() + " (size: " + object.getSize() + ")");
    }


    /**
     * Uploads file to cloud bucket
     *
     * @param bucketName    The name of the bucket to upload the file to
     * @param storageName   The name of the file that will appear on the storage
     * @param localFilePath The file to the file to upload on the local system
     */
    public void uploadFile(String bucketName, String storageName, String localFilePath) {
        try {
            Storage storage = getStorageService();
            StorageObject object = uploadSimple(storage, bucketName, storageName, new File(localFilePath));
            //Storage.Objects.Insert insertObject = storage.objects().insert(bucketName, null, mediaContent)
            //        .setName(objectName);
//            /**
//             * Delete an output.txt folder (if exists)
//             */
//            storage.objects().delete("ed6-cute-bucket", "output.txt").execute();
            System.out.println("Uploaded file " + object.getName() + " (size: " + object.getSize() + ")");
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

}
