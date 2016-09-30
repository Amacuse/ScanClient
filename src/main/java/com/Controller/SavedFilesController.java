package com.Controller;

import com.Bean.FileDTO;
import com.Bean.ContentIdAndDate;
import com.Service.Impl.RetrieveServiceImpl;
import com.Service.Interface.RetrieveService;
import com.SpringFxmlLoader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class SavedFilesController implements Initializable {
    private static final Logger LOGGER = LogManager.getLogger(SavedFilesController.class);

    private MessageSource ms = SpringFxmlLoader.getContext().getBean("messageSource", MessageSource.class);
    private RetrieveService retrieveService = SpringFxmlLoader.getContext().getBean("retrieveServiceImpl", RetrieveServiceImpl.class);

    @FXML
    private Label lbFileName;
    @FXML
    private TableView<ContentIdAndDate> tbFiles;
    @FXML
    private TableColumn<ContentIdAndDate, String> tbRowDate;
    @FXML
    private TableColumn<ContentIdAndDate, String> tbRowTime;
    @FXML
    private Button btSave;
    @FXML
    private Button btDelete;
    @FXML
    private Button btCancel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tbRowDate.setCellValueFactory(new PropertyValueFactory<ContentIdAndDate, String>("date"));
        tbRowTime.setCellValueFactory(new PropertyValueFactory<ContentIdAndDate, String>("time"));
        tbFiles.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tbFiles.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ContentIdAndDate>() {
            @Override
            public void changed(ObservableValue<? extends ContentIdAndDate> observable, ContentIdAndDate oldValue, ContentIdAndDate newValue) {
                if (newValue != null) {
                    btSave.setVisible(true);
                    btDelete.setVisible(true);
                }
            }
        });
        changeWindowLanguage();
    }

    public void getSelected() {
        ContentIdAndDate dto = tbFiles.getSelectionModel().getSelectedItem();

        if (dto != null) {
            FileDTO recoveryFile = retrieveService.getSelectedFile(dto.getId());

            if (recoveryFile != null) {

                String dirForFiles = chooseDirForFiles();

                if (!dirForFiles.isEmpty()) {
                    boolean confirm = true;
                    try {
                        saveFiles(dirForFiles, recoveryFile);
                    } catch (IOException e) {
                        confirm = false;
                        LOGGER.debug("Something has gone wrong " + e);
                    }

                    //Confirmation
                    if (confirm) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        setAlertTitles(alert,
                                ms.getMessage("savedFilesController.alert.confirmation.title",
                                        null, getLocale()),
                                ms.getMessage("savedFilesController.alert.confirmation.header",
                                        null, getLocale()),
                                ms.getMessage("savedFilesController.alert.confirmation.content",
                                        null, getLocale()));
                        alert.showAndWait();
                    }
                }
                cancel();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            setAlertTitles(alert,
                    ms.getMessage("savedFilesController.alert.noSelected.title",
                            null, getLocale()),
                    ms.getMessage("savedFilesController.alert.noSelected.header",
                            null, getLocale()),
                    ms.getMessage("savedFilesController.alert.noSelected.content",
                            null, getLocale()));
            alert.showAndWait();
        }
    }

    private String chooseDirForFiles() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setAlertTitles(alert,
                ms.getMessage("savedFilesController.alert.filesReceived.title",
                        null, getLocale()),
                ms.getMessage("savedFilesController.alert.filesReceived.header",
                        null, getLocale()),
                ms.getMessage("savedFilesController.alert.filesReceived.content",
                        null, getLocale()));
        ButtonType chooseDir = new ButtonType(
                ms.getMessage("savedFilesController.button.chooseDir", null, getLocale()));
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(chooseDir, cancel);
        Optional<ButtonType> buttonType = alert.showAndWait();
        String fileDir = "";
        if (buttonType.get() == chooseDir) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(ms.getMessage("text.settings.directoryChooserTitle", null, getLocale()));
            File directory = chooser.showDialog(btSave.getScene().getWindow());

            if (directory != null) {
                fileDir = directory.getAbsolutePath();
            }
        }
        return fileDir;
    }

    private void saveFiles(String dirForFiles, FileDTO recoveryFile) throws IOException {
        int index = lbFileName.getText().lastIndexOf(File.separator);
        String fileName = lbFileName.getText().substring(index + 1);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd$HH-mm-ss");
        FileUtils.writeByteArrayToFile(
                new File(dirForFiles,
                        dtf.print(recoveryFile.getBackUpDate()) + "$" + fileName), recoveryFile.getContent());
        LOGGER.debug("File name is ---> " + dtf.print(recoveryFile.getBackUpDate()) + "$" + fileName);
    }

    public void deleteSelected() {
        ContentIdAndDate dto = tbFiles.getSelectionModel().getSelectedItem();

        if (dto != null) {
            retrieveService.deleteSelectedFile(dto.getId());
            ObservableList<ContentIdAndDate> items = tbFiles.getItems();
            items.remove(dto);
            if (items.isEmpty()) {
                cancel();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            setAlertTitles(alert,
                    ms.getMessage("savedFilesController.alert.noSelected.title",
                            null, getLocale()),
                    ms.getMessage("savedFilesController.alert.noSelected.header",
                            null, getLocale()),
                    ms.getMessage("savedFilesController.alert.noSelected.content",
                            null, getLocale()));
            alert.showAndWait();
        }
    }

    public void changeWindowLanguage() {
        Locale currentLocale = getLocale();
        lbFileName.setText(ms.getMessage("savedFilesController.label.FileName", null, currentLocale));

        //Table
        tbRowDate.setText(ms.getMessage("savedFilesController.tableRow.date", null, currentLocale));
        tbRowTime.setText(ms.getMessage("savedFilesController.tableRow.time", null, currentLocale));
    }

    public void cancel() {
        Stage stage = (Stage) btCancel.getScene().getWindow();
        stage.close();
    }

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    private void setAlertTitles(Alert alert, String title, String header, String content) {
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
    }

}
