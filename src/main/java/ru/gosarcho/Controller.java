package ru.gosarcho;

import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

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

    private static List<File> oldFiles = new ArrayList<>();

    private Service service = new Service();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup forwardBackToggleGroup = new ToggleGroup();
        forward.setToggleGroup(forwardBackToggleGroup);
        back.setToggleGroup(forwardBackToggleGroup);

        ToggleGroup listsPlusToggleGroup = new ToggleGroup();
        oneListPlus.setToggleGroup(listsPlusToggleGroup);
        twoListsPlus.setToggleGroup(listsPlusToggleGroup);
        oneListPlus.setSelected(true);

        String url = service.getPropertyValue(PropertyName.DB_URL);
        String user = service.getPropertyValue(PropertyName.DB_USER);
        String pass = service.getPropertyValue(PropertyName.DB_PASSWORD);
        try (Connection connection = DriverManager.getConnection(url, user, pass);
             Statement statement = connection.createStatement()
        ) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM digitization.Сводный_журнал");
            while (resultSet.next()) {
                documentName.getEntries().add(resultSet.getString("ФОД").replace("_", " "));
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
        oldFiles.clear();
        File directory = new File(directoryName);
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile() && file.getName().startsWith(startOfFileNames) && (file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG"))) {
                oldFiles.add(file);
            }
        }
    }

    @FXML
    private void renameButtonClicked() {
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
            String fod = documentName.getText().replace(" ", "_");

            listFilesInFolderWithFilter(pathToFiles.getText(), firstLettersOfOldFiles.getText());

            CopyTask copyTask = new CopyTask(
                    pathToFiles.getText(),
                    oldFiles,
                    Integer.parseInt(startSheet.getText()),
                    listsPlus,
                    fod,
                    turnover.getText()
            );
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(copyTask.progressProperty());
            copyTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, workerStateEvent -> {
                //Windows only
                try {
                    Runtime.getRuntime().exec("explorer.exe /select, " + copyTask.getValue().get(0));
                } catch (IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Файлы переименованы");
                alert.setHeaderText(null);
                alert.setContentText(copyTask.getValue().size() + " из " + oldFiles.size());
                alert.showAndWait();
            });
            new Thread(copyTask).start();
        }
    }

    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @FXML
    private void pathButtonClicked() {
        try {
            File dir = directoryChooser.showDialog(Main.getPrimaryStage());
            pathToFiles.setText(dir.getAbsolutePath());
        } catch (NullPointerException ignored) {
            //
        }
    }
}
