package ru.gosarcho;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextField path;
    @FXML
    private TextField fond;
    @FXML
    private TextField op;
    @FXML
    private TextField document;
    @FXML
    private RadioButton forward;
    @FXML
    private RadioButton back;
    @FXML
    private Button rename;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup toggleGroup = new ToggleGroup();
        forward.setToggleGroup(toggleGroup);
        back.setToggleGroup(toggleGroup);
    }

    @FXML
    private void RenameButtonClicked(ActionEvent event) {
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    private void PathButtonClicked(ActionEvent event) {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
        }
    }
}