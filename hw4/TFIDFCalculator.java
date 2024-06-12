import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Queue;
import java.util.LinkedList;

public class TFIDFCalculator {
    static List<String> textList = new ArrayList<>(); // 放每五行為一文本的list

    public static void main(String[] args) {
        // long startTime = System.currentTimeMillis();

        // args[0]是文本路徑 args[1]是測資
        String txtFile = args[0];
        txtDealer.parts(txtFile);

        List<String> keyWordList = new ArrayList<>();
        List<Integer> txtList = new ArrayList<>();
        try {
            File tc = new File(args[1]);
            Scanner scanner = new Scanner(tc);

            // 讀取第一行，分割為字串
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.split(" ");
                for (String word : words) {
                    keyWordList.add(word);
                }
            }

            // 讀取第二行，轉換為整數
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] numbers = line.split(" ");
                for (String number : numbers) {
                    txtList.add(Integer.parseInt(number));
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("tc not found: " + e.getMessage());
        }
        // System.out.println("keyword List: " + keyWordList);
        // System.out.println("number List: " + txtList);
        String outputFile = "output.txt";
        try (FileWriter writer = new FileWriter(outputFile)) {
            IDFKeywordCounter counter = new IDFKeywordCounter();
            counter.insertContainKeyword(textList);
            for (int i = 0; i < keyWordList.size(); i++) {
                double tfIdf = TFIDF.tfIdfCalculate(textList.get(txtList.get(i)), textList, keyWordList.get(i),
                        counter);
                writer.write(String.format("%.5f", tfIdf) + " "); // 輸出到小數點下五位
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // long endTime = System.currentTimeMillis();
        // long totalTime = endTime - startTime;
        // System.out.println("程式運行時間：" + totalTime + " 毫秒");

    }
}

// 1.將所有非英文字元以空白代替。
// 2.將所有英文大寫轉換成小寫。
// 3.以空白進行詞彙的segmentation。
class txtDealer {
    public static String regularText(String text) {
        // 將非英文字元替換為空格
        String cleanText = text.replaceAll("[^a-zA-Z ]", " ");
        // 將所有字母轉換為小寫
        String lowercaseText = cleanText.toLowerCase();
        // 移除多餘的空格
        String trimmedText = lowercaseText.trim().replaceAll(" +", " ");
        return trimmedText;
    }

    public static void parts(String file) {

        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                line = regularText(line);
                sb.append(line).append(" "); // 將每一行文本加入StringBuilder，並以空格分隔
                lineCount++;

                // 每五行作為一個文本
                if (lineCount == 5) {
                    TFIDFCalculator.textList.add(sb.toString().trim()); // 將組合好的文本加入List
                    sb.setLength(0); // 清空StringBuilder
                    lineCount = 0; // 重置行數
                }
            }

            // 如果文本不足五行，將剩餘的文本加入List，應該是不會用到
            if (lineCount > 0) {
                System.out.println("出事了阿伯");
                TFIDFCalculator.textList.add(sb.toString().trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}

class TFIDF {

    private static int countWords(String sentence) {
        // 將句子按空格分割成單詞，並計算單詞數
        String[] words = sentence.split("\\s+");
        return words.length;
    }

    static public double tf(String doc, String term) {
        TFKeywordCounter counter = new TFKeywordCounter();
        // term在doc出現次數

        String[] docWord = doc.split(" ");
        for (String word : docWord) {
            counter.insert(word);
        }
        int count = counter.search(term);

        int wordCount = countWords(doc);

        // System.out.println(count);
        // System.out.println(wordCount);
        return (double) count / wordCount;
    }

    static public double idf(List<String> docs, String term, IDFKeywordCounter counter) {

        // 含term的文本數量

        int txt_contain_term = counter.searchContainKeyword(term);

        // System.out.println(docs.size());
        // System.out.println("----------");
        // System.out.println(txt_contain_term);
        return Math.log((double) docs.size() / txt_contain_term);
    }

    static public double tfIdfCalculate(String doc, List<String> docs, String term, IDFKeywordCounter counter) {
        // System.out.println("------");
        // System.out.println(tf(doc, term) * idf(docs, term));
        // System.out.println("------");
        return tf(doc, term) * idf(docs, term, counter);
    }

}

// Trie
class TrieNode {
    TrieNode[] children = new TrieNode[26];
    int count = 0;
    boolean isEndOf = false;
    int totalCount = 0;
}

class TFKeywordCounter {
    TrieNode root = new TrieNode();

    // 插入單詞到 Trie
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.count++; // 每次插入都增加該單詞的計數
        node.isEndOf = true;
    }

    // 搜尋關鍵字在 Trie 中的出現次數
    public int search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return 0; // 如果 Trie 中找不到該關鍵字，返回 0
            }
            node = node.children[index];
        }
        return node.count; // 返回該關鍵字的計數
    }
}

class IDFKeywordCounter {
    TrieNode root = new TrieNode();

    // 得到每個關鍵字占幾個文本
    public void insertContainKeyword(List<String> textList) {
        for (String text : textList) {

            TrieNode node = root; // 初始化為根節點

            String[] words = text.split(" ");
            // 每個文本
            for (String word : words) {
                node = root;
                // 每個字
                for (char c : word.toCharArray()) {
                    int index = c - 'a';
                    if (node.children[index] == null) {
                        node.children[index] = new TrieNode();
                    }
                    node = node.children[index];
                }
                if (!node.isEndOf) {
                    node.totalCount++;
                }
                node.isEndOf = true;
            }
            // 重新把每個end設為false，下一個文本進來的時候就可以再測一次是否包含keyword
            for (String word : words) {
                node = root;
                // 每個字
                for (char c : word.toCharArray()) {
                    int index = c - 'a';
                    if (node.children[index] == null) {
                        node.children[index] = new TrieNode();
                    }
                    node = node.children[index];
                }
                node.isEndOf = false;

            }
        }
    }

    // 搜尋是否包含關鍵字
    public int searchContainKeyword(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return 0; // 如果 Trie 中找不到該關鍵字，返回 0
            }
            node = node.children[index];
        }
        return node.totalCount; // 返回該關鍵字的計數
    }

}
