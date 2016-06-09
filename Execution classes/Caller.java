import java.math.BigInteger;

public class Caller {
    public static void main(String[] args) throws InterruptedException {
        CExecutionTest.Compile();
        BigInteger time = new BigInteger("0");
        BigInteger upperLimit = new BigInteger(CExecutionTest.exec());
        upperLimit = upperLimit.multiply(new BigInteger("5"));
        BigInteger result;
        int iter = 1000;
        int i = 0;
        while(i < iter){
            result = new BigInteger(CExecutionTest.exec());

                time = time.add(result);
                i++;

        }
        time = time.divide(new BigInteger(Integer.toString(iter)));
        System.out.println("\nAverage:");
        System.out.println(time.toString());
    }
}