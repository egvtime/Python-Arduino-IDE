package Error;

import java.util.*;

public class ErrorIdentifier {

    private final String text;
    private Vector<Integer> lines, columns;

    private static final List<String> methods = Arrays.asList(
            "run()","serialprintln","serialprint","serialwrite","serialwriteln",
            "randomseed","random","constrain","map","max","min","sqrt",
            "digitalwrite","digitalread","analogwrite","analogread",
            "serialbegin","delay","delaymicroseconds","pinmode"
    );

    private static final Map<String,String> Solutions = new HashMap<>();

    static {
        Solutions.put("Incorrect Method Statement", "All methods must be lowercase");
        Solutions.put("Incorrect Line Ending", "Remove \";\" At The End Of Statement");
        Solutions.put("Incorrect Logic Ending", "Add \":\" At The End Of Statement");
        Solutions.put("Incorrect Tab Placement",     "Correct Tab Placement After Logic Function");
        Solutions.put("Missing \"run()\" Method",    "Your script must end with a \"run()\" Method");
        Solutions.put("Code After \"run()\" Method",    "Your script must end with a \"run()\" Method");
        Solutions.put("Can't Specify Method Statement",    "Replace Type with: \"def\"");
    }

    public ErrorIdentifier(String text) {
        this.text = text;
    }

    public Vector<ArduinoError> identify() {
        Vector<ArduinoError> errors = new Vector<>();
        Vector<String> types = getErrorTypes();
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            int ln = lines.get(i), col = columns.get(i);
            errors.add(new ArduinoError(
                    "Error at line " + ln + ", col " + col + ": " + type,
                    new ArduinoError.Location<>(ln, col),
                    type,
                    Solutions.get(type)
            ));
        }
        return errors;
    }

    private Vector<String> getErrorTypes() {
        Vector<String> errorTypes = new Vector<>();
        Vector<Integer> lineNums   = new Vector<>();
        Vector<Integer> cols       = new Vector<>();

        boolean sawRun = false;

        int CurrentLine = 0;

        boolean inLogicStatement = false;
        int tabAmount = 0;
        for(String line : text.lines().toList()){
            CurrentLine++;

            if(sawRun && !line.isBlank()){
                errorTypes.add("Code After \"run()\" Method");
                lineNums.add(CurrentLine);
                cols.add(1);
                break;
            }

            if(line.contains("void")){
                errorTypes.add("Can't Specify Method Statement");
                lineNums.add(CurrentLine);
                cols.add(1);
            }
            else if((line.contains("int") || line.contains("str") || line.contains("byte") || line.contains("double")) && line.endsWith(":")){
                errorTypes.add("Can't Specify Method Statement");
                lineNums.add(CurrentLine);
                cols.add(1);
            }
            else if(!line.contains("def") && !line.contains("\t") && !line.contains("=") && !line.contains("run()") && !line.strip().startsWith("#") && !line.isBlank()){
                errorTypes.add("Incorrect Tab Placement");
                lineNums.add(CurrentLine);
                cols.add(1);
            }

            if(inLogicStatement){
                if(!line.isBlank()) {
                    if ((countTabs(line) - tabAmount) != 1) {
                        errorTypes.add("Incorrect Tab Placement");
                        lineNums.add(CurrentLine);
                        cols.add(1);
                        tabAmount = countTabs(line);
                    }
                    inLogicStatement = false;
                }
            }

            if(line.endsWith(":")) {
                tabAmount = countTabs(line);
                inLogicStatement = true;
            }

            if((line.contains("if") || line.contains("else") || line.contains("for") || line.contains("while") || line.contains("def")) && !line.endsWith(":")){
                errorTypes.add("Incorrect Logic Ending");
                inLogicStatement = true;
                lineNums.add(CurrentLine);
                cols.add(line.length());
            }

            String trimmedLine = line.strip();

            if(trimmedLine.endsWith(";")){
                errorTypes.add("Incorrect Line Ending");
                lineNums.add(CurrentLine);
                cols.add(line.indexOf(';'));
            }

            for(String method : methods) {
                if (trimmedLine.toLowerCase().contains(method) && !trimmedLine.contains(method)){
                    errorTypes.add("Incorrect Method Statement");
                    lineNums.add(CurrentLine);
                    cols.add(getColumn(method, text.toLowerCase()));
                }
            }

            if(line.contains("run()")){
                sawRun = true;
            }
        }

        if (!sawRun) {
            errorTypes.add("Missing \"run()\" Method");
            lineNums.add(text.length());
            cols.add(1);
        }

        this.lines   = lineNums;
        this.columns = cols;
        return errorTypes;
    }

    private static int getColumn(String targetWord, String text) {
        List<String> lines = text.lines().toList();
        for (String line : lines) {
            int index = line.indexOf(targetWord);
            if (index != -1) {
                return index + 1;
            }
        }
        return -1;
    }

    private static int countTabs(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == '\t') {
                count++;
            }
        }
        return count;
    }
}
