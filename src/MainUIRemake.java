import UI.Controllers.*;
import java.util.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class MainUIRemake extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("UI/FXML/MainUIRemake.fxml")));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        MainUIController ctrl = loader.getController();
        ctrl.setPrimaryStage(stage);
        Image icon = new Image(Objects.requireNonNull(getClass()
                .getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));
        stage.getIcons().add(icon);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Untitled Sketch    |    Python Arduino IDE");
        stage.setResizable(false);
        ctrl.countLines();
        ctrl.UpdateSaveIndicator();
        stage.show();
    }

    public static void newApplication() {
        launch();
    }

    public static void main(String[] args) {
        newApplication();
    }
}