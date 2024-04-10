package main;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MD5Brute implements Runnable {

    private boolean isFinished = false;

    private List<String> passwords;

    public MD5Brute(List<String> passwords) {
        this.passwords = passwords;
    }

    @Override
    public void run() {

        int passwordIndex = 0;

        while (!isFinished) {
            try {

                String password = passwords.get(passwordIndex);

                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(StandardCharsets.UTF_8.encode(password));
                String hashResult = String.format("%032x", new BigInteger(1, md5.digest()));

                passwordIndex++;

                //|| hashResult.equals(MainBrute.HASH_TEST)
                if (hashResult.equals(MainBrute.HASH_TARGET) ) {
                    System.out.println("Solution found in thread");
                    MainBrute.isSolutionFound = true;
                    MainBrute.solutionPw = password;
                    MainBrute.solutionHash = hashResult;
                    isFinished = true;
                } else if (passwordIndex == passwords.size()) {
                    isFinished = true;
                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}