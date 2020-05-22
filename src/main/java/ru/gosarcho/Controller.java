package ru.gosarcho;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller implements Initializable {
    @FXML
    private ComboBox<String> comboBoxExecutors;
    @FXML
    private TextField startSheet;
    @FXML
    private TextField turnover;
    @FXML
    private TextField firstLettersOfOldFiles;
    @FXML
    private TextField pathToFiles;
    @FXML
    private TextFieldWithAutocomplete documentName;
    @FXML
    private RadioButton oneListPlus;
    @FXML
    private RadioButton twoListsPlus;
    @FXML
    private RadioButton forward;
    @FXML
    private RadioButton back;
    @FXML
    private Button renameBtn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label errorLabel;

    private String url = "jdbc:postgresql://server:5433/archive";
    private static List<File> filesNames = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup forwardBackToggleGroup = new ToggleGroup();
        forward.setToggleGroup(forwardBackToggleGroup);
        back.setToggleGroup(forwardBackToggleGroup);

        ToggleGroup listsPlusToggleGroup = new ToggleGroup();
        oneListPlus.setToggleGroup(listsPlusToggleGroup);
        twoListsPlus.setToggleGroup(listsPlusToggleGroup);
        oneListPlus.setSelected(true);

        try (Connection connection = DriverManager.getConnection(url, "admin", "adminus")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Сводный_журнал");
            while (resultSet.next()) {
                documentName.getEntries().add(resultSet.getString("ФОД").replaceAll("_", " "));
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

    private void listFilesInFolderWithFilter(String directoryName, String startOfFileNames) {
        filesNames.clear();
        File directory = new File(directoryName);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile() && file.getName().startsWith(startOfFileNames) && (file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG"))) {
                filesNames.add(file);
            }
        }
    }

    @FXML
    private void RenameButtonClicked() {
        if (pathToFiles.getText().isEmpty()) {
            errorLabel.setText("Введите директорию");
        } else if (firstLettersOfOldFiles.getText().isEmpty()) {
            errorLabel.setText("Введите первые буквы файлов");
        } else if (!forward.isSelected() && !back.isSelected()) {
            errorLabel.setText("Задайте порядок нумерации (вперед-назад)");
        } else {
            errorLabel.setText("");
            int listsPlus = oneListPlus.isSelected() ? 1 : 2;
            listsPlus = forward.isSelected() ? listsPlus : -listsPlus;
            int sheetNumber = Integer.parseInt(startSheet.getText());
            String FOD = documentName.getText().replaceAll(" ", "_");

            listFilesInFolderWithFilter(pathToFiles.getText(), firstLettersOfOldFiles.getText());

            for (File file : filesNames) {
                String page;
                if (sheetNumber < 10) {
                    page = "00" + sheetNumber;
                } else if (sheetNumber < 100) {
                    page = "0" + sheetNumber;
                } else {
                    page = String.valueOf(sheetNumber);
                }

                if (!turnover.getText().isEmpty()) {
                    if (turnover.getText().matches("/d")) {
                        page += "-" + turnover.getText();
                    } else {
                        page += turnover.getText();
                    }
                }

                sheetNumber += listsPlus;

                try {
                    File newName = new File(pathToFiles.getText() + "\\" + FOD + "_" + page + ".jpg");
                    Files.copy(file.toPath(), newName.toPath(), REPLACE_EXISTING);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Файл переименован");
                    alert.setHeaderText(null);
                    alert.setContentText(file.getName() + " => " + newName.getName() + "\n" + newName.getParent());
                    alert.showAndWait();
                } catch (IOException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }

            }
        }
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    private void PathButtonClicked() {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            pathToFiles.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
        }
    }
}