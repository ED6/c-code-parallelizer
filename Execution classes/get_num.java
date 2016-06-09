import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
/**
 * class to test char types unicode
 */
public class get_num {
    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
        int siz = 90000;
        int temp = 0;
        while(siz != 0) {
            if (temp == 50){
                writer.println();
                temp = 0;
            }
            int i = (int) (Math.random() * 5000);
            writer.print(Integer.toString(i)+ ", ");
            temp++;
            siz--;
        }
        writer.close();
    }

}
