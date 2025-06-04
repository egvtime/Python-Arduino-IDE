package Profile.UI;

import Profile.ArduinoProfile.Project;
import UI.Controllers.*;
import java.io.IOException;
import java.util.Vector;
import java.util.Comparator;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProjectListController {
    @FXML private Button menuClose;
    @FXML private AnchorPane titleBar;
    @FXML private Button menuMinimize;
    @FXML private TextField searchField;
    @FXML private ListView<Project> listView;

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage;

    private final Image icon = new Image(Objects.requireNonNull(
            getClass().getResourceAsStream("/Assets/Icons/Py Arduino IDE Icon.png")
    ));

    private final ObservableList<Project> masterList = FXCollections.observableArrayList();
    private FilteredList<Project> filteredList;

    public void setProjects(Vector<Project> projects, Stage stage) {
        masterList.setAll(projects);
        FXCollections.sort(masterList, new ProjectTitleComparator());
        filteredList = new FilteredList<>(masterList, p -> true);
        listView.setItems(filteredList);

        this.stage = stage;
        menuClose.setOnMouseEntered(e -> menuClose.setStyle("-fx-background-color: red;"));
        menuClose.setOnMouseExited(e -> menuClose.setStyle("-fx-background-color: #171e21;"));
        menuMinimize.setOnMouseEntered(e -> menuMinimize.setStyle("-fx-background-color: gray;"));
        menuMinimize.setOnMouseExited(e -> menuMinimize.setStyle("-fx-background-color: #171e21;"));
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
        listView.setCellFactory(lv -> new ListCell<>() {
            private final Label titleLabel = new Label();
            private final Label descLabel = new Label();
            private final AnchorPane container = new AnchorPane();

            {
                titleLabel.getStyleClass().add("project-title");
                descLabel.getStyleClass().add("project-info");
                titleLabel.setLayoutX(5);
                titleLabel.setLayoutY(5);
                descLabel.setLayoutX(5);
                descLabel.setLayoutY(25);
                container.getChildren().addAll(titleLabel, descLabel);
                container.setPrefHeight(60);
            }

            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titleLabel.setText(project.getTitle());
                    descLabel.setText(project.getDescription());
                    setGraphic(container);
                }
            }
        });

        listView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Project selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openProject(selected);
                }
            }
        });
    }

    @FXML
    private void applyFilter() {
        String text = searchField.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            filteredList.setPredicate(p -> true);
        } else {
            filteredList.setPredicate(project ->
                    project.getTitle().toLowerCase().contains(text) ||
                            project.getDescription().toLowerCase().contains(text)
            );
        }
    }

    @FXML
    public void CloseScreen(ActionEvent actionEvent) {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void foldScreen(ActionEvent actionEvent) {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    private void openProject(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/UI/FXML/MainUIRemake.fxml")
            );
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

    public static class ProjectTitleComparator implements Comparator<Project> {
        @Override
        public int compare(Project a, Project b) {
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        }
    }
}
