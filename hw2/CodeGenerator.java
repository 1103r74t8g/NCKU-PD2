import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.*;
import java.util.jar.Attributes.Name;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;

import javax.naming.LinkRef;
import javax.print.DocFlavor.READER;

class ParameterFormatter {
    // 把多的空格用掉
    public static String formatParameterList(String parameterList) {

        String regex = "\\s+";
        String formattedParameters = parameterList.replaceAll(regex, " ");

        regex = ",\\s+";
        formattedParameters = formattedParameters.replaceAll(regex, ", ");

        regex = "\\(\\s+";
        formattedParameters = formattedParameters.replaceAll(regex, "(");
        regex = "\\s+\\)";
        formattedParameters = formattedParameters.replaceAll(regex, ")");
        return formattedParameters;
    }
}

class Challenge {
    // {} => :
    public static String formatClassDiagram(String input) {

        Pattern classPattern = Pattern.compile("class\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.MULTILINE);
        Matcher classMatcher = classPattern.matcher(input);

        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            String classContent = classMatcher.group(2);

            String formattedContent = formatClassContent(className, classContent);

            input = input.replace(classMatcher.group(), formattedContent);
        }

        return input;
    }

    private static String formatClassContent(String className, String classContent) {
        StringBuilder formattedContent = new StringBuilder();

        String[] lines = classContent.trim().split("\n");
        for (String line : lines) {
            formattedContent.append(className).append(" : ").append(line.trim()).append("\n");
        }

        return formattedContent.toString();
    }
}

class Parser {
    public static String splitByClass(String NameClass, String mermaidCode) {

        StringBuilder javaCode = new StringBuilder();
        mermaidCode = Challenge.formatClassDiagram(mermaidCode);
        String[] lines = mermaidCode.split("\n");

        // System.out.println(mermaidCode);
        javaCode.append("public class ").append(NameClass).append(" {\n");
        for (String line : lines) {
            line = ParameterFormatter.formatParameterList(line);
            // System.out.println(line);
            String[] parts = line.split(":");

            parts[0] = parts[0].trim();

            if (parts[0].equals(NameClass)) {
                // System.out.println("hhh " + parts[0] + " " + NameClass);
                parts[1] = parts[1].trim();

                if (parts[1].charAt(1) == ' ') {
                    parts[1] = parts[1].substring(0, 1) + parts[1].substring(2);
                }
                /*
                 * 我忘了這是什麼 抱錯的話再回來看
                 * if (parts[1].contains("(")) {
                 * parts[1] = parts[1].replaceFirst("\\s+(?=\\()", "");
                 * }
                 */
                // 符號前後空格處理
                int index = 0;
                while (index != -1) {
                    index = parts[1].indexOf("(", index);
                    if (index != -1) {
                        if (index > 0 && parts[1].charAt(index - 1) == ' ') {
                            parts[1] = parts[1].substring(0, index - 1) + parts[1].substring(index);
                        }
                        index++;
                    }
                }

                int indexSqu = 0;
                while (indexSqu != -1) {
                    indexSqu = parts[1].indexOf("[", indexSqu);
                    if (indexSqu != -1) {
                        if (indexSqu > 0 && parts[1].charAt(indexSqu - 1) == ' ') {
                            parts[1] = parts[1].substring(0, indexSqu - 1) + parts[1].substring(indexSqu);
                        }
                        indexSqu++;
                    }
                }
                int indexSpace = 0;
                while (indexSpace != -1) {
                    indexSpace = parts[1].indexOf("[", indexSpace);
                    if (indexSpace != -1) {
                        if (indexSpace > 0 && parts[1].charAt(indexSpace + 1) == ' ') {
                            parts[1] = parts[1].substring(0, indexSpace + 1) + parts[1].substring(indexSpace + 2);
                        }
                        indexSpace++;
                    }
                }
                // 逗號前面空格處理
                int indexDot = 0;
                while (indexDot != -1) {
                    indexDot = parts[1].indexOf(",", indexDot);
                    if (indexDot != -1) {
                        if (indexDot > 0 && parts[1].charAt(indexDot - 1) == ' ') {
                            parts[1] = parts[1].substring(0, indexDot - 1) + parts[1].substring(indexDot);
                        }
                        indexDot++;
                    }
                }
                // 逗號後面有沒有空格
                int indexComma = 0;
                while (indexComma != -1) {
                    indexComma = parts[1].indexOf(",", indexComma);
                    if (indexComma != -1) {
                        if (indexComma > 0 && parts[1].charAt(indexComma + 1) != ' ') {

                            parts[1] = parts[1].substring(0, indexComma + 1) + " " + parts[1].substring(indexComma + 1);
                        }
                        indexComma++;
                    }
                }
                int rightParenthesisIndex = parts[1].lastIndexOf(")");
                if (rightParenthesisIndex != -1 && rightParenthesisIndex < parts[1].length() - 1
                        && parts[1].charAt(rightParenthesisIndex + 1) != ' ') {
                    parts[1] = parts[1].substring(0, rightParenthesisIndex + 1) + " "
                            + parts[1].substring(rightParenthesisIndex + 1);
                }
                // System.out.println(parts[1]);
                String[] right;
                String wordInBra = "";
                if (parts[1].contains("(") || parts[1].contains(")")) {
                    int wordInBraFront = parts[1].indexOf("(");
                    // System.out.println(wordInBraFront);
                    int wordInBraBack = parts[1].indexOf(")");
                    // System.out.println(wordInBraBack);
                    wordInBra = parts[1].substring(wordInBraFront, wordInBraBack + 1);
                    // System.out.println(wordInBra);
                    right = parts[1].replace(wordInBra, "").split(" ");
                } else if (parts[1].contains("[") || parts[1].contains("]")) {
                    int wordInBraFront = parts[1].indexOf("[");
                    // System.out.println(wordInBraFront);
                    int wordInBraBack = parts[1].indexOf("]");
                    // System.out.println(wordInBraBack);
                    wordInBra = parts[1].substring(wordInBraFront, wordInBraBack + 1);
                    // System.out.println(wordInBra);
                    right = parts[1].replace(wordInBra, "").split(" ");
                } else {
                    right = parts[1].split(" ");
                }

                String GetorSet = "";
                if (right[0].length() >= 4) {
                    GetorSet = right[0].substring(1, 4);
                }

                if (right.length == 1) {

                    char pubOrPri = right[0].charAt(0);
                    if (pubOrPri == '+') {
                        javaCode.append("    public ");
                    } else if (pubOrPri == '-') {
                        javaCode.append("    private ");
                    }
                    if (GetorSet.equals("get") == false && GetorSet.equals("set") == false) {
                        javaCode.append("void " + right[0].substring(1) + wordInBra + " {;}\n");
                    } else if (GetorSet.equals("get")) {
                        String Return = right[0].substring(4);
                        Return = Character.toLowerCase(Return.charAt(0)) + Return.substring(1);
                        javaCode.append("void " + right[0].substring(1) + wordInBra + " {" + "\n" + "        return "
                                + Return
                                + ";\n    }" + "\n");
                    } else if (GetorSet.equals("set")) {
                        String Return = right[0].substring(4);
                        Return = Character.toLowerCase(Return.charAt(0)) + Return.substring(1);
                        javaCode.append("void " + right[0].substring(1)
                                + wordInBra + " {" + "\n" + "        this." + Return + " = " + Return
                                + ";\n    }" + "\n");
                    }

                    continue;
                }

                if (GetorSet.equals("get") == false && GetorSet.equals("set") == false) {
                    char pubOrPri = right[0].charAt(0);
                    if (pubOrPri == '+') {
                        javaCode.append("    public ");
                    } else if (pubOrPri == '-') {
                        javaCode.append("    private ");
                    }

                    if (right[1].contains("int") || right[1].contains("boolean") || right[1].contains("String")
                            || right[1]
                                    .contains("void")) {
                        String Return = ";";
                        if (right[1].contains("int")) {
                            Return = "return 0;";
                        } else if (right[1].contains("boolean")) {
                            Return = "return false;";
                        } else if (right[1].contains("String")) {
                            // System.out.println("gg");
                            Return = "return \"\";";
                        } else if (right[1].contains("void")) {

                            Return = ";";
                        }
                        javaCode.append(
                                right[1] + " " + right[0].substring(1) + wordInBra + " {" + Return + "}"
                                        + "\n");
                    } else {
                        javaCode.append(right[0].substring(1) + wordInBra + " " + right[1] + ";" + "\n");

                    }
                } else if (GetorSet.equals("get")) {
                    char pubOrPri = right[0].charAt(0);
                    if (pubOrPri == '+') {
                        javaCode.append("    public ");
                    } else if (pubOrPri == '-') {
                        javaCode.append("    private ");
                    }

                    String Return = right[0].substring(4);
                    Return = Character.toLowerCase(Return.charAt(0)) + Return.substring(1);
                    javaCode.append(
                            right[1] + " " + right[0].substring(1) + wordInBra + " {" + "\n" + "        return "
                                    + Return
                                    + ";\n    }" + "\n");
                } else if (GetorSet.equals("set")) {
                    char pubOrPri = right[0].charAt(0);
                    if (pubOrPri == '+') {
                        javaCode.append("    public ");
                    } else if (pubOrPri == '-') {
                        javaCode.append("    private ");
                    }

                    String Return = right[0].substring(4);

                    Return = Character.toLowerCase(Return.charAt(0)) + Return.substring(1);
                    javaCode.append(right[1] + " " + right[0].substring(1)
                            + wordInBra + " {" + "\n" + "        this." + Return + " = " + Return
                            + ";\n    }" + "\n");
                }
            }
        }
        javaCode.append("}\n");
        return javaCode.toString();

    }

    public static List<String> getClassName(String mermaidCode) {
        List<String> classNames = new ArrayList<>();
        String[] lines = mermaidCode.split("\n");
        for (String line : lines) {
            line = line.trim();
            line = ParameterFormatter.formatParameterList(line);
            int indexOpeningBra = line.indexOf("{");
            if (indexOpeningBra > 0 && line.charAt(indexOpeningBra - 1) != ' ') {
                // System.out.println("hhhhh");
                // 在 { 前面加一個空格
                line = line.substring(0, indexOpeningBra) + " " + line.substring(indexOpeningBra);
            }
            if (line.startsWith("class ")) {
                String className = line.split(" ")[1];
                classNames.add(className);
            }
        }
        return classNames;
    }
}

public class CodeGenerator {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("file name?");
            return;
        }
        String fileName = args[0];
        // System.out.println("File name: " + fileName);
        String mermaidCode = "";

        try {
            mermaidCode = Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            System.err.println("no file named " + fileName);
            e.printStackTrace();
            return;
        }

        try {

            List<String> className = Parser.getClassName(mermaidCode);
            // System.out.print(className);
            for (String NameClass : className) {
                String output = NameClass + ".java";

                String content = Parser.splitByClass(NameClass, mermaidCode);
                File file = new File(output);
                if (!file.exists()) {
                    file.createNewFile();
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    bw.write(content);
                }
                // System.out.println("Java class has been generated: " + output);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
