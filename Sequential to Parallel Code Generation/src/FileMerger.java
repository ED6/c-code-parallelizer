import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

//Class responsible for merging the output text files into one

public class FileMerger {

    /**
     * Main for testing
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {

        //Appending the number of sections to the beginning of the first output file
        FileMerger fm = new FileMerger();
        fm.addSecNum("5");

        //Merging the files commences


//        List<Path> inputs = Arrays.asList(
//                Paths.get("output.txt/part-00000"),
//                Paths.get("output.txt/part-00001"),
//                Paths.get("output.txt/part-00002"),
//                Paths.get("output.txt/part-00003")
//        );

        ArrayList<String> fileNames = new ArrayList<>();
        fm.mergeFiles(fileNames);
    }

    /**
     * Adds the amount of sections to the first output file ("output.txt/part-00000")
     *
     * @param secNum Number of sections to append to the beginning of the file
     * @throws IOException
     */
    public void addSecNum(String secNum) throws IOException {
        BufferedReader read = new BufferedReader(new FileReader("output.txt/part-00000"));
        ArrayList list = new ArrayList();
        String dataRow = read.readLine();
        while (dataRow != null) {
            list.add(dataRow);
            dataRow = read.readLine();
        }
        FileWriter writer = new FileWriter("output.txt/part-00000"); //same as the file name above so that it will replace it
        writer.append(secNum);

        for (int i = 0; i < list.size(); i++) {
            writer.append(System.getProperty("line.separator"));
            writer.append((String) list.get(i));
        }
        writer.flush();
        writer.close();

    }

    /**
     * Merge files with passed names
     *
     * @param fileNames List of files to merge
     */
    public void mergeFiles(ArrayList<String> fileNames) {
        List<Path> inputs = new ArrayList<>();
//        FileDownloader fd = new FileDownloader();
//        Launcher launcher = new Launcher();
        //ArrayList<String> fileNames = programCaller.getOutputFileNames(); //storing names of files in output.txt directory
//        try {
//            fileNames = fd.getFileNames();
//        } catch (IOException | GeneralSecurityException e) {
//            e.printStackTrace();
//        }

        for (String fileName : fileNames) {
            inputs.add(Paths.get(fileName));
        }

        Path output = Paths.get("output.txt/merged_file");
        Charset charset = StandardCharsets.UTF_8;
        for (Path path : inputs) {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(path, charset);
                Files.write(output, lines, charset, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                //      e.printStackTrace();
                System.out.println("Processed all the files, breaking from the loop");
                /**
                 * All the files have been processed at this point - break from the loop.
                 */
                break;
            }
        }
        System.out.println("The files have been merged. Passing the merged file to Mike");
    }

}
