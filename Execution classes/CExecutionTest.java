import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by x18 on 4/20/16.
 */
public final class CExecutionTest {
    public static void Compile (){

        Process p;
        try {

            String[] cmd1 = {"rm", "executable"};
            p = Runtime.getRuntime().exec(cmd1);    //gcc code.c -o executeMe
            p.waitFor();

            String[] cmd2 = {"gcc", "Par.c", "-pthread", "-o", "executable"};
            p = Runtime.getRuntime().exec(cmd2);    //gcc code.c -o executeMe
            p.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static String exec(){
        StringBuffer output = new StringBuffer();
        String oldline = "";
        Process p;
        try {
            //System.out.println("Starting execution \n");

            output = new StringBuffer();
            String cmd3 = "./executable";

            String line = "";
            p = Runtime.getRuntime().exec(cmd3);    //gcc code.c -o executeMe
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = reader.readLine())!= null) {
                oldline = line;
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int i = 0;
        while(!Character.isDigit(oldline.charAt(i))){
            i++;
        }
        String num = "";
        while(Character.isDigit(oldline.charAt(i))){
            num = num + oldline.charAt(i);
            i++;
        }
        //System.out.println(num);
        //System.out.println("Program output: \n" + output.toString());

        return num;
        }
}