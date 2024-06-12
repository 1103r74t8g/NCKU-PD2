import java.io.*;
import java.util.*;

public class BuildIndex {
    static List<String> textList = new ArrayList<>(); // 放每五行為一文本的list
    static TFKeywordCounter counter = new TFKeywordCounter();
    static int totalTexts = 0;
    static List<Integer> wordCounts = new ArrayList<>(); // 每個文本總字數

    public static void main(String[] args) {
        // args[0]是文本路徑
        String textFile = args[0];
        parts(textFile);

        char num = textFile.charAt(textFile.indexOf("corpus") + 6);
        String outputFile = "corpus" + num + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            oos.writeObject(new Indexer(counter, totalTexts, wordCounts));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void parts(String file) {

        // StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            int wordCount = 0;
            while ((line = reader.readLine()) != null) {
                line = regularText(line);
                // sb.append(line).append(" "); // 將每一行文本加入StringBuilder，並以空格分隔
                // Insert words into Trie
                String[] words = line.split("\\s+");
                for (String word : words) {
                    counter.insert(word, totalTexts);
                }
                lineCount++;
                wordCount += words.length;
                // 每五行作為一個文本
                if (lineCount == 5) {
                    wordCounts.add(wordCount);
                    lineCount = 0; // 重置行數
                    wordCount = 0;
                    totalTexts++;
                }

            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public static String regularText(String text) {
        // 將非英文字元替換為空格
        String cleanText = text.replaceAll("[^a-zA-Z ]", " ");
        // 將所有字母轉換為小寫
        String lowercaseText = cleanText.toLowerCase();
        // 移除多餘的空格
        String trimmedText = lowercaseText.trim().replaceAll(" +", " ");
        return trimmedText;
    }

}

// Trie
class TrieNode implements Serializable {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOf = false;
    int totalCount = 0;
    Set<Integer> textIndices = new HashSet<>();
    Map<Integer, Integer> documentFrequency = new HashMap<>(); // 記錄keyword在每個文檔中的出現次數
}

class TFKeywordCounter implements Serializable {
    TrieNode root = new TrieNode();

    // 插入單詞到 Trie
    public void insert(String word, int txtIndex) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (index < 0 || index >= 26) {
                continue; // 忽略非法的字符
            }
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        // node.count++; // 每次插入都增加該單詞的計數
        node.isEndOf = true;
        node.textIndices.add(txtIndex);
        node.documentFrequency.put(txtIndex, node.documentFrequency.getOrDefault(txtIndex, 0) + 1);
    }

    public int searchDocumentFrequency(String word, int txtIndex) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return 0; // 如果 Trie 中找不到該關鍵字，返回 0
            }
            node = node.children[index];
        }
        return node.documentFrequency.getOrDefault(txtIndex, 0); // 返回該文檔中該關鍵字的出現次數
    }

    public Set<Integer> searchTextIndices(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return Collections.emptySet();
            }
            node = node.children[index];
        }
        return node.textIndices;
    }
}
