package Settings.UI;

import Settings.*;
import javafx.fxml.*;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.*;
import javax.swing.*;

public class SettingsController {
    @FXML private CheckBox DisplayLibrary;
    @FXML private CheckBox DeveloperOptions;

    private Stage primaryStage;
    private boolean accountSettingsActive;
    private Boolean Saved;

    private SettingsConfiguration configuration;

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setOnCloseRequest(event -> {
            configuration = new SettingsConfiguration(DisplayLibrary.isSelected(), DeveloperOptions.isSelected());
            Saved = false;
        });
    }

    public void init(boolean libraryDisplayValue, boolean DeveloperOptions){
        DisplayLibrary.setSelected(libraryDisplayValue);
        this.DeveloperOptions.setSelected(DeveloperOptions);
        Saved = null;
    }

    public boolean getDisplayLibraryCheck() {
        return DisplayLibrary.isSelected();
    }

    public boolean getDeveloperOptions(){
        return DeveloperOptions.isSelected();
    }

    public Boolean isSaved() {
        return Saved;
    }

    @FXML
    private void LibraryClick(ActionEvent actionEvent){
        DisplayLibrary.setSelected(!DisplayLibrary.isSelected());
    }

    @FXML
    public void SaveClick(ActionEvent event) {
        Saved = true;
        configuration = new SettingsConfiguration(DisplayLibrary.isSelected(), DeveloperOptions.isSelected());
        primaryStage.close();
    }

    @FXML
    public void CancelClick(ActionEvent event){
        Saved = false;
        primaryStage.close();
    }

    @FXML
    public void AccountSettingsClick(ActionEvent event){
        accountSettingsActive = true;
        Saved = true;
        primaryStage.close();
    }

    public SettingsConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(SettingsConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean isAccountSettingsActive() {
        return accountSettingsActive;
    }

    @FXML
    public void DeveloperOptionsClick(ActionEvent actionEvent) {
        DeveloperOptions.setSelected(!DeveloperOptions.isSelected());
    }
}