import java.io.*;
import java.util.List;

public class Indexer implements Serializable {
    private static final long serialVersionUID = 1L;
    private TFKeywordCounter counter;
    private List<Integer> wordCounts;
    private int totalTexts;

    public Indexer(TFKeywordCounter counter, int totalTexts, List<Integer> wordCounts) {
        this.counter = counter;
        this.totalTexts = totalTexts;
        this.wordCounts = wordCounts;
    }

    public void saveIndex(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        }
    }

    public static Indexer loadIndex(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Indexer) ois.readObject();
        }
    }

    public TFKeywordCounter getCounter() {
        return counter;
    }

    public List<Integer> getWordCounts() { // 新增的 getter 方法
        return wordCounts;
    }

    public int getTotalTexts() {
        return totalTexts;
    }
}
