package ru.gosarcho;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CopyTask extends Task<List<File>> {
    private String targetDirectory;
    private List<File> oldFiles;
    private int pageNumber;
    private int listsPlus;
    private String fod;
    private String turnover;

    CopyTask(
            String targetDirectory,
            List<File> oldFiles,
            int pageNumber,
            int listsPlus,
            String fod,
            String turnover) {
        this.targetDirectory = targetDirectory;
        this.oldFiles = oldFiles;
        this.pageNumber = pageNumber;
        this.listsPlus = listsPlus;
        this.fod = fod;
        this.turnover = turnover;
    }

    @Override
    protected List<File> call() throws Exception {
        List<File> copiedFiles = new ArrayList<>();
        String page;
        int i = 0;
        for (File file : oldFiles) {
            if (pageNumber < 10) {
                page = "00" + pageNumber;
            } else if (pageNumber < 100) {
                page = "0" + pageNumber;
            } else {
                page = String.valueOf(pageNumber);
            }

            if (!turnover.isEmpty()) {
                if (turnover.matches("/d")) {
                    page += "-" + turnover;
                } else {
                    page += turnover;
                }
            }

            pageNumber += listsPlus;

            try {
                File newName = new File(targetDirectory + "\\" + fod + "_" + page + ".jpg");
                Files.move(file.toPath(), newName.toPath(), REPLACE_EXISTING);
                copiedFiles.add(newName);
                i++;
                this.updateProgress(i, oldFiles.size());
//                    Alert alert = new Alert(AlertType.INFORMATION);
//                    alert.setTitle("Файл переименован");
//                    alert.setHeaderText(null);
//                    alert.setContentText(file.getName() + " => " + newName.getName() + "\n" + newName.getParent());
//                    alert.showAndWait();
            } catch (IOException e) {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText(e.getMessage());
//                alert.showAndWait();
            }

        }

        return copiedFiles;
    }
}
