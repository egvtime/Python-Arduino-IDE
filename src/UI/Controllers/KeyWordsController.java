package UI.Controllers;

import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

public class KeyWordsController {
    @FXML public Button menuClose;
    @FXML public Button menuMinimize;
    @FXML public AnchorPane titleBar;
    @FXML private TextField pythonField;
    @FXML private TextField cppField;
    @FXML private Label warning;
    @FXML private ListView<String> view;
    @FXML private Button done;

    private static final List<String> PythonkeyWordList = Arrays.asList(
            "def setup():",
            "def loop():",
            "def",
            "run()",
            "serialprintln",
            "serialprint",
            "serialwrite",
            "serialwriteln",
            "serialbegin",
            "delay",
            "delaymicroseconds",
            "pinmode",
            "digitalwrite",
            "analogwrite",
            "digitalread",
            "analogread",
            "randomseed",
            "pulsein",
            "random",
            "constrain",
            "map",
            "max",
            "min",
            "sqrt"
    );

    private HashMap<String, String> KeyWords = new HashMap<>();
    private Stage stage;
    private MainUIController controller;

    private double xOffset = 0;
    private double yOffset = 0;

    public void setController(MainUIController controller) {
        this.controller = controller;
    }

    public HashMap<String, String> getKeyWords() {
        return KeyWords;
    }

    public void setKeyWords(HashMap<String, String> keyWords) {
        KeyWords = keyWords;
        view.getItems().clear();
        keyWords.forEach((py, cpp) ->
                view.getItems().add(py + "  ---  " + cpp)
        );
    }

    public void setPrimaryStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> {
            controller.setCommandMap(KeyWords);
        });
        menuClose.setOnMouseEntered(e -> menuClose.setStyle("-fx-background-color: red;"));
        menuClose.setOnMouseExited(e -> menuClose.setStyle("-fx-background-color:  #1E1F22;"));
        menuMinimize.setOnMouseEntered(e -> menuMinimize.setStyle("-fx-background-color: gray;"));
        menuMinimize.setOnMouseExited(e -> menuMinimize.setStyle("-fx-background-color:  #1E1F22;"));
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    @FXML
    private void initialize() {
        view.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selected = view.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String[] parts = selected.split("---");
                    String pyKey = parts[0].trim();
                    if(!(PythonkeyWordList.contains(pyKey))){
                        KeyWords.remove(pyKey);
                        warning.setTextFill(Color.WHITE);
                        warning.setText("KeyWord Deleted Successfully");
                        view.getItems().remove(selected);
                    }else{
                        warning.setTextFill(Color.RED);
                        warning.setText("Default KeyWord Cannot Be Removed");
                    }
                }
            }
        });
    }

    @FXML
    public void CloseScreen(ActionEvent actionEvent) {
        stage.close();
    }

    @FXML
    public void foldScreen(ActionEvent actionEvent) {
        stage.setIconified(true);
    }

    @FXML
    private void button(ActionEvent event) {
        String py = pythonField.getText().trim();
        String cpp = cppField.getText().trim();

        if (!py.isEmpty() && !cpp.isEmpty()) {
            KeyWords.put(py, cpp);
            view.getItems().add(py + "  ---  " + cpp);
            pythonField.clear();
            cppField.clear();
            warning.setTextFill(Color.WHITE);
            warning.setText("KeyWord Added Successfully");
        } else {
            warning.setTextFill(Color.RED);
            warning.setText("Both Commands Must Be Filled!");
            shakeNode(done);
        }
    }

    private void shakeNode(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(50), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
}
