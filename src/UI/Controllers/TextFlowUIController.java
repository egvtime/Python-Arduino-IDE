package UI.Controllers;

import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Objects;
import java.util.Vector;
import javafx.stage.*;

public class TextFlowUIController {
    @FXML private AnchorPane titleBar;
    @FXML private Label Title;
    @FXML private ImageView menuIcon;
    @FXML private Button menuClose;
    @FXML private Button menuMinimize;

    @FXML private TextFlow textFlow;

    private final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));

    private double xOffset = 0;
    private double yOffset = 0;

    private Vector<String> blueWords = new Vector<>();
    private Vector<String> orangeWords = new Vector<>();
    private Vector<String> redWords = new Vector<>();

    private Stage primaryStage;

    public void setup() {
        blueWords.add("void");
        blueWords.add("return");

        orangeWords.add("String");
        orangeWords.add("double");
        orangeWords.add("int");
        orangeWords.add("byte");
        orangeWords.add("char");
        orangeWords.add("bool");

        menuIcon.setImage(icon);
        menuClose.setOnMouseEntered(e -> menuClose.setStyle("-fx-background-color: red;"));
        menuClose.setOnMouseExited(e -> menuClose.setStyle("-fx-background-color: #1E2528;"));
        menuMinimize.setOnMouseEntered(e -> menuMinimize.setStyle("-fx-background-color: gray;"));
        menuMinimize.setOnMouseExited(e -> menuMinimize.setStyle("-fx-background-color: #1E2528;"));
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void customOrangeWords(Vector<String> keyWords){
        orangeWords.addAll(keyWords);
    }

    public void customBlueWords(Vector<String> keyWords){
        blueWords.addAll(keyWords);
    }

    public void customRedWords(Vector<String> keyWords){
        redWords.addAll(keyWords);
    }

    public void removeOrangeWord(String keyWord){
        orangeWords.remove(keyWord);
    }

    public void removeBLueWord(String keyWord){
        blueWords.remove(keyWord);
    }

    public void removeRedWord(String keyWord) {
        redWords.remove(keyWord);
    }

    public void addOrangeWord(String keyWord) {
        orangeWords.add(keyWord);
    }

    public void addBLueWord(String keyWord){
        blueWords.add(keyWord);
    }

    public void addRedWord(String keyWord){
        redWords.add(keyWord);
    }

    public Vector<String> getOrangeWords() {
        return orangeWords;
    }

    public Vector<String> getBlueWords() {
        return blueWords;
    }

    public Vector<String> getRedWords() {
        return redWords;
    }

    public void clearOrangeWord(){
        orangeWords = new Vector<>();
    }

    public void clearBlueWord(){
        blueWords = new Vector<>();
    }

    public void clearRedWords(){
        redWords = new Vector<>();
    }

    @FXML
    public void CloseScreen(ActionEvent actionEvent) {
        primaryStage.close();
    }

    @FXML
    public void foldScreen(ActionEvent actionEvent) {
        primaryStage.setIconified(true);
    }

    public void clearAllKeyWords(){
        clearOrangeWord();
        clearBlueWord();
        clearRedWords();
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public void setTitle(String titleText) {
        Title.setText(titleText);
    }

    public void setText(String fulltext){
        Vector<Text> textList = new Vector<>();
        boolean inComment = false;
        for(String line : fulltext.lines().toList()) {
            Text newText = null;
            for (String word : line.split(" ")) {
                newText = new Text(word + " ");
                if(!inComment) {
                    if (word.replace("\t", "").contains("//")) {
                        inComment = true;
                        newText.setFill(Color.GRAY);
                    } else if (blueWords.contains(word.replace("\t", ""))) {
                        newText.setFill(Color.web("#0ca1a2"));
                    } else if (orangeWords.contains(word.replace("\t", ""))) {
                        newText.setFill(Color.web("#f39c12"));
                    }else if(redWords.contains(word.replace("\t", ""))) {
                        newText.setFill(Color.web(String.valueOf(Color.RED)));
                    }
                    else{
                        newText.setFill(Color.WHITE);
                    }
                    newText.setFont(Font.font("Yu Gothic Regular", FontPosture.REGULAR, 14));
                    textList.add(newText);
                }else{
                    newText.setFill(Color.GRAY);
                    newText.setFont(Font.font("Yu Gothic Regular", FontPosture.REGULAR, 14));
                    textList.add(newText);
                }
            }
            if(!Objects.requireNonNull(newText).getText().isBlank()){
            textList.add(new Text("\n"));
            }
            inComment = false;
        }
        for(Text text : textList){
            textFlow.getChildren().add(text);
        }
    }
}
