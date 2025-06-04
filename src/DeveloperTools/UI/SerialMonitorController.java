package DeveloperTools.UI;

import DeveloperTools.SerialMonitor.*;
import java.awt.event.*;
import java.io.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class SerialMonitorController {
    @FXML private TextFlow SerialMessages;
    @FXML private TextField MessageField;
    @FXML private Button Send;

    private boolean stopped = false;

    private final SerialMonitorConnection connection = new SerialMonitorConnection();
    private Stage primaryStage;

    public void InsertCOM(String COM, int BaudRate) throws SerialException{
        Thread SMRunning = new Thread(() -> {
            while (!stopped) {
                String line = connection.readLine();
                if (line != null) {
                    Platform.runLater(() -> SerialMessages.getChildren().add(new Text(">\t" + line + "\n")));
                }
            }
        });
        boolean connected = connection.open(COM, BaudRate);
        if (connected) {
            SMRunning.start();
        } else {
            throw new SerialException("User Couldn't Connect");
        }

        Send.setOnAction(e -> {
            String message = MessageField.getText();
            try {
                connection.write(message);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            MessageField.clear();
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setOnCloseRequest(event -> {
            event.consume();
            stopped = true;
            connection.close();
            primaryStage.close();
        });
    }

    public void SendMessage(javafx.event.ActionEvent actionEvent) {
        try {
            connection.write(MessageField.getText());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MessageField.setText("");
    }
}
