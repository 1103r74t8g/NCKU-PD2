import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RegExp {
    public static void main(String[] args) {
        String str1 = args[1];
        String str2 = args[2];
        int s2Count = Integer.parseInt(args[3]);

        // For your testing of input correctness
        /*
         * System.out.println("The input file:" + args[0]);
         * System.out.println("str1=" + str1);
         * System.out.println("str2=" + str2);
         * System.out.println("num of repeated requests of str2 = " + s2Count);
         */

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {

                System.out.print(isPalindrome(line) ? "Y," : "N,");
                System.out.print(containsSubstring(line, str1) ? "Y," : "N,");
                System.out.print(containsSubstringNTimes(line, str2, s2Count) ? "Y," : "N,");
                System.out.println(containsPattern(line) ? "Y" : "N");

                // System.out.println(line);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPalindrome(String str) {
        int left = 0;
        int right = str.length() - 1;
        while (left < right) {
            if (Character.toLowerCase(str.charAt(left++)) != Character.toLowerCase(str.charAt(right--))) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsSubstring(String str, String subStr) {
        if (str.length() == 0) {
            return false;
        }
        for (int i = 0; i <= str.length() - subStr.length(); i++) {
            int j = 0;
            for (j = 0; j < subStr.length(); j++) {
                if (Character.toLowerCase(str.charAt(i + j)) != Character.toLowerCase(subStr.charAt(j))) {
                    break;
                }
            }
            if (j == subStr.length()) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSubstringNTimes(String str, String subStr, int n) {
        int count = 0;
        boolean contain = false;
        if (str.length() == 0) {
            return false;
        }
        for (int i = 0; i <= str.length() - subStr.length(); i++) {
            int j = 0;
            for (j = 0; j < subStr.length(); j++) {
                contain = true;
                if (Character.toLowerCase(str.charAt(i + j)) != Character.toLowerCase(subStr.charAt(j))) {
                    contain = false;
                }
            }
            if (contain == true) {
                count++;
            }
        }
        if (count >= n) {
            return true;
        }
        return false;
    }

    public static boolean containsPattern(String str) {
        int m;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (str.charAt(i) == 'a' || str.charAt(i) == Character.toUpperCase('a')) {
                int j = i + 1;
                int countA = 1;
                while (j < length && (str.charAt(j) == 'a' || str.charAt(j) == Character.toUpperCase('a'))) {
                    countA++;
                    j++;
                }
                m = countA;
                int countB = 0;
                while (j < length) {
                    if ((str.charAt(j) == 'b' || str.charAt(j) == Character.toUpperCase('b'))) {
                        countB++;
                        j++;
                        if (countB >= 2 * m) {
                            return true;
                        }
                    } else {
                        j++;
                        countB = 0;
                    }
                }
            }
        }
        return false;
    }
}
