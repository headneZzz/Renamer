package ru.gosarcho;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextField countFiles;
    @FXML
    private TextField startSheet;
    @FXML
    private TextField firstLetters;
    @FXML
    private TextField path;
    @FXML
    private TextFieldWithAutocomplete document;
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

    private String url = "jdbc:postgresql://server:5433/archive";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup toggleGroup = new ToggleGroup();
        forward.setToggleGroup(toggleGroup);
        back.setToggleGroup(toggleGroup);
        try (Connection connection = DriverManager.getConnection(url, "admin", "adminus")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Сводный_журнал");
            while (resultSet.next()) {
                document.getEntries().add(resultSet.getString("ФОД").replaceAll("_", " "));
            }
        } catch (SQLException e) {
            errorLabel.setText("Ошибка подлючения к базе данных");
            e.printStackTrace();
        }
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