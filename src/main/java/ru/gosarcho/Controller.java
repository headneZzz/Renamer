package ru.gosarcho;

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
    private ComboBox<String> comboBoxExecutors;
    @FXML
    private TextField countFiles;
    @FXML
    private TextField startSheet;
    @FXML
    private TextField turnover;
    @FXML
    private TextField firstLetters;
    @FXML
    private TextField path;
    @FXML
    private TextFieldWithAutocomplete document;
    @FXML
    private RadioButton oneListsPlus;
    @FXML
    private RadioButton twoListsPlus;
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
        ToggleGroup forwardBackToggleGroup = new ToggleGroup();
        forward.setToggleGroup(forwardBackToggleGroup);
        back.setToggleGroup(forwardBackToggleGroup);

        ToggleGroup listsPlusToggleGroup = new ToggleGroup();
        oneListsPlus.setToggleGroup(listsPlusToggleGroup);
        twoListsPlus.setToggleGroup(listsPlusToggleGroup);
        oneListsPlus.setSelected(true);

        try (Connection connection = DriverManager.getConnection(url, "admin", "adminus")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Сводный_журнал");
            while (resultSet.next()) {
                document.getEntries().add(resultSet.getString("ФОД").replaceAll("_", " "));
            }
            resultSet = statement.executeQuery("SELECT * FROM digitization.Исполнители");
            while (resultSet.next()) {
                comboBoxExecutors.getItems().addAll(resultSet.getString("Исполнитель") + " :" + resultSet.getInt("Код_исполнителя"));
            }
        } catch (SQLException e) {
            errorLabel.setText("Ошибка подлючения к базе данных");
            e.printStackTrace();
        }
    }

    @FXML
    private void RenameButtonClicked() {
        if (path.getText().isEmpty()) {
            errorLabel.setText("Введите директорию");
        }
        else if (firstLetters.getText().isEmpty()) {
            errorLabel.setText("Введите первые буквы файлов");
        }
        else if (!forward.isSelected() && !back.isSelected()) {
            errorLabel.setText("Задайте порядок нумерации= вперед-назад");
        }

        else {
            errorLabel.setText("");
            int listsPlus = oneListsPlus.isSelected() ? 1 : 2;
            int countF = Integer.parseInt(countFiles.getText());
        }
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    private void PathButtonClicked() {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            path.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
        }
    }
}