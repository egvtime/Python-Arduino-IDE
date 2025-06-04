package Profile.UI;

import Profile.*;
import UI.Controllers.*;
import java.io.*;
import java.util.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class ProfileUIController {
    @FXML private AnchorPane titleBar;
    @FXML private Button menuClose;
    @FXML private Button menuMinimize;
    @FXML private Label usernameTitle;

    @FXML private Label username;
    @FXML private Label id;
    @FXML private ImageView imageView;
    @FXML private ListView<ArduinoProfile.Project> projects;

    private Stage stage;

    private final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));
    private Stage primaryStage;
    private Vector<ArduinoProfile.Project> profileProjects = new Vector<>();

    private double xOffset = 0;
    private double yOffset = 0;

    public void initialize(ArduinoProfile profile, Stage stage) {
        this.stage = stage;
        menuClose.setOnMouseEntered(e -> menuClose.setStyle("-fx-background-color: red;"));
        menuClose.setOnMouseExited(e -> menuClose.setStyle("-fx-background-color: #171e21;"));
        menuMinimize.setOnMouseEntered(e -> menuMinimize.setStyle("-fx-background-color: gray;"));
        menuMinimize.setOnMouseExited(e -> menuMinimize.setStyle("-fx-background-color:  #171e21;"));
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        username.setText(profile.getUserName());
        usernameTitle.setText(username.getText());
        id.setText("#" + profile.getId());

        if (profile.getProfilePicture() == null) {
            imageView.setImage(new Image("Assets/Icons/Blank Profile Picture Icon.png"));
        } else {
            imageView.setImage(profile.getProfilePicture());
        }

        profileProjects = profile.getProjects();

        ObservableList<ArduinoProfile.Project> projectList = FXCollections.observableArrayList(profile.getProjects());

        projects.setCellFactory(listView -> new ListCell<ArduinoProfile.Project>() {
            @Override
            protected void updateItem(ArduinoProfile.Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                } else {
                    setText(project.getTitle() + " - " + project.getDescription());
                }
            }
        });

        projects.setItems(projectList);

        projects.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                ArduinoProfile.Project selected = projects.getSelectionModel().getSelectedItem();
                openProject(selected);
                primaryStage.close();
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Vector<ArduinoProfile.Project> getProfileProjects() {
        return profileProjects;
    }

    public void setProfileProjects(Vector<ArduinoProfile.Project> profileProjects) {
        this.profileProjects = profileProjects;
    }

    private void openProject(ArduinoProfile.Project project){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UI/FXML/MainUIRemake.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            MainUIController ctrl = loader.getController();
            ctrl.setPrimaryStage(newStage);
            newStage.getIcons().add(icon);
            newStage.setTitle(project.getTitle() + "    |    Python Arduino IDE");
            ctrl.setTitle(project.getTitle() + "    |    Python Arduino IDE");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.initStyle(StageStyle.UNDECORATED);
            ctrl.setUpToDate(false);
            ctrl.setTextAreaText(project.getInfo());
            newStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void CloseScreen(ActionEvent actionEvent) {
        stage.close();
    }

    @FXML
    public void foldScreen(ActionEvent actionEvent) {
        stage.setIconified(true);
    }
}
