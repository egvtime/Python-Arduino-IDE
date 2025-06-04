package Profile.ProfileControlUI.AccountManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ChooseActionController {
    private Stage stage;
    private Action chosenAction = Action.CANCEL;

    @FXML private Button btnCreate;
    @FXML private Button btnLogin;
    @FXML private Button btnCancel;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Action getChosenAction() {
        return chosenAction;
    }

    @FXML
    private void onCreate() {
        chosenAction = Action.CREATE;
        stage.close();
    }

    @FXML
    private void onLogin() {
        chosenAction = Action.LOGIN;
        stage.close();
    }

    @FXML
    private void onCancel() {
        chosenAction = Action.CANCEL;
        stage.close();
    }

    public enum Action {
        CREATE, LOGIN, CANCEL
    }
}
