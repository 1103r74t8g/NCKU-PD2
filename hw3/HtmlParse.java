import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Track;

class DataReader {
    private static List<String> stockNames = new ArrayList<>();
    private static List<Double> prices = new ArrayList<>();

    public static void dataToRegular() {
        String filePath = "data.csv";
        stockNames.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseLine(String line) {
        Pattern pattern = Pattern.compile("(day\\d+)\\s+(.*)");
        // 順便問一下到時候會讀出來的格式是一樣的嗎，不然沒有dayX會出事
        // 也可以順便問為什麼會讀到title
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            // int day = Integer.parseInt(matcher.group(1).replaceAll("[^\\d]", ""));

            String[] data = matcher.group(2).split("\\s+");
            stockNames.clear();

            // 提取股票名稱和價格
            for (int i = 0; i < data.length / 2; i++) {
                stockNames.add(data[i]);
                prices.add(Double.parseDouble(data[i + data.length / 2]));
            }

        }
    }

    public static List<String> getStockNames() {
        return stockNames;
    }

    public static List<Double> getPrices() {
        return prices;
    }
}

class numberAfterDot {
    /*
     * public static String format(double number) {
     * BigDecimal bd = new BigDecimal(Double.toString(number));
     * bd = bd.setScale(2, RoundingMode.HALF_UP); 
     * return bd.stripTrailingZeros().toPlainString();
     * }
     */

    public static String format(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        String formattedNumber = decimalFormat.format(number);

        if (formattedNumber.contains(".")) {
            formattedNumber = formattedNumber.replaceAll("0*$", "").replaceAll("\\.$",
                    "");
        }
        return formattedNumber;
    }

}

class iCantUseMath {
    public static double squareRoot(double num) {
        double t;
        double sqrtroot = num / 2;
        do {
            t = sqrtroot;
            sqrtroot = (t + (num / t)) / 2;
        } while ((t - sqrtroot) != 0);
        return sqrtroot;
    }

    public static int smallerOne(int first, int second) {
        if (first <= second) {
            return first;
        } else {
            return second;
        }
    }
}

// 可能要再課一個絕對值（再問問）

class taskZero {
    public static void task(String file) {
        DataReader.dataToRegular();
        List<String> stockNames = DataReader.getStockNames();
        List<Double> prices = DataReader.getPrices();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String content = String.join(",", stockNames);
            // System.out.println(stockNames + "\n字串長度 " + stockNames.size());
            writer.write(content);
            writer.newLine();
            int count = 0;
            for (Double price : prices) {
                String formattedValue = String.format("%.2f", price);
                writer.write(formattedValue);
                count++;
                if (count % stockNames.size() == 0) {
                    writer.newLine();
                } else {
                    writer.write(",");
                }
            }
            // System.out.println("file has generated:" + file);
        } catch (IOException e) {
            System.err.println("wrong :" + e.getMessage());
        }

    }
}

class taskOne {
    public static void task(String file, String whichStock, int beginDay, int finalDay) {
        DataReader.dataToRegular();
        List<String> stockNames = DataReader.getStockNames();
        List<Double> prices = DataReader.getPrices();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(whichStock + "," + beginDay + "," + finalDay + "\n");
            int index = stockNames.indexOf(whichStock);

            int howManyTimes = finalDay - beginDay - 3;
            for (int i = 0; i < howManyTimes; i++) {
                Double averege = 0.00;
                for (int j = beginDay; j < beginDay + 5; j++) {
                    averege += prices.get(index + stockNames.size() * (j - 1));
                }
                averege /= 5;
                String averageStr = numberAfterDot.format(averege);
                writer.write(averageStr);
                if (i < howManyTimes - 1) {
                    writer.write(",");
                }
                beginDay++;
            }
            writer.newLine();
            // System.out.println("file has generated:" + file);
        } catch (IOException e) {
            System.err.println("wrong :" + e.getMessage());
        }
    }
}

class taskTwo {
    public static void task(String file, String whichStock, int beginDay, int finalDay) {
        DataReader.dataToRegular();
        List<String> stockNames = DataReader.getStockNames();
        List<Double> prices = DataReader.getPrices();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(whichStock + "," + beginDay + "," + finalDay + "\n");
            int index = stockNames.indexOf(whichStock);
            double average = 0.0;
            double sum = 0.0;
            int numPrices = 0;
            for (int i = beginDay; i <= finalDay; i++) {
                double price = prices.get(index + stockNames.size() * (i - 1));
                average += price;
                numPrices++;
            }
            average /= numPrices;

            for (int i = beginDay; i <= finalDay; i++) {
                double price = prices.get(index + stockNames.size() * (i - 1));
                sum += (price - average) * (price - average);
            }
            double variance = sum / (numPrices - 1);

            double standardDeviation = iCantUseMath.squareRoot(variance);
            String stdDevStr = numberAfterDot.format(standardDeviation);

            writer.write(stdDevStr);
            writer.newLine();
            // System.out.println("file has generated:" + file);
        } catch (IOException e) {
            System.err.println("wrong :" + e.getMessage());
        }
    }
}

class IndexedDouble implements Comparable<IndexedDouble> {
    private int index;
    private double value;

    public IndexedDouble(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(IndexedDouble other) {

        return Double.compare(this.value, other.value);
    }
}

class taskThree {
    public static void task(String file, int beginDay, int finalDay) {
        DataReader.dataToRegular();
        List<String> stockNames = DataReader.getStockNames();
        List<Double> prices = DataReader.getPrices();
        List<Double> deviation = new ArrayList<>();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

            int index = 0;
            double average = 0.0;
            double sum = 0.0;
            int numPrices = 0;
            for (int k = index; k < stockNames.size(); k++) {
                average = 0.0;
                sum = 0.0;
                numPrices = 0;
                for (int i = beginDay; i <= finalDay; i++) {
                    double price = prices.get(k + stockNames.size() * (i - 1));
                    average += price;
                    numPrices++;
                }
                average /= numPrices;

                for (int i = beginDay; i <= finalDay; i++) {
                    double price = prices.get(k + stockNames.size() * (i - 1));
                    sum += (price - average) * (price - average);
                }
                double variance = sum / (numPrices - 1); // 计算方差

                double standardDeviation = iCantUseMath.squareRoot(variance); // 计算标准差
                deviation.add(standardDeviation);
            }
            List<IndexedDouble> indexedDoubles = new ArrayList<>();
            for (int i = 0; i < deviation.size(); i++) {
                indexedDoubles.add(new IndexedDouble(i, deviation.get(i)));
            }
            Collections.sort(indexedDoubles, Collections.reverseOrder());
            for (int i = 0; i < iCantUseMath.smallerOne(indexedDoubles.size(), 3); i++) {
                IndexedDouble indexedDouble = indexedDoubles.get(i);
                int stockIndex = indexedDouble.getIndex();
                String stockName = stockNames.get(stockIndex);
                writer.write(stockName + ",");
            }
            writer.write(beginDay + "," + finalDay + "\n");
            for (int i = 0; i < iCantUseMath.smallerOne(indexedDoubles.size(), 3); i++) {
                IndexedDouble indexedDouble = indexedDoubles.get(i);
                double value = indexedDouble.getValue();
                String valueStr = numberAfterDot.format(value);
                writer.write(valueStr);
                if (i < iCantUseMath.smallerOne(indexedDoubles.size(), 3) - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            // System.out.println("file has generated:" + file);
        } catch (IOException e) {
            System.err.println("wrong :" + e.getMessage());
        }
    }
}

class taskFour {
    public static void task(String file, String whichStock, int beginDay, int finalDay) {
        DataReader.dataToRegular();
        List<String> stockNames = DataReader.getStockNames();
        List<Double> prices = DataReader.getPrices();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(whichStock + "," + beginDay + "," + finalDay + "\n");

            int index = stockNames.indexOf(whichStock);
            double sum_t = 0.0;
            double sum_Y = 0.0;
            double sum_tY = 0.0;
            double sum_tSquared = 0.0;

            for (int t = beginDay; t <= finalDay; t++) {
                double Yt = prices.get(index + stockNames.size() * (t - 1));
                sum_t += t;
                sum_Y += Yt;
                sum_tY += t * Yt;
                sum_tSquared += t * t;
            }

            double mean_t = sum_t / (finalDay - beginDay + 1);
            double mean_Y = sum_Y / (finalDay - beginDay + 1);

            double b1 = (sum_tY - (sum_t * sum_Y) / (finalDay - beginDay + 1))
                    / (sum_tSquared - (sum_t * sum_t) / (finalDay - beginDay + 1));
            //System.out.println(b1);
            BigDecimal bd = new BigDecimal(Double.toString(b1));
            bd = bd.setScale(8, RoundingMode.HALF_UP); 
            b1 = bd.doubleValue();
            //System.out.println(b1);
            double b0 = mean_Y - b1 * mean_t;
            String b1str = numberAfterDot.format(b1);
            //System.out.println(b1str);
            String b0str = numberAfterDot.format(b0);
            writer.write(b1str + "," + b0str);
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}

public class HtmlParser {

    public static void main(String[] args) {

        int mode = Integer.parseInt(args[0]);

        if (mode == 0) {
            // 確認一下爬蟲會不會自己停
            crawlMode();
            DataReader.dataToRegular();
        } else if (mode == 1) {
            int task = Integer.parseInt(args[1]);

            String fileName = "output.csv";

            if (task == 0) {
                taskZero.task(fileName);
                // System.out.println("task 0");
            }
            if (task == 1) {
                // System.out.println("task 1");
                String whichStock = args[2];
                int beginDay = Integer.parseInt(args[3]);
                int finalDay = Integer.parseInt(args[4]);
                taskOne.task(fileName, whichStock, beginDay, finalDay);
            }
            if (task == 2) {
                // System.out.println("task 2");
                String whichStock = args[2];
                int beginDay = Integer.parseInt(args[3]);
                int finalDay = Integer.parseInt(args[4]);
                taskTwo.task(fileName, whichStock, beginDay, finalDay);
            }
            if (task == 3) {
                // System.out.println("task 3");
                // String whichStock = args[2];
                int beginDay = Integer.parseInt(args[3]);
                int finalDay = Integer.parseInt(args[4]);
                taskThree.task(fileName, beginDay, finalDay);
            }
            if (task == 4) {
                // System.out.println("哭啊吸奶");
                String whichStock = args[2];
                int beginDay = Integer.parseInt(args[3]);
                int finalDay = Integer.parseInt(args[4]);
                taskFour.task(fileName, whichStock, beginDay, finalDay);
            }

        }
    }

    private static void crawlMode() {
        String url = "https://pd2-hw3.netdb.csie.ncku.edu.tw/";
        String outputPath = "data.csv";

        try {
            Document doc = Jsoup.connect(url).get();
            String pageContent = doc.text();
            appendToCsv(outputPath, pageContent);
            // System.out.println("Crawling completed. Data appended to data.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendToCsv(String outputPath, String content) {
        try (FileWriter writer = new FileWriter(outputPath, true)) {
            writer.write(content + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
