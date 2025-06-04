package Profile.UI;

import java.io.*;
import java.util.*;

import Profile.ArduinoProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.stage.*;

public class UserListUIController {
    @FXML private Button menuClose;
    @FXML private AnchorPane titleBar;
    @FXML private Button menuMinimize;
    @FXML private ImageView iconImageView;
    @FXML private TextField searchField;
    @FXML private ListView<ArduinoProfile> listView;

    private double xOffset = 0;
    private double yOffset = 0;

    private Stage stage;

    private final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")));

    private final ObservableList<ArduinoProfile> firstList = FXCollections.observableArrayList();
    private FilteredList<ArduinoProfile> filteredList;

    public void setProfiles(Vector<ArduinoProfile> profiles, Stage stage) {
        firstList.setAll(profiles);
        FXCollections.sort(firstList, new UsernameComparator());
        filteredList = new FilteredList<>(firstList, p -> true);
        listView.setItems(filteredList);

        iconImageView.setImage(icon);
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
    }
    
    @FXML
    private void initialize() {

        listView.setCellFactory(lv -> new ListCell<ArduinoProfile>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final HBox container = new HBox(10, imageView, nameLabel);
            {
                container.setAlignment(Pos.CENTER_LEFT);
                imageView.setFitWidth(listView.getFixedCellSize() - 5);
                imageView.setFitHeight(listView.getFixedCellSize() - 5);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(ArduinoProfile profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nameLabel.setText(profile.getUserName() + " | #" + profile.getId());
                    if (profile.getProfilePicture() != null) {
                        imageView.setImage(profile.getProfilePicture());
                    } else {
                        imageView.setImage(new Image("Assets/Icons/Blank Profile Picture Icon.png"));
                    }
                    setGraphic(container);
                }
            }
        });

        listView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                ArduinoProfile selected = listView.getSelectionModel().getSelectedItem();
                Stage newStage = new Stage();
                FXMLLoader loader = new FXMLLoader((getClass().getResource("/Profile/UI/ProfileUI.fxml")));
                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                newStage.setScene(new Scene(root));
                ProfileUIController ctrl = loader.getController();
                ctrl.initialize(selected, newStage);
                ctrl.setPrimaryStage(newStage);
                newStage.setResizable(false);
                newStage.initStyle(StageStyle.UNDECORATED);
                newStage.getIcons().add(icon);
                newStage.setTitle(selected.getUserName());
                newStage.show();
            }
        });
    }

    @FXML
    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            filteredList.setPredicate(p -> true);
        } else {
            filteredList.setPredicate(profile -> profile.getUserName().toLowerCase().contains(text));
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

    public static class UsernameComparator implements Comparator<ArduinoProfile> {
        @Override
        public int compare(ArduinoProfile a, ArduinoProfile b) {
            int result = a.getUserName().compareToIgnoreCase(b.getUserName());
            if (result != 0) {
                return result;
            }
            return Integer.compare(a.getId(), b.getId());
        }
    }
}
