package main;

public class PermutationGenerator extends Thread{

    private int permLength;
    private char[] characters;
    private boolean isMultiPartition = false;

    public volatile boolean isWait;

    public PermutationGenerator(int permLength, char[] characters) {
        this.permLength = permLength;
        this.characters = characters;
    }

    public void run() {
        MainBrute.startBenchmark();
        populateList(permLength, "");

        if(!isMultiPartition) {
            MainBrute.stopBenchmark();
        }

        MainBrute.isPermutationComplete = true;
        MainBrute.isWait = false;
    }

    private void populateList(int depth, String stringSoFar) {
        if (depth == 0) {

            if(MainBrute.isListReset) {
                MainBrute.startBenchmark();
                MainBrute.passwords.clear();
                MainBrute.isListReset =false;
            }
            MainBrute.passwords.add(stringSoFar);

            if (MainBrute.passwords.size() >= MainBrute.MAX_LIST_SIZE) {

                System.out.println("Max passwords hit, partitioning permutations");
                MainBrute.stopBenchmark();
                isWait = true;
                isMultiPartition = true;

                MainBrute.isListReset = true;
                MainBrute.isMaxHit = true;
                MainBrute.isWait = false;

                while(isWait) {
                    //Wait for the main thread to process the pw list sofar
                }
            }

            return;
        }

        for (int i = 0; i < characters.length; i++) {
            populateList(depth - 1, stringSoFar + characters[i]);
        }
    }
}