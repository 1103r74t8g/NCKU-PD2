import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TFIDFSearch {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        String textFile = args[0] + ".ser"; // corpus檔
        String tcfileName = args[1]; // 測資
        String outputFile = "output.txt";

        try {
            Indexer indexer = Indexer.loadIndex(textFile);
            TFKeywordCounter counter = indexer.getCounter();
            List<Integer> wordCounts = indexer.getWordCounts();
            int totalTexts = indexer.getTotalTexts();
            int n;

            try (BufferedReader br = new BufferedReader(new FileReader(tcfileName));
                    FileWriter writer = new FileWriter(outputFile)) {
                String line = br.readLine();
                if (line != null) {
                    n = Integer.parseInt(line.trim());
                } else {
                    return; // 没有内容
                }

                ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
                List<Future<String>> futures = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    final int finalN = n;
                    final String currentLine = line;
                    futures.add(
                            executor.submit(() -> processLine(currentLine, counter, wordCounts, totalTexts, finalN)));
                }

                for (Future<String> future : futures) {
                    writer.write(future.get());
                }

                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading index: " + e.getMessage());
        }
    }

    private static String processLine(String line, TFKeywordCounter counter, List<Integer> wordCounts, int totalTexts,
            int n) {
        StringBuilder result = new StringBuilder();
        try {
            if (line.contains("AND")) {
                List<String> keywords = extractKeywords(line, "AND");
                List<List<Integer>> listOfLists = getIndicesList(keywords, counter);
                Set<Integer> intersectionSet = getIntersectionSet(listOfLists);
                result.append(writeResults(intersectionSet, keywords, counter, wordCounts, totalTexts, n));
            } else if (line.contains("OR")) {
                List<String> keywords = extractKeywords(line, "OR");
                List<List<Integer>> listOfLists = getIndicesList(keywords, counter);
                Set<Integer> unionSet = getUnionSet(listOfLists);
                result.append(writeResults(unionSet, keywords, counter, wordCounts, totalTexts, n));
            } else {
                Set<Integer> indices = counter.searchTextIndices(line);
                result.append(
                        writeResults(indices, Collections.singletonList(line), counter, wordCounts, totalTexts, n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static List<String> extractKeywords(String line, String separator) {
        List<String> keywords = new ArrayList<>();
        String[] tokens = line.split(" ");
        for (String token : tokens) {
            if (!token.equals(separator)) {
                keywords.add(token);
            }
        }
        return keywords;
    }

    private static List<List<Integer>> getIndicesList(List<String> keywords, TFKeywordCounter counter) {
        List<List<Integer>> listOfLists = new ArrayList<>();
        for (String keyword : keywords) {
            Set<Integer> indices = counter.searchTextIndices(keyword);
            listOfLists.add(new ArrayList<>(indices));
        }
        return listOfLists;
    }

    private static Set<Integer> getIntersectionSet(List<List<Integer>> listOfLists) {
        Set<Integer> intersectionSet = new HashSet<>(listOfLists.get(0));
        for (List<Integer> list : listOfLists) {
            intersectionSet.retainAll(list);
        }
        return intersectionSet;
    }

    private static Set<Integer> getUnionSet(List<List<Integer>> listOfLists) {
        Set<Integer> unionSet = new HashSet<>();
        for (List<Integer> list : listOfLists) {
            unionSet.addAll(list);
        }
        return unionSet;
    }

    private static String writeResults(Set<Integer> resultSet, List<String> keywords, TFKeywordCounter counter,
            List<Integer> wordCounts, int totalTexts, int n) throws IOException {
        StringBuilder result = new StringBuilder();
        HashMap<Integer, Double> rank = new HashMap<>();
        for (int text : resultSet) {
            double totalTFIDF = 0.0;
            for (String keyword : keywords) {
                double TFIDF_for_keyword = TFIDF.tfIdfCalculate(text, totalTexts, keyword, counter, wordCounts);
                totalTFIDF += TFIDF_for_keyword;
            }
            rank.put(text, totalTFIDF);
        }

        PriorityQueue<Map.Entry<Integer, Double>> queue = new PriorityQueue<>(
                new Comparator<Map.Entry<Integer, Double>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                        int valueCompare = Double.compare(o2.getValue(), o1.getValue());
                        if (valueCompare == 0) {
                            return Integer.compare(o1.getKey(), o2.getKey());
                        } else {
                            return valueCompare;
                        }
                    }
                });

        queue.addAll(rank.entrySet());

        int t = 0;
        while (t < n && !queue.isEmpty()) {
            result.append(queue.poll().getKey()).append(" ");
            t++;
        }
        while (t < n) {
            result.append("-1 ");
            t++;
        }
        result.append("\n");
        return result.toString();
    }
}

class TFIDF {
    static public double tf(int doc, String term, TFKeywordCounter counter, List<Integer> wordCounts) {
        int count = counter.searchDocumentFrequency(term, doc);
        int wordCount = wordCounts.get(doc);
        return (double) count / wordCount;
    }

    static public double idf(int totalTexts, String term, TFKeywordCounter counter) {
        Set<Integer> containKeyword = counter.searchTextIndices(term);
        if (containKeyword.size() == 0) {
            return 0;
        } else {
            return Math.log((double) totalTexts / containKeyword.size());
        }
    }

    static public double tfIdfCalculate(int doc, int totalTexts, String term, TFKeywordCounter counter,
            List<Integer> wordCounts) {
        return tf(doc, term, counter, wordCounts) * idf(totalTexts, term, counter);
    }
}
