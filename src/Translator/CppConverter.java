package Translator;

import java.io.*;
import java.util.*;

public class CppConverter {
    private File Cpp;

    private static HashMap<String, String> commandMap;
    private int tabAmount = 0;
    private String convertedText;

    private final Vector<String> indentKeywords = new Vector<>(Arrays.asList("if", "else", "for", "while", "def"));

    public CppConverter(File ArduinoFile) {
        this.Cpp = ArduinoFile;
        def();
    }

    public CppConverter() {
        def();
    }

    private void def() {
        commandMap = new HashMap<>();

        // Core structure
        commandMap.put("void setup()", "def setup()");
        commandMap.put("void loop()", "def loop()");
        commandMap.put("void", "def");

        // Serial Functions
        commandMap.put("Serial.println", "serialprintln");
        commandMap.put("Serial.print", "serialprint");
        commandMap.put("Serial.write", "serialwrite");
        commandMap.put("Serial.writeln", "serialwriteln");
        commandMap.put("Serial.begin", "serialbegin");

        // Timing
        commandMap.put("delay", "delay");
        commandMap.put("delayMicroseconds", "delaymicroseconds");

        // Pin control
        commandMap.put("pinMode", "pinmode");
        commandMap.put("digitalWrite", "digitalwrite");
        commandMap.put("analogWrite", "analogwrite");
        commandMap.put("digitalRead", "digitalread");
        commandMap.put("analogRead", "analogread");

        // Random & Seed
        commandMap.put("randomSeed", "randomseed");
        commandMap.put("pulseIn", "pulsein");
        commandMap.put("random", "random");

        // Math Functions
        commandMap.put("constrain", "constrain");
        commandMap.put("map", "map");
        commandMap.put("max", "max");
        commandMap.put("min", "min");
        commandMap.put("sqrt", "sqrt");
    }

    public void Convert() throws IOException {
        if(convertedText.isEmpty()) {
            convertedText = readFile();
        }

        convertedText = convertedText.replace("//", "#").replace("/*", "#").replace("*", "#");

        while(TabsFilterNeeded(convertedText)) {
            tabAmount = 0;
            convertedText = ApplyTabs(convertedText);
        }

        convertedText = ApplyCommandMap(convertedText);

        convertedText = VariableFilter(convertedText);

        convertedText = FinalFilter(convertedText);
    }

    public String readFile() throws IOException {
        BufferedReader BR = new BufferedReader(new FileReader(Cpp));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = BR.readLine()) != null) {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    private String ApplyTabs(String text){
        StringBuilder builder = new StringBuilder();
        for(String line : text.lines().toList()){
            line = line.strip();
            if(line.contains("#")){
                if(line.startsWith("#")){
                    builder.append("\t".repeat(Math.max(0, tabAmount)));
                    builder.append(line);
                    builder.append('\n');
                }
                else {
                    builder.append("\t".repeat(Math.max(0, tabAmount)));
                    builder.append(line.split("#")[0].strip()).append("\n");
                    builder.append("\t".repeat(Math.max(0, tabAmount)));
                    builder.append("#").append(line.split("#")[1].strip()).append("\n");
                }
            }
            else if(line.endsWith(";") && !line.contains("{"))
            {
                builder.append("\t".repeat(Math.max(0, tabAmount)));
                builder.append(line).append('\n');
            }
            else if(line.contains("[]") && !line.contains("for")){
                builder.append("\t".repeat(Math.max(0, tabAmount)));
                builder.append(line).append('\n');
            }
            else{
                if (line.endsWith("}") && !line.contains("{")) {
                    tabAmount--;
                    if (line.equals("}")) {
                        builder.append("}").append('\n');
                    }
                    else {
                        String withoutLast = line.substring(0, line.length() - 1);
                        builder.append(withoutLast).append('\n');
                        builder.append("}").append('\n');
                    }
                }
                else if (!line.endsWith("}") && line.contains("{")) {
                    int idxOpen = line.indexOf("{");
                    if (line.equals("{")) {
                        builder.append("\t".repeat(Math.max(0, tabAmount)));
                        builder.append("{").append('\n');
                    }
                    else if(line.startsWith("}")){
                        builder.append("\t".repeat(Math.max(0, tabAmount)));
                        builder.append("}").append("\n").append(line.replaceFirst("}", "")).append("\n");
                    }
                    else {
                        String beforeBrace = line.substring(0, idxOpen);
                        String afterBrace = line.substring(idxOpen + 1);
                        builder.append("\t".repeat(Math.max(0, tabAmount)));
                        builder.append(beforeBrace).append('\n');
                        builder.append("{").append('\n');
                        if (!afterBrace.isBlank()) {
                            builder.append(afterBrace).append('\n');
                        }
                    }
                    tabAmount++;
                }
                else if (line.contains("{") && line.endsWith("}")) {
                    int idxOpen = line.indexOf("{");
                    String beforeBrace = line.substring(0, idxOpen);
                    String middle = line.substring(idxOpen + 1, line.length() - 1);
                    builder.append("\t".repeat(Math.max(0, tabAmount)));
                    builder.append(beforeBrace).append('\n');
                    builder.append("{").append('\n');
                    if (!middle.isBlank()) {
                        builder.append("\t".repeat(Math.max(0, tabAmount + 1)));
                        builder.append(middle).append('\n');
                    }
                    builder.append("\t".repeat(Math.max(0, tabAmount)));
                    builder.append("}").append('\n');
                }
                else {
                    builder.append("\t".repeat(Math.max(0, tabAmount)));
                    builder.append(line).append('\n');
                }
            }
        }
        return builder.toString();
    }

    private boolean TabsFilterNeeded(String text){
        for(String line : text.lines().toList()) {
            line = line.strip();
            if(!line.endsWith(";") && !line.strip().startsWith("#")) {
                if (line.contains("{") && !line.equals("{")) {
                    return true;
                }
                if (line.contains("}") && !line.equals("}")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String ApplyCommandMap(String text){
        StringBuilder builder = new StringBuilder();
        for(String line : text.lines().toList()){
            if(!line.contains("[]")) {
                if (!line.contains("{") && !line.contains("}")) {
                    builder.append(applyMapToSubstrings(line)).append('\n');
                }
            }
            else {
                builder.append(applyMapToSubstrings(line)).append('\n');
            }
        }
        return builder.toString();
    }

    private static String applyMapToSubstrings(String input) {
        boolean didReplace;
        boolean sameReplacement = false;

        do {
            didReplace = false;
            int length = input.length();
            if(!sameReplacement) {
                for (int a = length; a > 0; a--) {
                    for (int start = 0; start <= length - a; start++) {
                        StringBuilder chunk = new StringBuilder();
                        for (int b = 0; b < a; b++) {
                            chunk.append(input.charAt(start + b));
                        }

                        if (commandMap.get(chunk.toString()) != null) {
                            String replacement = commandMap.get(chunk.toString());

                            if (replacement.contentEquals(chunk)) {
                                sameReplacement = true;
                            }
                            StringBuilder newInput = new StringBuilder();

                            for (int c = 0; c < start; c++) {
                                newInput.append(input.charAt(c));
                            }

                            newInput.append(replacement);

                            for (int d = start + a; d < length; d++) {
                                newInput.append(input.charAt(d));
                            }

                            input = newInput.toString();
                            didReplace = true;
                            a = 0;
                            break;
                        }
                    }
                }
            }
        } while (didReplace);

        return input;
    }

    private String VariableFilter(String Text) {
        Text = Text.replace("String", "str");
        StringBuilder builder = new StringBuilder();
        Text.lines()
                .map(this::processLine)
                .forEach(line -> builder.append(line).append("\n"));
        return builder.toString();
    }

    private String processLine(String line){
        if (!line.contains("=") || line.contains("==") || line.contains(">") || line.contains("<") || line.contains("!")) {
            return LogicFilter(line);
        }
        if(line.contains("[")){
            return ArrayVariableSetter(line);
        }
        return NonArrayVariableSetter(line);
    }

    private String LogicFilter(String line){
        boolean isLogicStatement = false;
        for(String word : indentKeywords){
            if (line.contains(word)) {
                isLogicStatement = true;
                break;
            }
        }
        if(isLogicStatement) {
            int indentCount = 0;
            while (indentCount < line.length() && line.charAt(indentCount) == '\t') {
                indentCount++;
            }
            String leadingTabs = line.substring(0, indentCount);
            String content = line.substring(indentCount);

            if (content.startsWith("for") && content.contains("(") && content.contains(")")) {
                int openParenIndex = content.indexOf('(');
                int closeParenIndex = content.lastIndexOf(')');
                String insideParentheses = content.substring(openParenIndex + 1, closeParenIndex).trim();

                String[] segments = insideParentheses.split(";");
                if (segments.length == 3) {
                    String initializer = segments[0].trim();
                    String[] initializerTokens = initializer.split("\\s+");
                    String variableName = initializerTokens[1];
                    String startValue = initializerTokens[3];

                    String condition = segments[1].trim();
                    String[] conditionTokens = condition.split("<");
                    String stopValue = conditionTokens[1].trim();

                    String increment = segments[2].trim();
                    String stepValue = "1";
                    if (increment.endsWith("++")) {
                        stepValue = "1";
                    } else if (increment.endsWith("--")) {
                        stepValue = "-1";
                    } else if (increment.contains("+=")) {
                        String[] incrementTokens = increment.split("\\+=");
                        stepValue = incrementTokens[1].trim();
                    } else if (increment.contains("-=")) {
                        String[] incrementTokens = increment.split("-=");
                        stepValue = "-" + incrementTokens[1].trim();
                    }

                    StringBuilder pythonFor = new StringBuilder();
                    pythonFor.append(leadingTabs)
                            .append("for ")
                            .append(variableName)
                            .append(" in range(")
                            .append(startValue)
                            .append(", ")
                            .append(stopValue);
                    if (!stepValue.equals("1")) {
                        pythonFor.append(", ").append(stepValue);
                    }
                    pythonFor.append(")");
                    return pythonFor.toString();
                }

                if (insideParentheses.contains(":")) {
                    String[] forEachParts = insideParentheses.split(":");
                    if (forEachParts.length == 2) {
                        String leftSide = forEachParts[0].trim();
                        String rightSide = forEachParts[1].trim();
                        String[] leftTokens = leftSide.split("\\s+");
                        String elementName = leftTokens[leftTokens.length - 1];

                        return leadingTabs +
                                "for " +
                                elementName.replace("[]", "") +
                                " in " +
                                rightSide;
                    }
                }
            }
            if (!line.contains("()")) {
                return replaceLast(line.replaceFirst("\\(", " "), ")" , "");
            }
        }
        return line;
    }

    private String replaceLast(String input, String regex, String replacement){
        if(input.contains(regex)) {
            StringBuilder builder = new StringBuilder();
            int index = input.lastIndexOf(regex);
            char[] charArray = input.toCharArray();
            for (int i = 0; i < index; i++) {
                builder.append(charArray[i]);
            }
            builder.append(replacement);
            for (int i = index + 1; i < input.length(); i++) {
                builder.append(charArray[i]);
            }
            return builder.toString();
        }
        return input;
    }

    private String ArrayVariableSetter(String line){
        if(!(line.contains(" = "))){
            line = line.replace("=", " = ");
        }
        String name = line.split(" = ")[0].strip().replace("[]", "").strip().split(" ")[1];
        String def = line.split(" = ")[1].strip().replace("{", "").replace("}", "");

        return new Variable(name, def).toString();
    }

    private String NonArrayVariableSetter(String line){
        if(!(line.contains(" = "))){
            line = line.replace("=", " = ");
        }
        line = line.replace("const ", "").replace("public ", "").replace("private", "");
        String type;
        String name;
        String def;
        if(line.split(" = ")[0].strip().split(" ").length > 1) {
            type = line.split(" = ")[0].strip().split(" ")[0];
            name = line.split(" = ")[0].strip().split(" ")[1];
            def = line.split(" = ")[1].strip();
            return extractTabs(line) + (new Variable(name, type, def));
        }
        return line;
    }

    private static String extractTabs(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        int tabCount = 0;
        for (char c : input.toCharArray()) {
            if (c == '\t') {
                tabCount++;
            }
        }

        StringBuilder onlyTabs = new StringBuilder(tabCount);
        for (int i = 0; i < tabCount; i++) {
            onlyTabs.append('\t');
        }

        return onlyTabs.toString();
    }

    private static int countTabs(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        int tabCount = 0;
        for (char c : input.toCharArray()) {
            if (c == '\t') {
                tabCount++;
            }
        }
        return tabCount;
    }

    private String FinalFilter(String text){
        StringBuilder builder = new StringBuilder();
        for(String line : text.lines().toList()) {
            if(!line.isBlank()) {
                if(line.strip().startsWith("#")){
                    builder.append(line).append('\n');
                }
                else if (line.endsWith(";")) {
                    builder.append(line.replace(";", "")).append('\n');
                }
                else {
                    builder.append(line.replace("str ", "def ").replace("int ", "def ").replace("bool ", "def ").replace("double ", "def ")).append(":").append('\n');
                }
            }
        }
        builder.append("run()");
        return builder.toString();
    }

    public void setConvertedText(String convertedText) {
        this.convertedText = convertedText;
    }

    public String getConvertedText() {
        return convertedText;
    }

    public static class Variable {
        private final String Name;
        private final String Type;
        private final String Def;
        private boolean Array = false;

        public Variable(String name, String type, String def) {
            this.Name = name;
            this.Type = type;
            this.Def = def;
        }

        public Variable(String name, String def) {
            this.Name = name;
            this.Type = null;
            Array = true;
            this.Def = def;
        }

        public String getName() {
            return Name;
        }

        public String getType() {
            return Type;
        }

        public String getDef() {
            return Def;
        }

        @Override
        public String toString() {
            if(Array){
                return Name + " = " + "[" + Def + "];";
            }
            return Name + " = " + Type + "(" + Def + ");";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Variable variable)) return false;
            return Objects.equals(Name, variable.Name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(Name);
        }
    }
}
