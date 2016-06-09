/**
 * Created by eon on 5/4/16.
 */
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;
import scala.Tuple3;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;


public class DepFinderNew {

    private static SparkConf conf = new SparkConf().setAppName("Dependency Finder").setMaster("local[*]");
    private static JavaSparkContext sc = new JavaSparkContext(conf);

    private static Broadcast<HashMap<Integer, ArrayList<String>>> datatableBrVar = sc.broadcast(new HashMap<>());


    public static void main(String args[]) {

        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        findIndependent(handleinput1(args[0]), handleinput2(args[1]), makeSections(Integer.valueOf(args[2])), args[3]);

    }
    private static JavaPairRDD<String,Integer> handleinput1(String path){
        JavaRDD<String> input1 = sc.textFile(path);
        JavaPairRDD<String,Integer> input1pair = input1.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String,Integer> call(String s) { return new Tuple2<>(s.split(" ")[0], Integer.valueOf(s.split(" ")[1])); }
        });
        return input1pair;
    }



    private static HashMap<Integer, ArrayList<String>> handleinput2(String path) {
        HashMap<Integer, ArrayList<String>> input2map = new HashMap<>();
        JavaRDD<String> input2 = sc.textFile(path);
        JavaRDD<Tuple2<Integer, ArrayList<String>>> input2rdd = input2.map(new Function<String, Tuple2<Integer, ArrayList<String>>>() {
            public Tuple2<Integer, ArrayList<String>> call(String s) {
                ArrayList<String> temp = new ArrayList<>(Arrays.asList(s.split(":")[1].split(" ")));
                return new Tuple2<>(Integer.valueOf(s.split(":")[0]), temp);
            }
        });
        List<Tuple2<Integer, ArrayList<String>>> input2list = input2rdd.collect();
        for (Tuple2<Integer, ArrayList<String>> elem : input2list){
            input2map.put(elem._1, elem._2);
        }
        // System.out.println(input2map);
        return input2map;
    }

    private static ArrayList<Integer> makeSections(int secnum){
        ArrayList<Integer> locsections = new ArrayList<>();
        for (int i = 1; i<secnum; i++){
            locsections.add(i);
        }
        return locsections;
    }

    private static void findIndependent(JavaPairRDD<String,Integer> datapair, HashMap<Integer, ArrayList<String>> datatable,
                                        ArrayList<Integer> sectionsList, String pathToSave) {

        JavaPairRDD<String, Iterable<Integer>> datapairgrouped = datapair.groupByKey().filter(new Function<Tuple2<String, Iterable<Integer>>, Boolean>() {
            public Boolean call(Tuple2<String, Iterable<Integer>> par) throws Exception {
                long size = par._2.spliterator().getExactSizeIfKnown(); //Java 8 thing, read more or rewrite
                return size > 1;
            }
        });
        JavaRDD<Tuple2<String, Iterable<Integer>>> datatuplegrouped = JavaRDD.fromRDD(JavaPairRDD.toRDD(datapairgrouped), datapairgrouped.classTag());

        List<Tuple2<String, Iterable<Integer>>> dtGroupedList = datatuplegrouped.collect();

        ArrayList<Tuple2<String, JavaRDD<Integer>>> forCartesian = new ArrayList<>();

        for (Tuple2<String, Iterable<Integer>> elem : dtGroupedList) {
            List<Integer> iterableAsList = new ArrayList<>();
            for (Integer item : elem._2) {
                iterableAsList.add(item);
            }

            forCartesian.add(new Tuple2<>(elem._1, sc.parallelize(iterableAsList)));
        }

        ArrayList<Tuple3<String, Integer, Integer>> commonslist = new ArrayList<>();

        for (int i = 0; i < forCartesian.size(); i++){

            //  System.out.println(forCartesian.get(i)._2.collect());


            JavaPairRDD<Integer, Integer> tempCommonsPair = forCartesian.get(i)._2.cartesian(forCartesian.get(i)._2).filter(new Function<Tuple2<Integer, Integer>, Boolean>() {
                public Boolean call(Tuple2<Integer, Integer> par) throws Exception {
                    return par._1 < par._2;
                }
            });


            JavaRDD<Tuple2<Integer, Integer>> tempCommonsRdd = JavaRDD.fromRDD(JavaPairRDD.toRDD(tempCommonsPair), tempCommonsPair.classTag());
            for (Tuple2<Integer, Integer> elem : tempCommonsRdd.collect()) {
                commonslist.add(new Tuple3<>(forCartesian.get(i)._1, elem._1, elem._2));
            }

        }

        JavaRDD<Tuple3<String, Integer, Integer>> commonsrdd = sc.parallelize(commonslist);
        // System.out.println(commonslist);

        datatableBrVar.unpersist();
        datatableBrVar = sc.broadcast(datatable);

        JavaPairRDD<Integer, Integer> dependentrdd = commonsrdd.filter(new Function<Tuple3<String, Integer, Integer>, Boolean>() {
            public Boolean call(Tuple3<String, Integer, Integer> par) throws Exception {
                String access1 = "";
                String access2 = "";
                for (int i = 0; i< datatableBrVar.value().get(par._2()).size(); i++){  //make as function
                    if (datatableBrVar.value().get(par._2()).get(i).substring(0,datatableBrVar.value().get(par._2()).get(i).length()-2).equals(par._1()))
                        access1 = datatableBrVar.value().get(par._2()).get(i).substring(datatableBrVar.value().get(par._2()).get(i).length()-1);
                }
                for (int i = 0; i< datatableBrVar.value().get(par._3()).size(); i++){
                    if (datatableBrVar.value().get(par._3()).get(i).substring(0,datatableBrVar.value().get(par._3()).get(i).length()-2).equals(par._1()))
                        access2 = datatableBrVar.value().get(par._3()).get(i).substring(datatableBrVar.value().get(par._3()).get(i).length()-1);
                }
                if (access1.equals("w") || access2.equals("w")) return true;
                return false;
            }
        }).mapToPair(new PairFunction<Tuple3<String, Integer, Integer>, Integer, Integer>() {
            public Tuple2<Integer,Integer> call(Tuple3<String, Integer, Integer> par) {
                return new Tuple2<>(par._2(), par._3());
            }
        });

        System.out.println(JavaRDD.fromRDD(JavaPairRDD.toRDD(dependentrdd), dependentrdd.classTag()).collect());

        JavaRDD<Integer> sectionsrdd = sc.parallelize(sectionsList);
        JavaPairRDD<Integer,Integer> secpairs = sectionsrdd.cartesian(sectionsrdd).filter(new Function<Tuple2<Integer, Integer>, Boolean>() {
            public Boolean call(Tuple2<Integer, Integer> par) throws Exception {
                return par._1 < par._2;
            }
        });

        // Debugging info
        System.out.println(secpairs.getNumPartitions() + " in secpairs");
        System.out.println(dependentrdd.getNumPartitions() + " in dependentrdd");
        JavaPairRDD<Integer, Integer> independentrdd = secpairs.subtract(dependentrdd);
        independentrdd.saveAsTextFile(pathToSave);

         System.out.println(independentrdd.getNumPartitions() + " in independentrdd");
         System.out.println(JavaRDD.fromRDD(JavaPairRDD.toRDD(independentrdd), independentrdd.classTag()).collect());

         sc.close();

    }

}




