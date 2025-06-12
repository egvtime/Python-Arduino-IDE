package Error.UI;

import java.util.Vector;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.*;
import Error.ArduinoError;

public class ErrorScreenController {
    @FXML
    private ListView<ArduinoError> ErrorList;

    public void SetList(Vector<ArduinoError> List) {
        ErrorList.setItems(FXCollections.observableArrayList(List));
    }
}
