package com.Controller;

import com.Bean.FileDTO;
import com.Bean.ContentIdAndDate;
import com.Bean.User;
import com.Bean.ScanSettings;
import com.Service.Interface.FileService;
import com.Service.Interface.RetrieveService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class MainController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

    @Autowired
    private FileService fileService;
    @Autowired
    private RetrieveService retrieveService;
    @Autowired
    private ScanSettings scanSettings;
    @Autowired
    private MessageSource ms;
    @Autowired
    private User user;

    @FXML
    private Menu menuFile;
    @FXML
    private MenuItem savedFiles;
    @FXML
    private MenuItem exit;
    @FXML
    private Menu menuSettings;
    @FXML
    private MenuItem settings;
    @FXML
    private Menu menuLanguage;
    @FXML
    private Menu language;
    @FXML
    private RadioMenuItem chEN;
    @FXML
    private RadioMenuItem chRU;
    @FXML
    private Label labelLanguage;
    @FXML
    private ImageView languageView;
    @FXML
    private TableView<FileDTO> savedFileTable;
    @FXML
    private TableColumn<FileDTO, String> tableColumn;
    @FXML
    private Button scanStart;

    @FXML
    private Button scanStop;
    @FXML
    private Label settingsTitle;
    @FXML
    private Label lbScanDir;
    @FXML
    private Label lbScanDirVal;
    @FXML
    private Label lbFileExt;
    @FXML
    private Label lbFileExtVal;
    @FXML
    private Label lbScanTime;
    @FXML
    private Label lbScanTimeVal;
    @FXML
    private Label lbTimeUnit;
    @FXML
    private ProgressBar progress;
    @FXML
    private Button show;
    @FXML
    private Button delete;
    @FXML
    private Button userAva;

    private ImageView lock;
    private ImageView unlock;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //load the icons
        lock = new ImageView("/img/lock.png");
        unlock = new ImageView("/img/unlock.png");
        //define contents for table view
        tableColumn.setCellValueFactory(new PropertyValueFactory<FileDTO, String>("fileName"));
        //define how many rows can be selected.In our case there is only one row can be selected
        savedFileTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        //add the listener to the table view.Just makes the 'show' and 'delete' buttons visible
        savedFileTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileDTO>() {
            @Override
            public void changed(ObservableValue<? extends FileDTO> observable, FileDTO oldValue, FileDTO newValue) {
                if (newValue != null) {
                    show.setVisible(true);
                    delete.setVisible(true);
                } else {
                    show.setVisible(false);
                    delete.setVisible(false);
                }
            }
        });

        //change the user icon depends on
        //whether the user is specified or not
        if (user.getId() != null) {
            userAva.setGraphic(unlock);
        } else {
            userAva.setGraphic(lock);
        }

        //determine the scanning settings
        if (scanSettings.getWorkDirectory() != null) {
            setUserLocale();
            setUserSettings();
        } else {
            setEnglish();
        }
    }

    /**
     * Start to scan
     */
    public void startScanning() throws IOException {
        //verify that the user is determined
        if (user.getId() == null) {
            setUser();
        }
        //verify that the scan settings are determined
        if (scanSettings.getWorkDirectory() == null) {
            setSettings();
        }
        if (user.getId() != null && scanSettings.getWorkDirectory() != null) {
            progress.setVisible(true);
            fileService.startScan();
        }
    }

    /**
     * Stop to scan
     */
    public void stopScanning() {
        progress.setVisible(false);
        fileService.stopScan();
    }


    /***
     * The user setup
     */
    public void setUser() {
        try {
            prepareModalityWindow("/fx/userSettings.fxml", "Title");
        } catch (IOException e) {
            /*NOP*/
        }
        //change the icon of the user
        //which means whether user is determined or not
        if (user.getId() != null) {
            userAva.setGraphic(unlock);
        } else {
            userAva.setGraphic(lock);
        }
    }

    /**
     * Window for setup a scan settings
     */
    public void setSettings() throws IOException {
        prepareModalityWindow("/fx/scanSettings.fxml",
                ms.getMessage("settingsWindowTitle", null, getLocale()));
        setUserSettings();
    }

    /**
     * Get the list of the saved files from the server
     */
    public void getSavedFiles() {
        //verify that the user is determined
        if (user.getId() == null) {
            //if it's not redirect to the user setup
            setUser();
        }
        if (user.getId() != null) {
            fileService.addAllSavedFiles(savedFileTable);
        }
    }

    /**
     * Get the saved copies of the file or remove the file from the server
     */
    public void actionWithFile(ActionEvent actionEvent) throws IOException {
        //get selected file
        FileDTO fileDTO = savedFileTable.getSelectionModel().getSelectedItem();
        if (actionEvent.getTarget() == show) {
            //retrieve all of the saved copies of the particular file
            List<ContentIdAndDate> copies = retrieveService.getFileCopies(fileDTO.getId());

            //create the modality window for the retrieved copies of the file
            Parent root = FXMLLoader.load(getClass().getResource("/fx/savedFiles.fxml"));
            Stage stage = new Stage();
            stage.setTitle(ms.getMessage("savedFilesController.title", null, getLocale()));
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(scanStart.getScene().getWindow());

            //adding data to the new creating window
            ObservableList<Node> childrenUnmodifiable = root.getChildrenUnmodifiable();
            for (Node node : childrenUnmodifiable) {
                if (node.getId() != null) {
                    if (node.getId().equals("lbFileName")) {
                        ((Label) node).setText(fileDTO.getFileName());
                    } else if (node.getId().equals("tbFiles")) {
                        ((TableView<ContentIdAndDate>) node).getItems().addAll(copies);
                    }
                }
            }
            stage.showAndWait();
        } else {
            //remove the file from the server
            fileService.deleteFile(fileDTO.getId());
            //remove the file from the table view
            savedFileTable.getItems().remove(fileDTO);
        }
    }

    /**
     * Set english language
     */
    public void setEnglish() {
        languageView.setImage(new Image("/img/en.png"));
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        scanSettings.setLocale(Locale.ENGLISH);
        changeWindowLanguage();
    }

    /**
     * Set russian language
     */
    public void setRussian() {
        languageView.setImage(new Image("/img/ru.png"));
        Locale locale = new Locale("ru", "RU");
        LocaleContextHolder.setLocale(locale);
        scanSettings.setLocale(locale);
        changeWindowLanguage();
    }

    /**
     * Setup the user settings
     */
    public void setUserSettings() {
        if (scanSettings.getWorkDirectory() != null) {
            lbScanDirVal.setText(scanSettings.getWorkDirectory());
            lbFileExtVal.setText(scanSettings.getFileExtension());
            lbScanTimeVal.setText(String.valueOf(scanSettings.getScanTimeOut()));
            lbTimeUnit.setText(scanSettings.getTimeUnit().toString());
        }
    }

    /**
     * Set the saved locale
     */
    private void setUserLocale() {
        Locale locale = scanSettings.getLocale();
        LOGGER.debug("User saved locale is ---> " + locale);
        //english locale is default
        if (new Locale("ru", "RU").equals(locale)) {
            setRussian();
        } else {
            setEnglish();
        }
    }

    /**
     * Change the window language
     */
    private void changeWindowLanguage() {
        Locale currentLocale = getLocale();
        //Menu
        //*File
        menuFile.setText(ms.getMessage("text.menuFile", null, currentLocale));
        savedFiles.setText(ms.getMessage("text.menuFileSavedFiles", null, currentLocale));
        exit.setText(ms.getMessage("text.menuFileExit", null, currentLocale));

        //*Settings
        menuSettings.setText(ms.getMessage("text.menuSettings", null, currentLocale));
        settings.setText(ms.getMessage("text.menuSettingsSettings", null, currentLocale));

        //*Language
        menuLanguage.setText(ms.getMessage("text.menuLanguage", null, currentLocale));
        language.setText(ms.getMessage("text.menuLanguageLanguage", null, currentLocale));
        chEN.setText(ms.getMessage("text.menuLanguageLanguageEN", null, currentLocale));
        chRU.setText(ms.getMessage("text.menuLanguageLanguageRU", null, currentLocale));

        labelLanguage.setText(ms.getMessage("text.labelLanguage", null, currentLocale));

        //Table
        savedFileTable.setPlaceholder(new Label(ms.getMessage("text.table", null, currentLocale)));
        tableColumn.setText(ms.getMessage("text.tableFileRow", null, currentLocale));

        //Scan buttons
        scanStart.setText(ms.getMessage("text.buttonStartScan", null, currentLocale));
        scanStop.setText(ms.getMessage("text.buttonStopScan", null, currentLocale));

        //User settings labels
        settingsTitle.setText(ms.getMessage("text.labelSettingsTitle", null, currentLocale));
        lbScanDir.setText(ms.getMessage("text.labelScanDirectory", null, currentLocale));
        lbFileExt.setText(ms.getMessage("text.labelFileExtension", null, currentLocale));
        lbScanTime.setText(ms.getMessage("text.labelScanTimeOut", null, currentLocale));
    }

    /**
     * Retrieve a locale from the context
     */
    private Locale getLocale() {
        LOGGER.debug("Current locale is ---> " + LocaleContextHolder.getLocale());
        return LocaleContextHolder.getLocale();
    }

    /**
     * Prepare a modality window
     */
    private void prepareModalityWindow(String fxmlDestination, String windowTitle) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlDestination));
        Stage stage = new Stage();
        stage.setTitle(windowTitle);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(scanStart.getScene().getWindow());

        stage.showAndWait();
    }

    /**
     * Exit
     */
    public void exit() {
        Platform.exit();
    }
}
