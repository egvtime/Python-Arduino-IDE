package UI.Controllers;

import DeveloperTools.SerialMonitor.*;
import DeveloperTools.UI.*;
import Error.UI.*;
import Profile.*;
import Profile.ProfileControlUI.AccountManager.*;
import Profile.ProfileControlUI.GetID.*;
import Profile.ProfileControlUI.Login.*;
import Profile.ProfileControlUI.NewAccount.*;
import Profile.ProfileControlUI.NewProject.*;
import Profile.UI.*;
import Translator.*;
import Settings.*;
import Settings.UI.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.datatransfer.Clipboard;
import java.net.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Error.*;

public class MainUIController {
    @FXML private Button menuClose;
    @FXML private AnchorPane titleBar;
    @FXML private Button menuMinimize;
    @FXML private ImageView menuIcon;
    @FXML private Label Title;

    @FXML private MenuBar Menu;
    @FXML private RadioMenuItem Save;
    @FXML private ListView<String> lineNumberList;
    @FXML private TextArea codeArea;

    @FXML private Button OpenButton;
    @FXML private Button ConvertButton;
    @FXML private Button ProfileButton;

    @FXML private Label LeftStatus;
    @FXML private ImageView SaveImage;
    @FXML private Label RightStatus;

    private Stage stage;
    private String fileLocation;
    private boolean saved = false;
    private boolean upToDate = true;

    private String name = "Untitled JPython File";
    private final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));
    private HashMap<String, String> commandMap;
    private Vector<ArduinoError> errorList = new Vector<>();

    private final Vector<String> indentKeywords = new Vector<>(Arrays.asList("if", "else", "for", "while", "def"));

    private static ArduinoProfile userProfile;

    private SettingsConfiguration settingsConfiguration;
    private boolean developerOptionsActive = false;

    private int line = 1;
    private int col;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        menuIcon.setImage(icon);

        menuClose.setOnMouseEntered(e -> menuClose.setStyle("-fx-background-color: red;"));
        menuClose.setOnMouseExited(e -> menuClose.setStyle("-fx-background-color: #171E21;"));
        menuMinimize.setOnMouseEntered(e -> menuMinimize.setStyle("-fx-background-color: gray;"));
        menuMinimize.setOnMouseExited(e -> menuMinimize.setStyle("-fx-background-color: #171E21;"));

        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        ContextMenu OpenContextMenu = new ContextMenu();

        MenuItem OpenFolder = new MenuItem("Open Python Folder");
        OpenFolder.setOnAction(e -> OpenFolder(null));
        MenuItem OpenJPY = new MenuItem("Open JPY");
        OpenJPY.setOnAction(e -> Open(null));
        OpenContextMenu.getItems().addAll(OpenFolder, OpenJPY);
        OpenButton.setOnContextMenuRequested(event -> {
            OpenContextMenu.show(OpenButton, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        ContextMenu ExportContextMenu = new ContextMenu();

        MenuItem ConvertWithoutExporting = new MenuItem("Convert Without Exporting");
        ConvertWithoutExporting.setOnAction(e -> ConvertWithoutExporting(null));
        MenuItem Convert = new MenuItem("Convert Into INO File");
        Convert.setOnAction(e -> Convert(null));
        ExportContextMenu.getItems().addAll(Convert, ConvertWithoutExporting);
        ConvertButton.setOnContextMenuRequested(event -> {
            ExportContextMenu.show(ConvertButton, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        ContextMenu AccountContextMenu = new ContextMenu();
        MenuItem Profile = new MenuItem("Open Profile");
        Profile.setOnAction(e -> OpenProfile(null));
        MenuItem ProfileList = new MenuItem("Open Profile List");
        ProfileList.setOnAction(e -> {
            try {
                OpenList(null);
            } catch (SQLException | IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem ProfileLogOut = new MenuItem("Log Out");
        ProfileLogOut.setOnAction(e -> Logout(null));
        ProfileLogOut.setOnAction(e -> Logout(null));
        MenuItem ProjectList = new MenuItem("Open Project List");
        ProjectList.setOnAction(e -> {
            try {
                OpenProjectList(null);
            } catch (SQLException | IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        AccountContextMenu.getItems().addAll(Profile, ProfileList, ProjectList, ProfileLogOut);

        ProfileButton.setOnContextMenuRequested(event -> {
            AccountContextMenu.show(ProfileButton, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        codeArea.addEventFilter(KeyEvent.ANY, e -> {
            countLines();
            if (e.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = e.getCode();
                if(!(e.isControlDown() && code == KeyCode.S)) {
                    upToDate = false;
                }
                else{
                    Save(null);
                }
            }
            UpdateSaveIndicator();
        });

        countLines();
        codeArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> countLines());

        lineNumberList.setStyle("-fx-control-inner-background: #1E2528; -fx-background-color: #1E2528; -fx-text-fill: white; ");
        lineNumberList.setFixedCellSize(23);
        updateLineNumbers();
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            updateLineNumbers();
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

    private void updateLineNumbers() {
        String[] lines = codeArea.getText().split("\n", -1);
        int numberOfLines = lines.length;
        ObservableList<String> lineNumbers = FXCollections.observableArrayList();
        for (int i = 1; i <= numberOfLines; i++) {
            lineNumbers.add(String.valueOf(i));
        }
        lineNumberList.setItems(lineNumbers);
        codeArea.setPrefRowCount(numberOfLines);
        double cellHeight = lineNumberList.getFixedCellSize();
        if(cellHeight * numberOfLines > 451) {
            lineNumberList.setPrefHeight(numberOfLines * cellHeight + 3);
            codeArea.setPrefHeight(23.14538 * numberOfLines);
        }
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

    public void setPrimaryStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> {
            event.consume();
            handleExit();
        });
    }

    @FXML
    public void foldScreen(ActionEvent actionEvent) {
        stage.setIconified(true);
    }

    @FXML
    private void Quit(ActionEvent event) {
        handleExit();
    }

    public void setTextAreaText(String text) {
        codeArea.setText(text);
    }

    public void setUpToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }

    public static ArduinoProfile getUserProfile() {
        return userProfile;
    }

    public void setTitle(String titleText) {
        Title.setText(titleText);
    }

    public static void setUserProfile(ArduinoProfile userProfile) {
        MainUIController.userProfile = userProfile;
    }

    public void countLines() {
        String text = codeArea.getText();
        int caret = codeArea.getCaretPosition();
        line = 1;
        col = 1;
        for (int i = 0; i < caret; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
        LeftStatus.setText("Line: " + line + " Column: " + col);

        int totalLines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') totalLines++;
        }
        RightStatus.setText("Lines: " + totalLines);
    }

    private void handleExit() {
        if (!upToDate) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Confirmation");
            alert.setHeaderText("Do you want to save changes to " + name + "?");

            ButtonType btnYes = new ButtonType("Save");
            ButtonType btnNo = new ButtonType("No");
            ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnYes, btnNo, btnCancel);

            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(icon);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == btnYes) {
                    if (!saved) {
                        loadSaver();
                    }
                    setPythonCode();
                    stage.close();
                } else if (result.get() == btnNo) {
                    stage.close();
                }
            }
        } else {
            stage.close();
        }
    }

    @FXML
    private void SaveJPY(ActionEvent event) {
        loadSaver();
        setPythonCode();
    }

    public void setCommandMap(HashMap<String, String> foreignCommands) {
        commandMap = foreignCommands;
        commandMapSetup();
    }

    private void commandMapSetup() {
        // Core structure
        commandMap.put("def setup():", "void setup(){");
        commandMap.put("def loop():", "void loop(){");
        commandMap.put("def", "void");
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
        commandMap.put("random", "random");

        // Math Functions
        commandMap.put("constrain", "constrain");
        commandMap.put("map", "map");
        commandMap.put("max", "max");
        commandMap.put("min", "min");
        commandMap.put("sqrt", "sqrt");
    }

    @FXML
    private void SavePY(ActionEvent event) {
        String FolderLocation = Objects.requireNonNull(getFolderLocation("Pick Folder To Save File"));
        fileLocation = FolderLocation + "/" + (new File(FolderLocation).getName()) + ".py";
        try {
            cloneFile(new File("src/Assets/Arduino Library/arduino.py"), FolderLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setPythonCode();
        name = (new File(FolderLocation).getName()) + ".py";
        setTitle(name + "    |    Python Arduino IDE");
    }

    @FXML
    private void Save(ActionEvent event) {
        if (!saved) loadSaver();
        setPythonCode();
    }

    public void UpdateSaveIndicator() {
        SaveImage.setImage(new Image("Assets/Icons/Save Icon.png"));
        SaveImage.setVisible(upToDate);
        Save.setSelected(upToDate);
        SaveImage.setFitHeight(16);
        SaveImage.setFitWidth(16);
    }

    @FXML
    private void Convert(ActionEvent event) {
        if (!saved) {
            loadSaver();
            setPythonCode();
        }
        if (!upToDate) {
            setPythonCode();
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Python Code As CPP Code");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arduino File", "*.ino"));
        File location = chooser.showSaveDialog(stage);
        if (location != null) {
            PyConverter converter = new PyConverter(new File(fileLocation), location.getAbsolutePath());
            if (settingsConfiguration != null) {
                if (settingsConfiguration.getDisplayLibraryCheck()) {
                    converter.setLibraryDisplay(true);
                }
            }
            try {
                if (commandMap != null) {
                    PyConverter.setCommandMap(commandMap);
                }
                converter.translate();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/FXML/TextFlowUI.fxml"));
                Parent root = loader.load();
                TextFlowUIController controller = loader.getController();
                controller.setup();
                controller.setText(converter.getConvertedText());
                Stage convertStage = new Stage();
                convertStage.getIcons().add(icon);
                convertStage.initStyle(StageStyle.UNDECORATED);
                controller.setPrimaryStage(convertStage);
                convertStage.setScene(new Scene(root));
                controller.setTitle("Converted Text of " + name);
                convertStage.setResizable(false);
                convertStage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void ConvertWithoutExporting(ActionEvent event) {
        if (!saved) {
            loadSaver();
            setPythonCode();
        }
        if (!upToDate) {
            setPythonCode();
        }
        PyConverter converter = new PyConverter(new File(fileLocation));
        if (settingsConfiguration != null) {
            if (settingsConfiguration.getDisplayLibraryCheck()) {
                converter.setLibraryDisplay(true);
            }
        }
        try {
            if (commandMap != null) {
                PyConverter.setCommandMap(commandMap);
            }
            String ConvertedText = converter.translateWithoutExporting();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/FXML/TextFlowUI.fxml"));
            Parent root = loader.load();
            TextFlowUIController controller = loader.getController();
            controller.setup();
            controller.setText(ConvertedText);
            Stage convertStage = new Stage();
            convertStage.initStyle(StageStyle.UNDECORATED);
            controller.setPrimaryStage(convertStage);
            convertStage.getIcons().add(icon);
            controller.setTitle("Converted Text of " + name);
            convertStage.setScene(new Scene(root));
            convertStage.setResizable(false);
            convertStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void ApplicationLaunch(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/UI/FXML/MainUIRemake.fxml")));
            Parent root = loader.load();
            Stage newStage = new Stage();
            MainUIController ctrl = loader.getController();
            ctrl.setPrimaryStage(newStage);
            newStage.getIcons().add(icon);
            newStage.setTitle("Untitled Sketch    |    Python Arduino IDE");
            ctrl.setTitle("Untitled Sketch    |    Python Arduino IDE");
            newStage.setScene(new Scene(root));
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.setResizable(false);
            newStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void Open(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Arduino Python Code");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPython files", "*.jpy"));
        File file = chooser.showOpenDialog(stage);
        if (file != null && file.exists()) {
            fileLocation = file.getAbsolutePath();
            saved = true;
            name = file.getName();
            setTitle(name + "    |    Python Arduino IDE");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                codeArea.setText(sb.toString());
                setPythonCode();
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        UpdateSaveIndicator();
    }

    @FXML
    public void OpenFolder(ActionEvent event) {
        String directoryLocation = getFolderLocation("Select The Python Folder");
        String PythonFileLocation = null;
        if (directoryLocation != null) {
            try {
                for (String listedFileLocations : listAllFiles(directoryLocation)) {
                    if (listedFileLocations.contains(".py") && !(listedFileLocations.contains("arduino.py"))) {
                        PythonFileLocation = listedFileLocations;
                        break;
                    }
                }
                if (PythonFileLocation != null) {
                    BufferedReader BR = new BufferedReader(new FileReader(PythonFileLocation));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = BR.readLine()) != null) {
                        if (!line.equals("from arduino import *")) {
                            builder.append(line).append('\n');
                        }
                    }
                    codeArea.setText(builder.toString());
                    name = new File(PythonFileLocation).getName();
                    saved = true;
                    stage.setTitle(name + "    |    Python Arduino IDE");
                    upToDate = true;
                    fileLocation = PythonFileLocation;
                    UpdateSaveIndicator();
                    BR.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void OpenINO(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Arduino Code");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("INO files", "*.ino"));
        File file = chooser.showOpenDialog(stage);
        if (file != null && file.exists()) {
            saved = false;
            name = file.getName();
            setTitle(name.replace("ino", "jpy") + "    |    Python Arduino IDE");
            try {
                CppConverter converter = new CppConverter(file);
                converter.Convert();
                codeArea.setText(converter.getConvertedText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        UpdateSaveIndicator();
    }

    private Vector<String> listAllFiles(String directoryPath) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(directoryPath))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(Vector::new));
        } catch (IOException e) {
            return new Vector<>();
        }
    }

    private void setPythonCode() {
        if(fileLocation != null) {
            String raw = codeArea.getText();
            upToDate = true;
            String pythonCode = raw.replaceAll("\\t", "\t");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation));
                if (!name.contains(".jpy")) {
                    writer.write("from arduino import *\n");
                }
                writer.write(pythonCode);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UpdateSaveIndicator();
        }
    }

    private void loadSaver() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Python Code");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPython file", "*.jpy"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            fileLocation = file.getAbsolutePath();
            name = file.getName();
            setTitle(name + "    |    Python Arduino IDE");
            if (!new File(fileLocation).exists()) {
                try {
                    new File(fileLocation).createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            saved = true;
        }
        UpdateSaveIndicator();
    }

    @FXML
    private void AddKeyWord(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/FXML/KeyWordsUI.fxml"));
        Parent root = loader.load();
        KeyWordsController controller = loader.getController();
        Stage stage = new Stage();
        stage.getIcons().add(icon);
        stage.setTitle("Manage KeyWords");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        controller.setController(this);
        if (commandMap != null) {
            controller.setKeyWords(commandMap);
        } else {
            commandMap = new HashMap<>();
            commandMapSetup();
            controller.setKeyWords(commandMap);
        }
        stage.show();
        controller.setPrimaryStage(stage);
    }

    @FXML
    private void AddKeyWordFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Get KeyWords Through .txt Files");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            String textFileLocation = file.getAbsolutePath();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(textFileLocation));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("\t")) {
                        String[] keyWords = line.split("\t", 2);
                        if (commandMap == null) {
                            commandMap = new HashMap<>();
                            commandMap.put(keyWords[0].strip(), keyWords[1].strip());
                            setCommandMap(commandMap);
                        } else {
                            commandMap.put(keyWords[0].strip(), keyWords[1].strip());
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @FXML
    private void OpenErrorList(ActionEvent event) {
        if (errorList.isEmpty()) CheckErrors();

        if (errorList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error Detection Results:");
            alert.setHeaderText("No Error Detected");
            alert.show();
        } else {
            Stage newstage = new Stage();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/Error/UI/ErrorScreen.fxml")));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            newstage.setScene(new Scene(root));
            ErrorScreenController ctrl = loader.getController();
            ctrl.SetList(errorList);
            newstage.setResizable(false);
            newstage.setTitle("File Errors");
            newstage.getIcons().add(icon);
            newstage.show();
        }
    }

    @FXML
    private void ShowErrorButton(){
        ClearErrorList(null);
        OpenErrorList(null);
    }

    private void CheckErrors() {
        ErrorIdentifier id = new ErrorIdentifier(codeArea.getText());
        errorList.addAll(id.identify());
    }

    @FXML
    private void ClearErrorList(ActionEvent event) {
        errorList = new Vector<>();
    }

    @FXML
    private void OpenProfile(ActionEvent event) {
        if (userProfile == null) {
            userProfile = getUser();
        }
        if (userProfile != null) {
            Stage newstage = new Stage();
            FXMLLoader loader = new FXMLLoader((getClass().getResource("/Profile/UI/ProfileUI.fxml")));
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            newstage.setScene(new Scene(root));
            ProfileUIController ctrl = loader.getController();
            ctrl.initialize(userProfile, newstage);
            ctrl.setPrimaryStage(stage);
            newstage.setResizable(false);
            newstage.initStyle(StageStyle.UNDECORATED);
            newstage.setTitle("Your Profile");
            newstage.getIcons().add(icon);
            newstage.show();
        }
    }

    private ArduinoProfile getUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Profile/ProfileControlUI/AccountManager/ChooseAction.fxml")
            );
            Parent root = loader.load();

            Stage choiceStage = new Stage();
            choiceStage.setScene(new Scene(root));
            choiceStage.initStyle(StageStyle.UNDECORATED);
            choiceStage.setResizable(false);

            ChooseActionController controller = loader.getController();
            controller.setStage(choiceStage);

            choiceStage.showAndWait();

            ChooseActionController.Action action = controller.getChosenAction();
            if (action == ChooseActionController.Action.CANCEL) {
                return null;
            }

            if (action == ChooseActionController.Action.CREATE) {
                return createNewProfile();
            } else {
                return login();
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArduinoProfile createNewProfile() throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();
        Stage newstage = new Stage();
        URL fxmlUrl = getClass().getResource("/Profile/ProfileControlUI/NewAccount/CreateProfile.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newstage.setScene(new Scene(root));
        CreateController ctrl = loader.getController();
        ctrl.setPrimaryStage(newstage);
        newstage.setResizable(false);
        newstage.initStyle(StageStyle.UNDECORATED);
        newstage.setAlwaysOnTop(true);
        newstage.setTitle("Login");
        newstage.getIcons().add(icon);
        newstage.showAndWait();

        dao.saveProfile(ctrl.getProfile());

        return ctrl.getProfile();
    }

    private ArduinoProfile login() {
        Stage newstage = new Stage();
        URL fxmlUrl = getClass().getResource("/Profile/ProfileControlUI/Login/LogIn.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newstage.setScene(new Scene(root));
        LogInController ctrl = loader.getController();
        ctrl.setPrimaryStage(newstage);
        newstage.setResizable(false);
        newstage.initStyle(StageStyle.UNDECORATED);
        newstage.setAlwaysOnTop(true);
        newstage.setTitle("Login");
        newstage.getIcons().add(icon);
        newstage.showAndWait();

        return ctrl.getProfile();
    }

    @FXML
    private void OpenList(ActionEvent event) throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();

        int userID = 1;
        Vector<ArduinoProfile> profileVector = new Vector<>();

        while (dao.getProfileThroughID(userID) != null) {
            profileVector.add(dao.getProfileThroughID(userID));
            userID++;
        }

        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/Profile/UI/UserListUI.fxml")));
        Parent root = loader.load();
        newStage.setScene(new Scene(root));
        UserListUIController ctrl = loader.getController();
        ctrl.setProfiles(profileVector, newStage);
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setResizable(false);
        newStage.getIcons().add(icon);
        newStage.setTitle("User List");
        newStage.show();
    }

    @FXML
    private void OpenProjectList(ActionEvent actionEvent) throws IOException, SQLException {
        ProfileDAO dao = new ProfileDAO();

        int userID = 1;
        Vector<ArduinoProfile> profileVector = new Vector<>();

        while (dao.getProfileThroughID(userID) != null) {
            profileVector.add(dao.getProfileThroughID(userID));
            userID++;
        }

        Vector<ArduinoProfile.Project> projects = new Vector<>();
        for(ArduinoProfile profile : profileVector){
            projects.addAll(profile.getProjects());
        }

        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/Profile/UI/ProjectList.fxml")));
        Parent root = loader.load();
        newStage.setScene(new Scene(root));
        ProjectListController ctrl = loader.getController();
        ctrl.setProjects(projects, newStage);
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setResizable(false);
        newStage.getIcons().add(icon);
        newStage.setTitle("User List");
        newStage.show();
    }

    @FXML
    private void OpenID(ActionEvent event) {
        Stage newstage = new Stage();
        URL fxmlUrl = getClass().getResource("/Profile/ProfileControlUI/GetID/GetID.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newstage.setScene(new Scene(root));
        GetIDController ctrl = loader.getController();
        ctrl.setPrimaryStage(newstage);
        newstage.setResizable(false);
        newstage.initStyle(StageStyle.UNDECORATED);
        newstage.setAlwaysOnTop(true);
        newstage.setTitle("get ID");
        newstage.getIcons().add(icon);
        newstage.showAndWait();

        ArduinoProfile profile = ctrl.getProfile();

        if (profile != null) {
            Stage newStage = new Stage();
            FXMLLoader profileLoader = new FXMLLoader((getClass().getResource("/Profile/UI/ProfileUI.fxml")));
            Parent profileRoot = null;
            try {
                profileRoot = profileLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            newStage.setScene(new Scene(profileRoot));
            ProfileUIController accountController = profileLoader.getController();
            accountController.initialize(profile, newStage);
            newStage.initStyle(StageStyle.UNDECORATED);
            accountController.setPrimaryStage(newStage);
            newStage.setResizable(false);
            newStage.getIcons().add(icon);
            newStage.setTitle(profile.getUserName());
            newStage.show();
        }
    }

    @FXML
    private void SaveProject(ActionEvent event) throws SQLException, IOException {
        if (userProfile == null) {
            userProfile = getUser();
        }
        if (userProfile == null) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Profile/ProfileControlUI/NewProject/NewProject.fxml")
        );
        Parent root = loader.load();

        Stage newStage =  new Stage();

        NewProjectController controller = loader.getController();
        controller.setPrimaryStage(newStage);
        controller.setUserProfile(userProfile);
        controller.setInfo(codeArea.getText());

        Scene scene = new Scene(root);
        newStage.setScene(scene);
        newStage.setResizable(false);
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setAlwaysOnTop(true);
        newStage.setTitle("New Project");
        newStage.showAndWait();
    }

    private void UserSettings() throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();

        if (userProfile == null) {
            userProfile = getUser();
        }

        if (userProfile != null) {
            Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
            choice.setTitle("User Settings");
            choice.setHeaderText("What would you like to change?");
            ButtonType btnUsername = new ButtonType("Username");
            ButtonType btnPassword = new ButtonType("Password");
            ButtonType btnPicture = new ButtonType("Profile Picture");
            ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            choice.getButtonTypes().setAll(btnUsername, btnPassword, btnPicture, btnCancel);

            Optional<ButtonType> result = choice.showAndWait();
            if (result.isEmpty() || result.get() == btnCancel) {
                return;
            }

            if (result.get() == btnUsername) {
                TextInputDialog dlg = new TextInputDialog(userProfile.getUserName());
                dlg.setTitle("Change Username");
                dlg.setHeaderText("Enter new username:");
                String newUsername = dlg.showAndWait()
                        .orElseThrow(() -> new RuntimeException("Username is required"));
                userProfile.setUserName(newUsername);
            } else if (result.get() == btnPassword) {
                Dialog<ButtonType> dlg = new Dialog<>();
                dlg.setTitle("Change Password");
                dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);

                PasswordField oldPass = new PasswordField();
                PasswordField newPass = new PasswordField();
                grid.add(new Label("Old Password:"), 0, 0);
                grid.add(oldPass, 1, 0);
                grid.add(new Label("New Password:"), 0, 1);
                grid.add(newPass, 1, 1);

                dlg.getDialogPane().setContent(grid);

                Optional<ButtonType> pwResult = dlg.showAndWait();
                if (pwResult.isPresent() && pwResult.get() == ButtonType.OK) {
                    String oldPassword = oldPass.getText();
                    String newPassword = newPass.getText();
                    if (!userProfile.setPassword(newPassword, oldPassword)) {
                        throw new RuntimeException("Incorrect old password");
                    }
                } else {
                    return;
                }
            } else if (result.get() == btnPicture) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Choose Profile Picture");
                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                File file = chooser.showOpenDialog(stage);
                if (file != null) {
                    Image img = new Image(file.toURI().toString());
                    userProfile.setProfilePicture(img);
                } else {
                    return;
                }
            }

            if (dao.getProfileThroughID(userProfile.getId()) == null) {
                dao.saveProfile(userProfile);
            } else {
                dao.updateProfile(userProfile);
            }
        }
    }

    @FXML
    private void DownloadPythonFile(ActionEvent event) {
        String directoryLocation = getFolderLocation("Select Where To Download Python Library");
        if (directoryLocation != null) {
            try {
                cloneFile(new File("src/Assets/Arduino Library/arduino.py"), directoryLocation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getFolderLocation(String fileTitle) {
        String directoryLocation = null;
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(fileTitle);
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File folder = chooser.showDialog(stage);
        if (folder != null && folder.isDirectory()) {
            directoryLocation = folder.getAbsolutePath();
        }
        return directoryLocation;
    }

    private static void cloneFile(File file, String fileLocation) throws IOException {
        BufferedReader BR = new BufferedReader(new FileReader(file));
        BufferedWriter BW = new BufferedWriter(new FileWriter(fileLocation + "\\" + file.getName()));
        String line;
        while ((line = BR.readLine()) != null) {
            BW.write(line + '\n');
        }
        BW.close();
        BR.close();
    }

    @FXML
    private void Settings(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Settings/UI/Settings.fxml"));
        Parent settingsRoot = loader.load();
        SettingsController controller = loader.getController();
        if (settingsConfiguration != null)
            controller.init(settingsConfiguration.getDisplayLibraryCheck(), settingsConfiguration.getDeveloperTools());
        Stage dialog = new Stage();
        dialog.getIcons().add(icon);
        dialog.setScene(new Scene(settingsRoot));
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(true);
        controller.setPrimaryStage(dialog);
        dialog.showAndWait();

        if (controller.isSaved()) {
            settingsConfiguration = controller.getConfiguration();
            if (controller.isAccountSettingsActive()) try {
                UserSettings();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Menu devTools = new Menu("Developer Tools");
        MenuItem appErr = new MenuItem("Custom Application Error"), fileErr = new MenuItem("Custom File Error");
        appErr.setOnAction(e -> {
            CustomApplicationError();
            e.consume();
        });
        fileErr.setOnAction(e -> {
            CustomError();
            e.consume();
        });
        devTools.getItems().addAll(appErr, fileErr, new SeparatorMenuItem());

        MenuItem serialMon = new MenuItem("Serial Monitor");
        serialMon.setOnAction(e -> {
            try {
                openSerialMonitor();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        devTools.getItems().addAll(serialMon, new SeparatorMenuItem());

        MenuItem CopyClip = new MenuItem("Copy Exported Text");
        CopyClip.setOnAction(e ->{
            CopyConvert();
        });
        devTools.getItems().addAll(CopyClip, new SeparatorMenuItem());

        MenuItem ArduinoEditor = new MenuItem("Open Arduino Editor");
        ArduinoEditor.setOnAction(e ->{
            try {
                openArduinoEditor();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        devTools.getItems().add(ArduinoEditor);

        Scene scene = Menu.getScene();
        Parent root = scene.getRoot();
        Menu devSettings = new Menu("Dev Settings");
        MenuItem deleteUser = new MenuItem("Delete User");
        deleteUser.setOnAction(e -> {
            try {
                DeleteUser();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        devSettings.getItems().add(deleteUser);
        MenuItem deleteProject = new MenuItem("Delete User Project");
        deleteProject.setOnAction(e -> {
            try {
                DeleteProject();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        devSettings.getItems().add(deleteProject);
        if (settingsConfiguration != null && settingsConfiguration.getDeveloperTools() && !developerOptionsActive) {
            Menu.getMenus().addAll(devTools, devSettings);
            developerOptionsActive = true;
        } else {
            Menu.getMenus().removeAll(devTools, devSettings);
            developerOptionsActive = false;
        }
    }

    private void openSerialMonitor() throws IOException {
        Dialog<String> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Select Serial Port");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        String[] ports = SerialMonitorConnection.getPorts();
        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(ports));
        listView.setPrefSize(300, 200);

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selection = listView.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    dialog.setResult(selection);
                    dialog.close();
                }
            }
        });

        VBox content = new VBox(10, listView);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Optional<String> result = dialog.showAndWait();
        String Port = result.orElseThrow(() -> new RuntimeException("Port selection cancelled"));

        TextInputDialog dlgPass = new TextInputDialog();
        dlgPass.setTitle("Set Baud Rate");
        dlgPass.setHeaderText("Enter your desired BaudRate:");
        String BaudRate = dlgPass.showAndWait().orElseThrow(() -> new RuntimeException("Baud Rate is required"));

        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/DeveloperTools/UI/SerialMonitor.fxml")));
        Parent root = loader.load();
        newStage.setScene(new Scene(root));
        SerialMonitorController ctrl = loader.getController();
        ctrl.setPrimaryStage(newStage);
        ctrl.InsertCOM(Port, Integer.parseInt(BaudRate));
        newStage.setResizable(false);
        newStage.getIcons().add(icon);
        newStage.show();
    }

    private void CustomError() {
        TextInputDialog dlgUser = new TextInputDialog();
        dlgUser.setTitle("Set ErrorType");
        dlgUser.setHeaderText("Enter your desired ErrorType:");
        String errorType = dlgUser.showAndWait().orElseThrow(() -> new RuntimeException("ErrorType is required"));

        TextInputDialog dlgPass = new TextInputDialog();
        dlgPass.setTitle("Set Solution");
        dlgPass.setHeaderText("Enter your desired solution:");
        String solution = dlgPass.showAndWait().orElseThrow(() -> new RuntimeException("Solution is required"));

        errorList.add(new ArduinoError("Error at line " + line + ", col " + col + ": ", errorType, solution));
    }

    private void CustomApplicationError() {
        TextInputDialog dlgUser = new TextInputDialog();
        dlgUser.setTitle("Set ErrorType");
        dlgUser.setHeaderText("Enter your desired ErrorType:");
        String errorType = dlgUser.showAndWait().orElseThrow(() -> new RuntimeException("ErrorType is required"));

        TextInputDialog dlgPass = new TextInputDialog();
        dlgPass.setTitle("Set Solution");
        dlgPass.setHeaderText("Enter your desired solution:");
        String solution = dlgPass.showAndWait().orElseThrow(() -> new RuntimeException("Solution is required"));

        ArduinoError error = new ArduinoError("time: " + LocalTime.now().getHour() + " : " + LocalTime.now().getMinute(), errorType, solution);
        errorList.add(error);
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText(error.toString());
        errorAlert.showAndWait();
    }

    private void openArduinoEditor() throws IOException {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/DeveloperTools/UI/ArduinoEditor.fxml")));
        Parent root = loader.load();
        newStage.setScene(new Scene(root));
        ArduinoEditorController ctrl = loader.getController();
        ctrl.initialize(newStage, this);
        newStage.setResizable(false);
        newStage.showAndWait();
    }

    private void DeleteUser() throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();

        TextInputDialog dlgID = new TextInputDialog();
        dlgID.setTitle("Log In");
        dlgID.setHeaderText("Enter your profile ID:");
        int id = Integer.parseInt(dlgID.showAndWait()
                .orElseThrow(() -> new RuntimeException("ID is required")));

        ArduinoProfile existing = dao.getProfileThroughID(id);
        if (existing == null) {
            throw new RuntimeException("No profile found with ID " + id);
        }

        TextInputDialog dlgPass = new TextInputDialog();
        dlgPass.setTitle("Log In");
        dlgPass.setHeaderText("Enter your password:");
        String inputPass = dlgPass.showAndWait()
                .orElseThrow(() -> new RuntimeException("Password is required"));

        if (!existing.getPassword().equals(inputPass)) {
            throw new RuntimeException("Incorrect password");
        } else {
            dao.deleteProfile(existing.getId());
        }
    }

    private void DeleteProject() {
        ProfileDAO dao = new ProfileDAO();

        if (userProfile == null) {
            userProfile = getUser();
        }

        if (userProfile != null) {
            ObservableList<String> projectTitles = FXCollections.observableArrayList();
            for (ArduinoProfile.Project p : userProfile.getProjects()) {
                projectTitles.add(p.getTitle());
            }
            ListView<String> listView = new ListView<>(projectTitles);
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Delete Project");
            alert.getDialogPane().setContent(listView);
            alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            listView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    String selectedTitle = listView.getSelectionModel().getSelectedItem();
                    if (selectedTitle != null) {
                        try {
                            boolean removed = dao.removeProject(userProfile.getId(), selectedTitle);
                            if (removed) {
                                userProfile.getProjects().removeIf(p -> p.getTitle().equals(selectedTitle));
                                listView.getItems().remove(selectedTitle);
                                alert.close();
                            }
                        } catch (SQLException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            alert.show();
        }
    }

    public void CopyConvert() {
        if (!saved) {
            loadSaver();
            setPythonCode();
        }
        if (!upToDate) {
            setPythonCode();
        }
        PyConverter converter = new PyConverter(new File(fileLocation));
        if (settingsConfiguration != null) {
            if (settingsConfiguration.getDisplayLibraryCheck()) {
                converter.setLibraryDisplay(true);
            }
        }
        try {
            if (commandMap != null) {
                PyConverter.setCommandMap(commandMap);
            }
            String ConvertedText = converter.translateWithoutExporting();
            copyToClipboard(ConvertedText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    public void Logout(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (userProfile == null) {
            alert.setTitle("Account not found");
            alert.setHeaderText("Account not found");
            alert.setContentText("Account not configured");
        } else {
            alert.setTitle("Command Successful");
            alert.setHeaderText("Request Completed Successfully");
            alert.setContentText("logged Out of " + userProfile.getUserName());
            userProfile = null;
        }
        alert.show();
    }
}