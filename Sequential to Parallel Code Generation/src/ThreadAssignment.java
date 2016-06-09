import java.util.ArrayList;

public class ThreadAssignment {
    ArrayList<ArrayList<Integer>> ThreadDistribute(ArrayList<ArrayList<Integer>> AccesibilityChecked) {
        int ThreadNum = Runtime.getRuntime().availableProcessors(); //thread num = amount of cores
        if(ThreadNum <= 1){
            ThreadNum = 2;
        }
        System.out.println("Amount of processors available: " + ThreadNum);
        for (ArrayList<Integer> a : AccesibilityChecked) {
            System.out.println(a);
        }
        System.out.println();

        //Generate the external linking coefficients:
        ArrayList<Integer> extCoef = new ArrayList<Integer>();

        int i = 1;
        while (i <= AccesibilityChecked.size()) {
            extCoef.add(0);
            for (ArrayList<Integer> a : AccesibilityChecked) {
                if (a.contains(i)) {
                    extCoef.set(i - 1, extCoef.get(i - 1) + 1);
                }
            }
            i++;
        }
        System.out.println("External coefficients ordered by Section number:\n" + extCoef);


        //Generate list of index ordered by external coefficients
        ArrayList<Integer> tempExtCoef;//= new ArrayList<Integer>();
        tempExtCoef = (ArrayList) extCoef.clone();
        ArrayList<String> order = new ArrayList<>();
        int min = AccesibilityChecked.size() + 1;
        ArrayList<Integer> GlobalMinIndex = new ArrayList<Integer>();
        ArrayList<Integer> LocalMinIndex = new ArrayList<Integer>();
        ArrayList<Integer> tempLocalMinIndex = new ArrayList<Integer>();
        i = 0;

        while (i < tempExtCoef.size()) {
            if (tempExtCoef.get(i) < min) {
                min = tempExtCoef.get(i);
                LocalMinIndex.clear();
                LocalMinIndex.add(i);
            } else if (tempExtCoef.get(i).equals(min)) {
                LocalMinIndex.add(i);
            }
            i++;
            if (i == tempExtCoef.size() && min != AccesibilityChecked.size() + 1) {
                min = AccesibilityChecked.size() + 1;
                //remove them from local list in order of best parent!

                tempLocalMinIndex = (ArrayList) LocalMinIndex.clone();
                while (!tempLocalMinIndex.isEmpty()) {
                    int max = 0;
                    int maxIndex = 0;
                    int j = 0;
                    for (Integer iter : tempLocalMinIndex) {
                        if (AccesibilityChecked.get(iter).size() > max) {
                            maxIndex = j;
                            max = AccesibilityChecked.get(iter).size();
                        }
                        j++;
                    }
                    GlobalMinIndex.add(tempLocalMinIndex.get(maxIndex));
                    tempLocalMinIndex.remove(maxIndex);
                }


                for (Integer LocalIndex : LocalMinIndex) {
                    tempExtCoef.set(LocalIndex, AccesibilityChecked.size() + 2);
                }
                i = 0;
            }
        }
        System.out.println("Ordered List of Sections by external coefficients:\n" + GlobalMinIndex);
        int MinimumParentNum = (extCoef.size() + 1) / ThreadNum;
        System.out.println("Minimum Pearent Threads: " + MinimumParentNum);
        ArrayList<Integer> parentThreads = new ArrayList<Integer>();

        //generate parent threads first from unconnectable threads (extCoef = 0)
        i = 0;
        while (i < GlobalMinIndex.size() && extCoef.get(GlobalMinIndex.get(i)) == 0) {
            parentThreads.add(GlobalMinIndex.get(i));
            i++;
        }

        //remove 0 index from GlobalMin
        int tempIndex = 0;
        while (tempIndex < parentThreads.size()) {
            GlobalMinIndex.remove(0);
            tempIndex++;
        }
        //System.out.println("Threads left after 0s removed: " + GlobalMinIndex);
        //now pad it with least movable threads if not already full
        //prioritize external factor, then internal factor(how good a parent it will be/interconnectivity)
        while (parentThreads.size() < MinimumParentNum) {
            //remove parent from all of the lists exept its own in Accessibility checked
            parentThreads.add(GlobalMinIndex.get(0));
            for (ArrayList<Integer> list : AccesibilityChecked) {
                if (list.contains(GlobalMinIndex.get(0) + 1)) {
                    list.remove(list.indexOf(GlobalMinIndex.get(0) + 1));
                }
            }
            GlobalMinIndex.remove(0);

        }


        for (ArrayList<Integer> a : AccesibilityChecked) {
            System.out.println(a);
        }
        System.out.println();
        parentThreads = MergeSort.MergeSort(parentThreads);
        System.out.println("Threads assigned as parents:\n" + parentThreads);
        System.out.println("unasigned still: " + GlobalMinIndex);
        //order parent threads into ThreadExecutionTable, and fix format into 2D
        ArrayList<ArrayList<Integer>> ThreadExecutionTable = MergeSort.FormatFix(MergeSort.MergeSort(parentThreads));
        System.out.println("Execution table only parents: " + ThreadExecutionTable);

        //now try to assign the remaining threads into parents as padding starting form index i carried out form last part!
        while (!GlobalMinIndex.isEmpty()) {
            int currentIndex = GlobalMinIndex.get(0);
            ArrayList<Integer> posibleParallelizations = new ArrayList<>();
            //find posible parents to parralelize with
            for (Integer iter : parentThreads) {
                if (AccesibilityChecked.get(iter).contains(currentIndex + 1)) {
                    posibleParallelizations.add(iter);
                }
            }
            //add as parent if no posible parallelizations
            if (posibleParallelizations.isEmpty()) {
                parentThreads.add(currentIndex);
                parentThreads = MergeSort.MergeSort(parentThreads);
                ThreadExecutionTable = MergeSort.addParent(ThreadExecutionTable, currentIndex);
                for (ArrayList<Integer> list : AccesibilityChecked) {
                    if (list.contains(GlobalMinIndex.get(0) + 1)) {
                        list.remove(list.indexOf(GlobalMinIndex.get(0) + 1));
                    }
                }
                GlobalMinIndex.remove(0);
            }
            /* Two approaches possible: ensure even distribution of threads, at the cost of potential forced parents
            or ensure minimum parents (maximum parallelism + overhead) over even distribution
             */

            //find worst parent(least posible parralelizations) option
            else {
                //filter out the ones which already have maximum threads
                for (int k = 0; k < posibleParallelizations.size(); k++) {
                    if (ThreadExecutionTable.get(parentThreads.indexOf(posibleParallelizations.get(k))).size() == ThreadNum) {
                        posibleParallelizations.remove(posibleParallelizations.indexOf(posibleParallelizations.get(k)));
                        k--;
                    }
                }
                if(posibleParallelizations.isEmpty()){
                    parentThreads.add(currentIndex);
                    parentThreads = MergeSort.MergeSort(parentThreads);
                    ThreadExecutionTable = MergeSort.addParent(ThreadExecutionTable, currentIndex);
                    for (ArrayList<Integer> list : AccesibilityChecked) {
                        if (list.contains(GlobalMinIndex.get(0) + 1)) {
                            list.remove(list.indexOf(GlobalMinIndex.get(0) + 1));
                        }
                    }
                    GlobalMinIndex.remove(0);
                }
                else {
                    //get worst parents in list and add it to its execution list
                    min = AccesibilityChecked.get(posibleParallelizations.get(0)).size();
                    ArrayList<Integer> temp = new ArrayList<>();
                    for (Integer iter : posibleParallelizations) {
                        if (AccesibilityChecked.get(iter).size() < min) {
                            temp.clear();
                            temp.add(iter);
                        } else if (AccesibilityChecked.get(iter).size() == min) {
                            temp.add(iter);
                        }
                    }
                    posibleParallelizations = (ArrayList) temp.clone();
                    //now get the least connected one
                    min = ThreadNum;
                    tempIndex = 0;
                    for (Integer iter : posibleParallelizations) {
                        if (ThreadExecutionTable.get(parentThreads.indexOf(iter)).size() < min) {
                            min = ThreadExecutionTable.get(parentThreads.indexOf(iter)).size();
                            tempIndex = parentThreads.indexOf(iter);
                        }
                    }

                    //add to its queue and remove form accesibility checker and GlobalMinIndex
                    ThreadExecutionTable.get(tempIndex).add(GlobalMinIndex.get(0));
                    for (ArrayList<Integer> list : AccesibilityChecked) {
                        if (list.contains(GlobalMinIndex.get(0) + 1)) {
                            list.remove(list.indexOf(GlobalMinIndex.get(0) + 1));
                        }
                    }
                    GlobalMinIndex.remove(0);
                }
            }
        }
        for (ArrayList<Integer> arr : ThreadExecutionTable) {
            for (int k = 0; k < arr.size(); k++) {
                arr.set(k, arr.get(k) + 1);
            }
        }
        System.out.println(ThreadExecutionTable);
        return ThreadExecutionTable;
    }
}
