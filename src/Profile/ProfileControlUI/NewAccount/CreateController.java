package Profile.ProfileControlUI.NewAccount;

import Profile.ArduinoProfile;
import Profile.ProfileControlUI.*;
import Profile.ProfileDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Vector;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class CreateController {
    @FXML private ImageView icon;
    @FXML private TextField user;
    @FXML private TextField password;
    @FXML private TextField passwordRepeat;
    @FXML private Button button;
    @FXML private Label error;

    private final Image ardIcon = new Image(
            Objects.requireNonNull(
                    getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")
            )
    );
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
                passwordRepeat.requestFocus();
            }
        });
        passwordRepeat.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    Confirm(null);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
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

    @FXML
    public void Cancel(javafx.event.ActionEvent actionEvent) {
        primaryStage.close();
    }

    @FXML
    public void Confirm(javafx.event.ActionEvent actionEvent) throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();

        String username       = user.getText().strip();
        String pw             = password.getText().strip();
        String pwRepeat       = passwordRepeat.getText().strip();

        if (username.isEmpty() || pw.isEmpty() || pwRepeat.isEmpty()) {
            error.setText("All fields must be filled");
            shakeNode(button);
            throw new ProfileException("All fields must be filled");
        }

        if (!pw.equals(pwRepeat)) {
            error.setText("Passwords don’t match");
            shakeNode(button);
            throw new ProfileException("Passwords don’t match");
        }

        if (dao.usernameExists(username)) {
            error.setText("Username is already taken");
            shakeNode(button);
            throw new ProfileException("Username is already taken");
        }

        int nextId = dao.getHighestID() + 1;
        profile = new ArduinoProfile(nextId, username, pw, new Vector<>());

        boolean saved = dao.saveProfile(profile);
        if (!saved) {
            error.setText("Could not save new profile (username may already exist)");
            shakeNode(button);
            throw new ProfileException("Could not save new profile");
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
