package PythonTranslator;

import java.io.*;
import java.util.*;

public class Converter {
    private final File py;
    private final String fileLocation;
    private BufferedReader BR;
    private BufferedWriter BW;

    private static HashMap<String, String> commandMap;
    private static final String[] variableTypes = {"int", "String", "double", "char", "byte"};
    private static Vector<Variable> arraysList = new Vector<>();
    private Vector<Variable> variableList = new Vector<>();

    private int tabAmount = 0;
    private String convertedText;

    private boolean LibraryDisplay = false;

    public Converter(File PythonFile) {
        this.py = PythonFile;
        fileLocation = "";
        def();
    }

    public Converter(File PythonFile, String fileLocation) {
        this.py = PythonFile;
        this.fileLocation = fileLocation;
        def();
    }

    private void def() {
        commandMap = new HashMap<>();

        // Core structure
        commandMap.put("def setup():", "void setup(){");
        commandMap.put("def loop():", "void loop(){");
        commandMap.put("def", "void ");
        commandMap.put("run()", "");

        // Serial Functions
        commandMap.put("serialprintln", "Serial.println");
        commandMap.put("serialprint", "Serial.print");
        commandMap.put("serialwrite", "Serial.write");
        commandMap.put("serialwriteln", "Serial.writeln");
        commandMap.put("serialbegin", "Serial.begin");

        // Timing
        commandMap.put("delay", "delay");
        commandMap.put("delaymicroseconds", "delayMicroseconds");

        // Pin control
        commandMap.put("pinmode", "pinMode");
        commandMap.put("digitalwrite", "digitalWrite");
        commandMap.put("analogwrite", "analogWrite");
        commandMap.put("digitalread", "digitalRead");
        commandMap.put("analogread", "analogRead");

        // Random & Seed
        commandMap.put("randomseed", "randomSeed");
        commandMap.put("pulsein", "pulseIn");
        commandMap.put("random", "random");

        // Math Functions
        commandMap.put("constrain", "constrain");
        commandMap.put("map", "map");
        commandMap.put("max", "max");
        commandMap.put("min", "min");
        commandMap.put("sqrt", "sqrt");

        arraysList = new Vector<>();
        variableList = new Vector<>();
    }

    public static void setCommandMap(HashMap<String, String> commandMap) {
        Converter.commandMap = commandMap;
    }

    public void addCommand(String pyCommand, String arduinoCommand) {
        commandMap.put(pyCommand, arduinoCommand);
    }

    public void removeCommand(String pyCommand) {
        commandMap.remove(pyCommand);
    }

    public void removeCommand(String pyCommand, String arduinoCommand) {
        commandMap.remove(pyCommand, arduinoCommand);
    }

    public void getCommand(String Command) {
        commandMap.get(Command);
    }

    public Vector<Variable> getVariableList() {
        return variableList;
    }

    public String getConvertedText() {
        return convertedText;
    }

    public boolean isLibraryDisplay() {
        return LibraryDisplay;
    }

    public void setLibraryDisplay(boolean libraryDisplay) {
        LibraryDisplay = libraryDisplay;
    }

    private void setupTranslate() {
        try {
            BR = new BufferedReader(new FileReader(py));
            BW = new BufferedWriter(new FileWriter(fileLocation));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeTranslate() {
        try {
            BW.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void translate() throws IOException {
        setupTranslate();

        StringBuilder builder = new StringBuilder();

        builder = scriptingToProgramming(builder);

        builder = variablesFilter(builder.toString());

        builder = logicFilter(builder.toString());

        builder = syntaxFixer(builder.toString());

        builder = returnStatements(builder.toString());

        convertedText = builder.toString();

        writeFile(builder);
    }

    public String translateWithoutExporting() throws IOException {
        BR = new BufferedReader(new FileReader(py));
        StringBuilder builder = new StringBuilder();

        builder = scriptingToProgramming(builder);

        builder = variablesFilter(builder.toString());

        builder = logicFilter(builder.toString());

        builder = syntaxFixer(builder.toString());

        builder = returnStatements(builder.toString());

        convertedText = builder.toString();

        return builder.toString();
    }

    private StringBuilder scriptingToProgramming(StringBuilder builder) throws IOException {
        String line;
        while ((line = BR.readLine()) != null) {
            if (!(line.isBlank())) {
                if (line.endsWith(":") &&
                        !line.contains("if") &&
                        !line.contains("elif") &&
                        !line.contains("else") &&
                        !line.contains("for") &&
                        !line.contains("while")
                ){
                    if (tabAmount == 0) {
                        builder.append(applyMapToLine(line)).append("\n");
                        tabAmount = 1;
                    } else if(tabAmount != 1) {
                        builder.append("}".repeat(Math.max(0, tabAmount)));
                        builder.append("\n");
                        builder.append(applyMapToLine(line)).append("\n");
                        tabAmount = 1;
                    }else {
                        builder.append("}".repeat(tabAmount));
                        builder.append("\n");
                        builder.append(applyMapToLine(line)).append("\n");
                    }
                } else if (line.contains("run()")) {
                    while (tabAmount >= 0) {
                        builder.append("\t".repeat(Math.max(0, tabAmount - 1)));
                        builder.append("}\n");
                        tabAmount--;
                    }
                } else {
                    int CurrentTabAmount = countTabs(line);

                    if (CurrentTabAmount > tabAmount && !line.contains("def")) {
                        builder.append("\t".repeat(Math.max(0, tabAmount)));
                        builder.append("{").append("\n");
                        builder.append(applyMapToLine(line)).append(";").append("\n");
                    } else if (CurrentTabAmount < tabAmount) {
                        builder.append("\t".repeat(Math.max(0, tabAmount - CurrentTabAmount)));
                        builder.append("}".repeat(Math.max(0, tabAmount - CurrentTabAmount)));
                        builder.append('\n');
                        builder.append(applyMapToLine(line)).append(";").append("\n");
                    } else {
                        builder.append(applyMapToLine(line)).append(";").append("\n");
                    }
                    tabAmount = CurrentTabAmount;
                }
            }else{
                builder.append("\n");
            }
        }
        builder = new StringBuilder(builder.toString().strip());
        builder.deleteCharAt(builder.length() - 1);
        return builder;
    }

    private String applyMapToLine(String lines) {
        StringBuilder Filtered = new StringBuilder();

        for (String line : lines.lines().toList()) {

            String[] lineArray = line.split(" ");

            if (line.contains("def setup")) {
                Filtered.append(commandMap.get(line)).append("\n");
            } else if (line.contains("def loop")) {
                Filtered.append(commandMap.get(line)).append("\n");
            }
            else {
                for (String l : lineArray) {
                    for (int i = 0; i < l.length(); i++) {
                        if (l.charAt(i) == '\t') {
                            Filtered.append("\t");
                        }
                    }
                    if (commandMap.containsKey(getOutsideBracketMethod(l))) {
                        Filtered.append(applyMapToSubstrings(l));
                    }else if (commandMap.containsKey(l.replaceAll("\t", "").strip())) {
                        Filtered.append(commandMap.get(l.replaceAll("\t", "").strip())).append(" ");
                    }
                    else {
                        Filtered.append(l.replaceAll("\t", "").strip());
                        if (!l.contains(")")) {
                            Filtered.append(" ");
                        }
                    }
                }
            }
        }
        return Filtered.toString();
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

    private String getOutsideBracketMethod(String line){
        return line.split("\\(")[0].strip();
    }

    public String getInsideBracketMethod(String input) {
        int start = input.indexOf('(');
        if (start < 0) {
            return input;
        }
        int depth = 1;
        StringBuilder builder = new StringBuilder();
        for (int i = start + 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                depth++;
                builder.append(c);
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return builder.toString();
                }
                builder.append(c);
            } else {
                builder.append(c);
            }
        }
        return builder + ")";
    }

    private static int countTabs(String line) {
        if (line == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < line.length(); ) {
            if (line.charAt(i) == '\t') {
                count++;
                i++;
            } else if (i + 3 < line.length()
                    && line.charAt(i) == ' '
                    && line.charAt(i + 1) == ' '
                    && line.charAt(i + 2) == ' '
                    && line.charAt(i + 3) == ' ') {
                count++;
                i += 4;
            } else {
                i++;
            }
        }
        return count;
    }

    private StringBuilder logicFilter(String text) {
        StringBuilder FirstString;
        FirstString = logicSyntax(text);
        StringBuilder builder = new StringBuilder();

        int ForAmount = 0;
        for(String line : FirstString.toString().lines().toList()){
            if(line.contains(" and ")){line = line.replace(" and ", " && ");}
            if(line.contains(" or ")){line = line.replace(" or ", "||");}
            if(line.contains(" not ")){line = line.replace(" not ", "!");}

            if(line.contains("for"))
            {
                if (line.contains("in range")) {
                    String Range = getInRange(text, ForAmount);
                    ForAmount++;
                    builder.append("\t".repeat(Math.max(0, countTabs(line))));
                    builder.append("for(int i = 0; i <= ").append(Range).append(" ; i++)\n");
                }else {
                    builder.append("\t".repeat(Math.max(0, countTabs(line))));
                    builder.append(getOutsideBracketMethod(line)).append("(");
                    builder.append(ForLogic(getInsideBracketMethod(getInsideBracketMethod(line))));
                }
            }
            else {
                builder.append(line).append("\n");
            }
        }
        return builder;
    }

    private String ForLogic(String line) {
        return line.split("in")[0].strip() +
                " : " +
                line.split("in")[1].strip() +
                ")";
    }

    private String getInRange(String text, int OutsideAmount) {
        int InsideAmount = 0;
        String Range = "";
        for (String line : text.lines().toList()) {
            if (line.contains("in range")) {
                if (InsideAmount == OutsideAmount) {
                    Range = line.split("\\(")[1].split("\\)")[0].replace(")", "");
                } else {
                    InsideAmount++;
                }
                Object checkedRange = checkString(Range);
                if (checkedRange instanceof Integer || checkedRange instanceof Double) {
                    return Range;
                }
                else if (checkedRange instanceof String) {
                    if(commandMap.containsKey(line.split("\\(")[1])){
                        return (commandMap.get(line.split("\\(")[1]) + "(" + line.split("\\(")[2]).replace("):", "").replace(";", "");
                    }else
                    {
                        return ((line.split("\\(")[1]) + "(" + line.split("\\(")[2]).replace("):", "").replace(";", "");
                    }
                }
            }
        }
        return null;
    }

    private static StringBuilder logicSyntax(String text) {
        StringBuilder builder = new StringBuilder();
        List<String> lines = text.lines().toList();
        for (String line : lines) {
            if (line.contains("elif")) {
                line = line.replace("elif", "else if (");
                line = line.replace("):", ")");
                line = line.replace(":", ")");
                line = constFilter(line);
            }else if (line.contains("if")) {
                line = line.replace("if ", "if(");
                line = line.replace("):", ")");
                line = line.replace(":", ")");
                line = constFilter(line);
            } else if (line.contains("else")) {
                line = line.replace("):", ")");
                line = line.replace(":", "");
                line = constFilter(line);
            } else if (line.contains("while")) {
                line = line.replace("while", "while(");
                line = constFilter(line);
                line = line.replace("):", ")");
                line = line.replace(":", ")");
            }
            else if (line.contains("for")) {
                line = line.replace("for", "for(");
                line = constFilter(line);
                line = line.replace("):", ")");
                line = line.replace(":", ")");
            }
            else if (line.contains("#")) {
                line = line.replace("#", "//");
                line = constFilter(line);
                if (!(line.replaceAll(" {4}", "").replace("\t", "")).startsWith("//")) {
                    line = line.replace(" //", "; //");
                }
            }
            builder.append(line).append('\n');
        }
        return builder;
    }

    private static String constFilter(String line) {
        line = line.replace(";", "");
        line = line.replace("( ", "(");
        return line;
    }

    private StringBuilder variablesFilter(String text) {
        text = text.replace("str(", "string(");
        StringBuilder builder = new StringBuilder();
        text.lines()
                .map(this::processLine)
                .forEach(line -> builder.append(line).append("\n"));
        return builder;
    }

    private String processLine(String line) {
        if (!line.contains("=") || line.contains("==") || line.contains(">") || line.contains("<") || line.contains("!")) {
            return line;
        }
        if(line.contains("[")){
            return ArrayVariableSetter(line);
        }
        return NonArrayVariableSetter(line);
    }

    private String ArrayVariableSetter(String line) {
        if(!(line.contains(" = "))){
            line = line.replace("=", " = ");
        }
        String name = line.split(" = ")[0].strip();

        Variable usedVariable = null;
        boolean isUsedBefore = false;
        for(Variable var : arraysList){
            String used;
            used = (line.split(" = ")[1].split("\\[")[0]).replace(";", "").strip();
            if(var.getName().equals(used)){
                usedVariable = var;
                isUsedBefore = true;
            }
        }

        if(isUsedBefore){
            return usedVariable.getType() + " " + line;
        }
        else
        {
            line = line.replace("[", "{").replace("]", "}");

            String Value = line.trim().split("\\{", 2)[1].trim().split(",")[0].trim();

            Object parsedVariable = checkString(Value);

            String type = "int";
            if (parsedVariable instanceof Double) {
                type = "double";
            } else if (parsedVariable instanceof String) {
                if (Value.contains("+")) {
                    if (getFirstMathStatement(Value).equals("+")) {
                        String method = getOutsideBracketMethod(Value.split("\\+")[0].trim());
                        if (checkString(method) instanceof Double) {
                            type = "double";
                        } else if (checkString(method) instanceof Integer) {
                            type = "int";
                        } else if (!commandMap.containsValue(method) && !commandMap.containsKey(method)){
                            type = "String";
                        }
                    }
                } else if (Value.contains("*") || Value.contains("/") || Value.contains("-")) {
                    String dec = getFirstMathStatement(Value);
                    String method;
                    if (dec.equals("*")) {
                        method = getOutsideBracketMethod(Value.split("\\*")[0].strip());
                    } else {
                        method = getOutsideBracketMethod(Value.split(dec)[0].strip());
                    }
                    if (checkString(method) instanceof Double) {
                        type = "double";
                    }
                    if (checkString(method) instanceof Integer) {
                        type = "int";
                    } else if (!commandMap.containsValue(method) && !commandMap.containsKey(method)) {
                        type = "String";
                    }
                } else {
                    String method = getOutsideBracketMethod(Value.strip());
                    if (!commandMap.containsValue(method) && !commandMap.containsKey(method)) {
                        type = "String";
                    }
                }
            }

            line = line.replace(" = ", "[] = ");

            arraysList.add(new Variable(name, type));

            return type + " " + applyMapToSubstrings(line);
        }
    }

    private String NonArrayVariableSetter(String line) {
        if(!(line.contains(" = "))){
            line = line.replace("=", " = ");
        }
        String[] parts = line.trim().split(" = ", 2);
        String name = parts[0].strip();
        String Value = applyMapToLine(parts[1]);
        if (Value.isEmpty()) {
            return line;
        }
        String finalLine = line;
        return Arrays.stream(variableTypes)
                .filter(line.trim()::contains)
                .findFirst()
                .map(type -> getLinesWithDeclaredType(finalLine, name, type, Value))
                .orElseGet(() -> typeGetter(finalLine, name, Value.strip()));
    }

    private String getLinesWithDeclaredType(String line, String name, String type, String value) {
        Variable var = new Variable(name, type, applyMapToSubstrings(value));
        variableList.add(var);
        int tabs = 0;
        for (char c : line.toCharArray()) {
            if (c == '\t') tabs++;
            else break;
        }
        String indentation = "\t".repeat(tabs);
        return indentation + var;
    }

    private String typeGetter(String line, String name, String Value) {
        Object parsedVariable = checkString(Value.replace(" ;", "").trim());
        String type = "int";
        if (parsedVariable instanceof Double) {
            type = "double";
        }
        else if (parsedVariable instanceof String) {
            if(Value.contains("+")) {
                if(getFirstMathStatement(Value).equals("+")) {
                    String method = getOutsideBracketMethod(Value.split("\\+")[0].trim());
                    if (checkString(method) instanceof Double) {
                        type = "double";
                    }
                    else if(checkString(method) instanceof Integer){
                        type = "int";
                    }if (!commandMap.containsValue(method) && !commandMap.containsKey(method)) {
                        type = "String";
                    }
                }
            }
            else if(Value.contains("*") || Value.contains("/") || Value.contains("-")) {
                String dec = getFirstMathStatement(Value);
                String method;
                if (dec.equals("*")) {
                    method = getOutsideBracketMethod(Value.split("\\*")[0].strip());
                }else{
                    method = getOutsideBracketMethod(Value.split(dec)[0].strip());
                }
                if (checkString(method) instanceof Double) {
                    type = "double";
                }
                if (checkString(method) instanceof Integer) {
                    type = "int";
                }
                if (!commandMap.containsValue(method) && !commandMap.containsKey(method)) {
                    type = "String";
                }
            }
            else{
                String method = getOutsideBracketMethod(Value.replace(" ;", "").trim());
                if(method.contains("true") || method.contains("false")){
                    type = "bool";
                }
                else if (!commandMap.containsValue(method) && !commandMap.containsKey(method)) {
                    type = "String";
                }
                for(Variable var : variableList) {
                    if(var.getName().equals(method)) {
                        type = var.getType();
                    }
                }
                StringBuilder nameBuilder = new StringBuilder(name);
                for(Variable var : arraysList) {
                    if(var.getName().equals(method)) {
                        type = var.getType();
                        nameBuilder.append("[]");
                    }
                }
                name = nameBuilder.toString();
            }
        }
        Variable var = new Variable(name, type, Value);
        if (!variableList.contains(var)) {
            variableList.add(var);
            int tabs = 0;
            for (char c : line.toCharArray()) {
                if (c == '\t') tabs++;
                else break;
            }
            String indentation = "\t".repeat(tabs);
            return indentation + var;
        }
        return line;
    }

    private String getFirstMathStatement(String Value) {
        for(char character : Value.toCharArray()){
            if(character == '+'){return "+";}
            if(character == '-'){return "-";}
            if(character == '*'){return "*";}
            if(character == '/'){return "/";}
        }
        throw new RuntimeException("No Math Declaration");
    }

    private StringBuilder returnStatements(String text) {
        if(text.contains("return")){
            StringBuilder builder = new StringBuilder();
            int CurrentLine = 0;
            Vector<Integer> DefLines = new Vector<>();
            Vector<String> returnValues = new Vector<>();
            int DefLine = 0;
            for(String line : text.lines().toList()) {
                CurrentLine++;
                if (line.contains("void")) {
                    DefLine = CurrentLine;
                }
                if (line.contains("return")) {
                    DefLines.add(DefLine);
                    returnValues.add(line);
                }
            }

            CurrentLine = 0;
            int ReturnStatement = 0;
            for(String line : text.lines().toList()){
                CurrentLine++;
                if(DefLines.contains(CurrentLine)){
                    String Var = getReturnVariable(returnValues, ReturnStatement);
                    line = line.replace("void", Var);
                    ReturnStatement++;
                }
                builder.append(line).append('\n');
            }
            return builder;
        }
        return new StringBuilder().append(text);
    }

    private String getReturnVariable(Vector<String> returnValues, int ReturnStatement) {
        String Var = "";
        String firstVar = returnValues.get(ReturnStatement).strip().split(" ")[1].replace(";", "");
        if(firstVar.contains("+") || firstVar.contains("*") || firstVar.contains("/") || firstVar.contains("-")){
            if(firstVar.contains("+")) {
                if(getFirstMathStatement(firstVar).equals("+")) {
                    String method = getOutsideBracketMethod(firstVar.split("\\+")[0].trim());
                    if (checkString(method) instanceof Double) {
                        Var = "double";
                    }
                    else if(checkString(method) instanceof Integer) {
                        Var = "int";
                    }
                    else if (!commandMap.containsValue(method)) {
                        Var = "String";
                    } else{
                        Var = "int";
                    }
                }
            }
            else if(firstVar.contains("*") || firstVar.contains("/") || firstVar.contains("-")) {
                String dec = getFirstMathStatement(firstVar);
                String method;
                if (dec.equals("*")) {
                    method = getOutsideBracketMethod(firstVar.split("\\*")[0].trim());
                }else{
                    method = getOutsideBracketMethod(firstVar.split(dec)[0].trim());
                }
                if (checkString(method) instanceof Double) {
                    Var = "double";
                }
                else if (checkString(method) instanceof Integer) {
                    Var = "int";
                } else if (!commandMap.containsValue(method)) {
                    Var = "String";
                } else
                {
                    Var = "int";
                }
            }
            else{
                String method = getOutsideBracketMethod(firstVar.replace(" ;", "").trim());
                if (!commandMap.containsValue(method)) {
                    Var = "String";
                }else{
                    Var = "int";
                }
            }
        }
        else {
            if (!(checkString(firstVar) instanceof String)) {
                Var = (checkString(firstVar)).getClass().toString().replace("class java.lang.", "").replace("Double", "double").replace("Integer", "int");
            }
            else if(firstVar.contains("true") || firstVar.contains("false")){
                Var = "bool";
            }
            else {
                String method = getOutsideBracketMethod(firstVar.replace(" ;", "").trim());

                boolean isInList = false;

                for(Variable varListElement :  variableList){
                    if(firstVar.equals(varListElement.getName())){
                        Var = varListElement.getType();
                        isInList = true;
                        break;
                    }
                }

                if(!isInList) {
                    if (firstVar.contains("'")) {
                        Var = "char";
                    } else if (firstVar.contains("\"")) {
                        Var = "String";
                    } else if (!commandMap.containsValue(method)) {
                        Var = "String";
                    } else {
                        Var = "int";
                    }
                }
            }
        }
        return Var;
    }

    private StringBuilder syntaxFixer(String string) {
        StringBuilder builder = new StringBuilder();

        if(LibraryDisplay) {
            if(string.contains("from arduino import * ;")) {
                string = string.replace("from arduino import * ;", "#include <Arduino.h>");
            }else{
                string = "#include <Arduino.h>" + '\n' + string;
            }
        }else{
            string = string.replace("from arduino import * ;", "");
        }

        builder.append(string
                .replace("elif", "else if")
                .replace("():", "(){")
                .replace(" ;", ";"));

        StringBuilder SecondBuilder = new StringBuilder();
        for(String line : builder.toString().lines().toList()){
            if (line.contains("#") && !line.contains("#include")) {
                line = line.replace("#", "//");
                line = constFilter(line);
                if (!(line.replaceAll(" {4}", "").replace("\t", "")).startsWith("//")) {
                    line = line.replace(" //", "; //");
                }
            }
            if(line.contains("void") && line.contains("):")){
                SecondBuilder.append(line.replace("):", "){")).append("\n");
            } else {
                SecondBuilder.append(line).append("\n");
            }
        }

        StringBuilder ThirdBuilder = new StringBuilder();
        String finalPrompt = SecondBuilder.toString();
        while(finalPrompt.contains("int     ") ||
                finalPrompt.contains("double     ") ||
                finalPrompt.contains("string     ")) {
            finalPrompt = finalPrompt.replace("int     ", "\tint ");
            finalPrompt = finalPrompt.replace("double     ", "\tdouble ");
            finalPrompt = finalPrompt.replace("string     ", "\tstring ");
        }
        ThirdBuilder.append(finalPrompt);

        return stairBracketsFilter(ThirdBuilder.toString());
    }

    private StringBuilder stairBracketsFilter(String line) {
        StringBuilder builder = new StringBuilder();
        int length = line.length();
        int i = 0;

        while (i < length) {
            char currentChar = line.charAt(i);
            if (currentChar != '}') {
                builder.append(currentChar);
                i++;
            } else {
                int count = 0;
                int j = i;
                while (j < length && line.charAt(j) == '}') {
                    count++;
                    j++;
                }
                if (count > 1) {
                    for (int braceIndex = 0; braceIndex < count; braceIndex++) {
                        builder.append("\n");
                        builder.append("\t".repeat((count - braceIndex)));
                        builder.append("}");
                    }
                } else {
                    builder.append("}");
                }
                i = j;
            }
        }
        return builder;
    }

    private static Object checkString(String input) {
        try {
            double d = Double.parseDouble(input);
            if (d == (int) d) {
                return (int) d;
            } else {
                return d;
            }
        } catch (NumberFormatException e) {
            return input;
        }
    }

    private void writeFile(StringBuilder built) throws IOException {
        List<String> builtScript = built.toString().lines().toList();
        for(String line : builtScript) {
            if (!line.isEmpty()) {
                BW.write(line);
                BW.newLine();
            }
        }
        closeTranslate();
    }

    public static class Variable {
        private final String Name;
        private final String Type;
        private final String Def;

        public Variable(String name, String type, String def) {
            this.Name = name;
            this.Type = type;
            this.Def = def;
        }

        public Variable(String name, String type) {
            this.Name = name;
            this.Type = type;
            Def = ";";
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
            return Type + " " + Name + " = " + Def;
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