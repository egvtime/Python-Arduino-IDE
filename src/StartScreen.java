import UI.Controllers.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class StartScreen extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Stage newstage = new Stage();;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/FXML/StartScreen.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newstage.setScene(new Scene(root));
        newstage.initStyle(StageStyle.UNDECORATED);
        Image icon = new Image(Objects.requireNonNull(getClass()
                .getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));
        newstage.getIcons().add(icon);
        StartScreenController ctrl = loader.getController();
        ctrl.setPrimaryStage(newstage);
        newstage.setTitle("Welcome To Arduino Python IDE");
        newstage.show();
    }
}
