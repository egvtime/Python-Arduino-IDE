package DeveloperTools.UI;

import Translator.*;
import UI.Controllers.*;
import java.io.*;
import java.util.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;

public class ArduinoEditorController {
    @FXML
    private TextArea codeArea;

    private MainUIController controller;

    private final Vector<String> indentKeywords = new Vector<>(Arrays.asList("if", "else", "for", "while", "def"));

    public void initialize(Stage primaryStage, MainUIController controller){
        this.controller = controller;
        primaryStage.setOnCloseRequest(e -> {
            sendCode();
        });

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.ENTER) {
                event.consume();
                int caretPos = codeArea.getCaretPosition();
                String indent = calculateIndent(codeArea.getText(), caretPos);
                codeArea.insertText(caretPos, indent);
                codeArea.positionCaret(caretPos + indent.length());
            } else if (code == KeyCode.TAB && event.isShiftDown()) {
                event.consume();
                int caretPos = codeArea.getCaretPosition();
                String text = codeArea.getText();
                int lineStart = text.lastIndexOf('\n', caretPos - 1);
                if (lineStart == -1) {
                    lineStart = 0;
                } else {
                    lineStart = lineStart + 1;
                }

                if (text.startsWith("\t", lineStart)) {
                    codeArea.deleteText(lineStart, lineStart + 1);
                    codeArea.positionCaret(Math.max(caretPos - 1, lineStart));
                }
            }
            if (code == KeyCode.SLASH && event.isControlDown()) {
                event.consume();
                int caretPos = codeArea.getCaretPosition();
                String fullText = codeArea.getText();

                int lastNewline = fullText.lastIndexOf('\n', caretPos - 1);
                int firstNonTab = lastNewline + 1;

                while (firstNonTab < fullText.length() && fullText.charAt(firstNonTab) == '\t') {
                    firstNonTab++;
                }

                if (firstNonTab < fullText.length() && fullText.charAt(firstNonTab) == '#') {
                    codeArea.deleteText(firstNonTab, firstNonTab + 1);
                } else {
                    codeArea.insertText(firstNonTab, "#");
                }
            }

        });
    }

    private String calculateIndent(String text, int caretPos) {
        int lineStart = text.lastIndexOf('\n', caretPos - 1);
        if (lineStart == -1) {
            lineStart = 0;
        } else {
            lineStart++;
        }

        int indentCount = 0;
        while (lineStart + indentCount < text.length() &&
                text.charAt(lineStart + indentCount) == '\t') {
            indentCount++;
        }

        String lineText = text.substring(lineStart, caretPos);
        String trimmed = lineText.trim();
        String[] parts = trimmed.split("\\s+");

        if (trimmed.endsWith(":") ||
                (parts.length > 0 && indentKeywords.contains(parts[0]))) {
            indentCount++;
        }

        return "\n" + "\t".repeat(Math.max(0, indentCount));
    }

    public void sendCode(){
        CppConverter converter = new CppConverter();
        converter.setConvertedText(codeArea.getText());
        controller.setTitle("Code From Arduino Editor    |    Python Arduino IDE");
        try {
            converter.Convert();
            controller.setTextAreaText(converter.getConvertedText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
