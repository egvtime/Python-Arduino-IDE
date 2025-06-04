package Profile.ProfileControlUI.Login;

import Profile.ArduinoProfile;
import Profile.ProfileControlUI.*;
import Profile.ProfileDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class LogInController {
    @FXML private AnchorPane pane;
    @FXML private ImageView icon;
    @FXML private TextField user;
    @FXML private TextField password;
    @FXML private Button button;
    @FXML private Label error;

    private final Image ardIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));
    private Stage primaryStage;
    private ArduinoProfile profile;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        icon.setImage(ardIcon);

        user.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                password.requestFocus();
            }
        });

        password.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    Confirm(null);
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ArduinoProfile getProfile() {
        return profile;
    }

    public void setProfile(ArduinoProfile profile) {
        this.profile = profile;
    }

    @FXML
    public void Cancel(javafx.event.ActionEvent actionEvent) {
        primaryStage.close();
    }

    @FXML
    public void Confirm(javafx.event.ActionEvent actionEvent) throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();

        String username = user.getText().strip();
        String pw       = password.getText().strip();

        if (username.isEmpty() || pw.isEmpty()) {
            error.setText("All fields must be filled");
            shakeNode(button);
            throw new ProfileException("All fields must be filled");
        }

        ArduinoProfile existing = dao.getProfileThroughUsername(username);
        if (existing == null) {
            error.setText("No profile found with username: " + username);
            shakeNode(button);
            throw new ProfileException("No profile found with username: " + username);
        }

        if (!existing.getPassword().equals(pw)) {
            error.setText("Incorrect password");
            shakeNode(button);
            throw new ProfileException("Incorrect password");
        }

        profile = existing;
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
