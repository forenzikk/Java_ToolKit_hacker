package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainBrute {

    // DEFAULT ----------------------------------------------------------------|

    // More than 25 mil passwords makes memory go bye bye.
    protected static final int MAX_LIST_SIZE = 30000;

    protected volatile static List<String> passwords;
    private static double benchMarkStart;

    // THREAD STUFF -----------------------------------------------------------|

    protected volatile static boolean isSolutionFound = false;
    protected volatile static boolean isWait;
    protected volatile static boolean isPermutationComplete;
    protected volatile static boolean isListReset;
    protected volatile static boolean isMaxHit;

    protected volatile static String solutionPw;
    protected volatile static String solutionHash;

    private static Thread cmdWaitingThread;

    // CONFIG -----------------------------------------------------------------|

    public volatile static String HASH_TARGET = "5f4dcc3b5aa765d61d8327deb882cf99";
    //public volatile static String HASH_TEST = "827ccb0eea8a706c4c34a16891f84e7b";

    //	public static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\\\|;:\\'\\\",<.>/?";
    public static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
//	public static final String CHAR_SET = "abcdefghijklmnopqrstuvwxyz0123456789";
//	public static final String CHAR_SET = "0123456789";

    public static final int MIN_LENGTH = 9;
    public static final int MAX_LENGTH = 9;
    public static final String PASSWORD_FILES = "list";

    public static final boolean USE_PASSWORD_FILES = true;
    public static final boolean USE_PERMUTATIONS = true;

    public static final boolean IS_MAX_THREADS = false;

    public static final int MAX_THREADS = 16;

    // ------------------------------------------------------------------------------------------------------------------|
    // ------------------------------------------------------------------------------------------------------------------|

    public static void main(String[] args) {
        System.out.println(CHAR_SET.length());

        if (USE_PASSWORD_FILES) {
            readFilesIntoPasswordList();

            startBruteThreads();

            if (isSolutionFound) {
                return;
            }
        }

        if (USE_PERMUTATIONS) {
            performPermutationCheck();
        }
    }

    private static void startBruteThreads() {
        System.out.println("Setting up threads");

        int threads = getThreadCount();
        List<MD5Brute> bruteList = new ArrayList<MD5Brute>();
        Thread[] threadArray = new Thread[threads];

        // Split the arraylist into parts for every threads
        int partitionSize = (int) Math.ceil(passwords.size() / threads);
        int partitionIndex = 0;

        System.out.println("Partitioning passwords for threads");

        for (int i = 1; i <= threads; i++) {

            List<String> partition = passwords.subList(partitionIndex,
                    Math.min(partitionSize * i, passwords.size() - 1));
            System.out.println("Partition size : " + partition.size() + " Thread : " + i);

            MD5Brute brute = new MD5Brute(partition);

            bruteList.add(brute);
            partitionIndex = partitionSize * i;
        }

        System.out.println("Starting threads");
        startBenchmark();

        for (int i = 0; i < threadArray.length; i++) {
            Thread t = new Thread(bruteList.get(i));
            threadArray[i] = t;
            t.start();
        }

        waitForThreadsToFinish(threadArray);
        checkForSolution();
        stopBenchmark();
    }

    /**
     * Waits for every thread in an array to die. Blocks execution
     *
     * @param threads
     *            The threads to wait for
     */
    private static void waitForThreadsToFinish(Thread[] threads) {
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkForSolution() {
        if (isSolutionFound) {
            System.out.println("-------------------------------------------|");
            System.out.println("-|SOLUTION FOUND ");
            System.out.println("-|-----------------------------------------|");
            System.out.println("-|PASSWORD: " + solutionPw);
            System.out.println("-|-----------------------------------------|");
            System.out.println("-|HASH FOUND: " + solutionHash);
            System.out.println("-|-----------------------------------------|");
        } else {
            System.out.println("No solution found");
        }
    }

    /**
     * Reads all text files from /src/passwords and adds them to the pw list
     */
    private static void readFilesIntoPasswordList() {
        System.out.println("Reading files into password list");

        passwords = new ArrayList<String>();

        startWaitingThread();
        startBenchmark();

        try {
            File f = new File(PASSWORD_FILES);

            FilenameFilter textFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".txt");
                }
            };

            File[] files = f.listFiles(textFilter);

            for (File file : files) {
                if (file.isDirectory()) {
                    // do nothing
                } else {
                    String line = null;
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    while ((line = br.readLine()) != null) {
                        passwords.add(line);
                    }
                    System.out.println("Added file: " + file.getName());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopWaitingThread();
        System.out.println("Reading files completed");
        stopBenchmark();
        System.out.println(passwords.size() + " passwords generated");
    }

    /**
     *
     * @return
     */
    private static void performPermutationCheck() {
        System.out.println("Generating password permutations");

        passwords = new ArrayList<>();
        int permLength = MIN_LENGTH;
        char[] characters = CHAR_SET.toCharArray();
        isPermutationComplete = false;
        isWait = false;
        isListReset = false;

        while (permLength <= MAX_LENGTH) {
            System.out.println("Generating permutations of length " + permLength);

            int partitionCount = 0;

            PermutationGenerator generator = new PermutationGenerator(permLength, characters);
            generator.start();

            while (!isPermutationComplete) {

                isMaxHit = false;
                isWait = true;
                generator.isWait = false;
                while (isWait) {
                    // Wait for the generator to complete / be done with one partition
                }

                if(isMaxHit) {
                    partitionCount++;

                    System.out.println("Partition " + partitionCount +  " created for length " + permLength);
                } else {
                    System.out.println("Permutations for length " + permLength + " completed");
                }

                System.out.println("Generated " + passwords.size() + " passwords");

                startBruteThreads();

                if (isSolutionFound) {
                    //Solution found, stop the generator and this method
                    isPermutationComplete = true;
                    generator.isWait = false;

                    return;
                }

                //Clear password list
                passwords.clear();
            }

            isPermutationComplete = false;
            permLength++;
        }
    }

    /**
     * Return how many processors the machine has
     *
     * @return
     */
    private static int getThreadCount() {
        int threads = IS_MAX_THREADS ? MAX_THREADS : Runtime.getRuntime().availableProcessors();
        System.out.println("Total threads available: " + threads);
        return threads;
    }

    /**
     * A Threads that displays stuff while the user is waiting for progress
     */
    public static void startWaitingThread() {
        cmdWaitingThread = new Thread() {
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        System.out.print(".");
                        this.sleep(50);
                        System.out.print(".");
                        this.sleep(50);
                        System.out.print(".");
                        this.sleep(50);
                        System.out.println("");
                    }
                } catch (InterruptedException e) {
                    return;
                }

            }
        };
        cmdWaitingThread.start();
    }

    public static void stopWaitingThread() {
        cmdWaitingThread.interrupt();
    }

    /**
     * Sets the start time of a benchmark.
     */
    protected static void startBenchmark() {
        benchMarkStart = System.currentTimeMillis();
    }

    /*
     * Gets the difference to the time of the last benchmark start timestamp and
     * prints it.
     */
    protected static void stopBenchmark() {
        double elapsedTimeMillis = System.currentTimeMillis() - benchMarkStart;
        System.out.println(elapsedTimeMillis / 1000F + "s");
    }
}