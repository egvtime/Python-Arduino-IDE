package UI.Controllers;

import Profile.*;
import Profile.ProfileControlUI.AccountManager.*;
import Profile.ProfileControlUI.Login.*;
import Profile.ProfileControlUI.NewAccount.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class StartScreenController {
    @FXML private AnchorPane titleBar;
    @FXML private ImageView menuIcon;
    @FXML private Button menuClose;
    @FXML private Button menuMinimize;
    @FXML public TextField Search;
    @FXML public Button NewButton;
    @FXML public Button OpenJPY;
    @FXML public Button OpenFolder;
    @FXML public ListView<ArduinoProfile.Project> projects;
    @FXML private ImageView bigMenuIcon;
    @FXML private Button username;
    @FXML private ImageView Profile;
    @FXML public javafx.scene.control.Label ProjectAmount;

    private final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));
    private final Image pfp = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Blank Profile Picture Icon.png")));
    private Stage primaryStage;
    private ArduinoProfile profile;
    private FilteredList<ArduinoProfile.Project> filteredList;
    private SortedList<ArduinoProfile.Project> sortedList;

    private double xOffset = 0;
    private double yOffset = 0;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        menuIcon.setImage(icon);
        bigMenuIcon.setImage(icon);
        Profile.setImage(pfp);
        Search.setStyle("-fx-text-fill: white; -fx-background-color: #171E21;");
        Search.setOnKeyTyped(e -> updateSearch());
        menuClose.setOnMouseEntered(e -> menuClose.setStyle("-fx-background-color: red;"));
        menuClose.setOnMouseExited(e -> menuClose.setStyle("-fx-background-color: #1E2528;"));
        menuMinimize.setOnMouseEntered(e -> menuMinimize.setStyle("-fx-background-color: gray;"));
        menuMinimize.setOnMouseExited(e -> menuMinimize.setStyle("-fx-background-color: #1E2528;"));
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
        projects.setStyle("-fx-control-inner-background: #171E21; -fx-background-color: #171E21;");
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @FXML
    public void CloseScreen(ActionEvent actionEvent) {
        primaryStage.close();
    }

    @FXML
    public void foldScreen(ActionEvent actionEvent) {
        primaryStage.setIconified(true);
    }

    @FXML
    public void NewProject(ActionEvent actionEvent) {
        nod(NewButton, this::openNewProject);
    }

    @FXML
    public void OpenJPY(ActionEvent actionEvent) {
        nod(OpenJPY, this::openJPY);
    }

    @FXML
    public void OpenFolder(ActionEvent actionEvent) {
        nod(OpenFolder, this::openProjectFolder);
    }

    @FXML
    public void LogIn(ActionEvent actionEvent) {
        if(profile == null) {
            nod(username, () -> {
                profile = getUser();
                if (profile != null) {
                    MainUIController.setUserProfile(profile);
                    username.setStyle("-fx-background-color: #1E2528;");
                    username.setText(profile.getUserName() + " | #" + profile.getId());
                    if (profile.getProfilePicture() == null) {
                        Profile.setImage(pfp);
                    } else {
                        Profile.setImage(profile.getProfilePicture());
                    }
                    ProjectAmount.setText(String.valueOf(profile.getProjects().size()));

                    ObservableList<ArduinoProfile.Project> projectList =
                            FXCollections.observableArrayList(profile.getProjects());

                    filteredList = new FilteredList<>(projectList, p -> true);
                    sortedList = new SortedList<>(filteredList);

                    projects.setCellFactory(listView -> new ListCell<ArduinoProfile.Project>() {
                        @Override
                        protected void updateItem(ArduinoProfile.Project project, boolean empty) {
                            super.updateItem(project, empty);
                            if (empty || project == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(project.getTitle() + " - " + project.getDescription());
                                setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-background-color: #171E21;");
                            }
                        }
                    });

                    projects.setItems(sortedList);

                    projects.setOnMouseClicked((MouseEvent event) -> {
                        if (event.getClickCount() == 2) {
                            ArduinoProfile.Project selected = projects.getSelectionModel().getSelectedItem();
                            openProject(selected);
                            primaryStage.close();
                        }
                    });
                }
            });
        }
    }

    private void openProject(ArduinoProfile.Project project) {
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

    private MainUIController newFile() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/UI/FXML/MainUIRemake.fxml")));
            Parent root = loader.load();
            Stage newStage = new Stage();
            MainUIController ctrl = loader.getController();
            ctrl.setPrimaryStage(newStage);
            newStage.getIcons().add(icon);
            newStage.setTitle("Untitled Sketch" + "    |    Python Arduino IDE");
            ctrl.setTitle("Untitled Sketch" + "    |    Python Arduino IDE");
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.show();
            return ctrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openNewProject() {
        newFile();
        primaryStage.close();
    }

    private void openJPY() {
        MainUIController ctrl = newFile();
        primaryStage.close();
        ctrl.Open(new ActionEvent());
    }

    private void openProjectFolder() {
        MainUIController ctrl = newFile();
        primaryStage.close();
        ctrl.OpenFolder(new ActionEvent());
    }

    private void updateSearch() {
        String txt = Search.getText().toLowerCase().trim();
        filteredList.setPredicate(project -> {
            if (txt.isEmpty()) return true;
            return project.getTitle().toLowerCase().contains(txt) || project.getDescription().toLowerCase().contains(txt);
        });

        if (txt.isEmpty()) {
            sortedList.setComparator(null);
        } else {
            sortedList.setComparator((a, b) -> {
                boolean aMatch = a.getTitle().toLowerCase().startsWith(txt);
                boolean bMatch = b.getTitle().toLowerCase().startsWith(txt);
                if (aMatch && !bMatch) return -1;
                if (!aMatch && bMatch) return 1;
                return new NameComparator().compare(a, b);
            });
        }
    }

    private ArduinoProfile getUser() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Profile/ProfileControlUI/AccountManager/ChooseAction.fxml")
            );
            Parent root = loader.load();

            Stage choiceStage = new Stage();
            choiceStage.setScene(new Scene(root));
            choiceStage.initStyle(StageStyle.UNDECORATED);
            choiceStage.setResizable(false);

            ChooseActionController controller = loader.getController();
            controller.setStage(choiceStage);

            choiceStage.showAndWait();

            ChooseActionController.Action action = controller.getChosenAction();
            if (action == ChooseActionController.Action.CANCEL) {
                return null;
            }

            if (action == ChooseActionController.Action.CREATE) {
                return createNewProfile();
            } else {
                return login();
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArduinoProfile createNewProfile() throws SQLException, IOException {
        ProfileDAO dao = new ProfileDAO();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile/ProfileControlUI/NewAccount/CreateProfile.fxml"));
        Parent root = loader.load();
        CreateController ctrl = loader.getController();
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setScene(new Scene(root));
        dialog.getIcons().add(icon);
        ctrl.setPrimaryStage(dialog);
        dialog.showAndWait();
        dao.saveProfile(ctrl.getProfile());
        return ctrl.getProfile();
    }

    private ArduinoProfile login() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile/ProfileControlUI/Login/LogIn.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LogInController ctrl = loader.getController();
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        ctrl.setPrimaryStage(dialog);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setScene(new Scene(root));
        dialog.getIcons().add(icon);
        dialog.showAndWait();
        return ctrl.getProfile();
    }

    private void shakeNode(Node node, Runnable after) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> Platform.runLater(after));
        tt.play();
    }

    private void nod(Node node, Runnable after) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByY(3);
        tt.setCycleCount(2);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> Platform.runLater(after));
        tt.play();
    }

    public static class NameComparator implements Comparator<ArduinoProfile.Project> {
        @Override
        public int compare(ArduinoProfile.Project a, ArduinoProfile.Project b) {
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        }
    }
}
