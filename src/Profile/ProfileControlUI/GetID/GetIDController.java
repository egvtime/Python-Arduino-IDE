package Profile.ProfileControlUI.GetID;

import Profile.*;
import Profile.ProfileControlUI.*;
import java.io.*;
import java.sql.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;

public class GetIDController {
    @FXML private TextField idField;
    @FXML private Label error;
    @FXML private Button search;
    private ArduinoProfile profile;
    private Stage primaryStage;

    @FXML
    public void SetID(ActionEvent actionEvent) {
        if(idField.getText().isEmpty()){
            error.setText("All Parameters Must Be Filled");
            shakeNode(search);
            throw new ProfileException("All Parameters Must Be Filled");
        }
        ProfileDAO dao = new ProfileDAO();
        try {
            profile = dao.getProfileThroughID(Integer.parseInt(idField.getText()));
            if(profile == null){
                error.setText("No Account By That ID");
                shakeNode(search);
                throw new ProfileException("Account Handle Missing");
            }else{
                primaryStage.close();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArduinoProfile getProfile() {
        return profile;
    }

    public void setProfile(ArduinoProfile profile) {
        this.profile = profile;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        idField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.ENTER) {
                SetID(new ActionEvent());
            }
        });
    }

    private void shakeNode(javafx.scene.Node node) {
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(50), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    public void Cancel(ActionEvent actionEvent) {
        primaryStage.close();
    }
}
