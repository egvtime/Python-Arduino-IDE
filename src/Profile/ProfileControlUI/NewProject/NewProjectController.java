package Profile.ProfileControlUI.NewProject;

import Profile.*;
import Profile.ProfileControlUI.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javafx.animation.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;

public class NewProjectController {
    @FXML private ImageView icon;
    @FXML private TextField title;
    @FXML private TextField description;
    @FXML private Button button;
    @FXML private Label error;

    private String info;

    private final Image ardIcon = new Image(
            Objects.requireNonNull(
                    getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")
            )
    );
    private Stage primaryStage;
    private ArduinoProfile userProfile;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        icon.setImage(ardIcon);

        title.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                description.requestFocus();
            }
        });

        description.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    Confirm(null);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setUserProfile(ArduinoProfile profile) {
        this.userProfile = profile;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    public void Cancel(javafx.event.ActionEvent actionEvent) {
        primaryStage.close();
    }

    @FXML
    public void Confirm(javafx.event.ActionEvent actionEvent) throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();

        if (userProfile == null) {
            throw new ProfileException("No user is logged in.");
        }

        String projTitle = title.getText().strip();
        String projDesc  = description.getText().strip();

        if (projTitle.isEmpty() || projDesc.isEmpty()) {
            error.setText("Both Title and Description are required");
            shakeNode(button);
            throw new ProfileException("Title or Description missing");
        }

        Vector<ArduinoProfile.Project> projects = userProfile.getProjects();
        if (projects == null) {
            projects = new Vector<>();
        }
        projects.add(new ArduinoProfile.Project(projTitle, info, projDesc));
        userProfile.setProjects(projects);

        if (dao.getProfileThroughID(userProfile.getId()) == null) {
            dao.saveProfile(userProfile);
        } else {
            dao.updateProfile(userProfile);
        }

        primaryStage.close();
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
}