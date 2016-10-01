package com.Controller;

import com.Bean.ScanSettings;
import com.SpringFxmlLoader;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class ScanSettingsController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger(ScanSettingsController.class);

    private ScanSettings settings = SpringFxmlLoader.getContext().getBean("scanSettings", ScanSettings.class);
    private MessageSource ms = SpringFxmlLoader.getContext().getBean("messageSource", MessageSource.class);

    private String directoryPath;
    private TimeUnit previousTimeUnit = settings.getTimeUnit();

    @FXML
    private Label lbWorkDir;
    @FXML
    private Label lbFileExt;
    @FXML
    private Label lbScanTime;
    @FXML
    private Button btnChooseDirectory;
    @FXML
    private Button btnOk;
    @FXML
    private Button btnCancel;
    @FXML
    private TextField txtDirectory;
    @FXML
    private TextField txtExtension;
    @FXML
    private TextField txtScanTime;
    @FXML
    private Label directoryValidate;
    @FXML
    private Label extensionValidate;
    @FXML
    private Label timeOutValidate;
    @FXML
    private RadioButton chSec;
    @FXML
    private RadioButton chMin;
    @FXML
    private RadioButton chHr;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //for i18n
        changeWindowLanguage();
        //populate scan settings if they are
        if (settings.getWorkDirectory() != null) {
            txtDirectory.setText(settings.getWorkDirectory());
            directoryPath = settings.getWorkDirectory();
            txtExtension.setText(settings.getFileExtension());
            txtScanTime.setText(String.valueOf(settings.getScanTimeOut()));
            //setup radio button flag
            switch (settings.getTimeUnit()) {
                case SECONDS:
                    chSec.fire();
                    break;
                case MINUTES:
                    chMin.fire();
                    break;
                case HOURS:
                    chHr.fire();
                    break;
            }
        }
    }

    /**
     * Verify and populate scan settings
     */
    public void setSettings(ActionEvent actionEvent) {
        if (actionEvent.getSource() == btnOk) {

            boolean validate = true;

            //Validate directory
            if (directoryPath != null) {
                directoryValidate.setText("");
            } else {
                validate = false;
                directoryValidate.setText(ms.getMessage("error.directory", null, getLocale()));
            }

            //Validate extension
            String extension = txtExtension.getText().toLowerCase();
            //only letters are available
            if (extension.matches("[a-z]+")) {
                extensionValidate.setText("");
            } else {
                LOGGER.debug(ms.getMessage("error.settings.extension", new Object[]{extension}, getLocale()));
                validate = false;
                extensionValidate.setText(ms.getMessage("error.extension", null, getLocale()));
            }

            //Validate time-out
            int scanTime = 0;
            try {
                scanTime = Integer.parseInt(txtScanTime.getText());
                //must be positive
                if (scanTime > 0) {
                    timeOutValidate.setText("");
                } else {
                    LOGGER.debug(ms.getMessage("error.settings.scanTimeOut", new Object[]{scanTime}, getLocale()));
                    validate = false;
                    timeOutValidate.setText(ms.getMessage("error.timeOutNegative", null, getLocale()));
                }
            } catch (NumberFormatException e) {
                LOGGER.debug(ms.getMessage("error.settings.scanTimeOut", new Object[]{scanTime}, getLocale()));
                validate = false;
                timeOutValidate.setText(ms.getMessage("error.timeOutNotNumber", null, getLocale()));
            }

            //Check TimeUnit flag
            if (settings.getTimeUnit() == null) {
                validate = false;
                //inform user that time unit flag must be setup
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText(ms.getMessage("error.timeUnit", null, getLocale()));
                alert.showAndWait();
            }

            //if everything is ok populate the settings
            if (validate) {
                settings.setWorkDirectory(directoryPath);
                settings.setFileExtension(extension);
                settings.setScanTimeOut(scanTime);

                //close the window
                ((Stage) btnCancel.getScene().getWindow()).close();
            }

        } else {
            //if user press 'Cancel' and before that has changed the time unit flag
            //return the saved one
            settings.setTimeUnit(previousTimeUnit);
            //close the window
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }

    /**
     * Choose a directory for scan
     */
    public void chooseDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(ms.getMessage("text.settings.directoryChooserTitle", null, getLocale()));
        File directory = chooser.showDialog(txtDirectory.getScene().getWindow());

        if (directory != null) {
            directoryPath = directory.getAbsolutePath();
            txtDirectory.setText(directoryPath);
        }
    }

    /**
     * Choose a time unit for the scan timeout
     */
    public void setTimeUnitForScanTime(ActionEvent actionEvent) {
        EventTarget target = actionEvent.getTarget();
        if (target == chSec) {
            settings.setTimeUnit(TimeUnit.SECONDS);
        } else if (target == chMin) {
            settings.setTimeUnit(TimeUnit.MINUTES);
        } else {
            settings.setTimeUnit(TimeUnit.HOURS);
        }
    }

    /**
     * Change the window language
     */
    private void changeWindowLanguage() {
        Locale locale = getLocale();

        //Label
        lbWorkDir.setText(ms.getMessage("text.settings.labelWorkDir", null, locale));
        lbFileExt.setText(ms.getMessage("text.settings.labelFileExt", null, locale));
        lbScanTime.setText(ms.getMessage("text.settings.labelScanTimeOut", null, locale));

        //Text for text placeholder
        txtExtension.setPromptText(ms.getMessage("text.settings.txtFieldExtPrompt", null, locale));
        txtScanTime.setPromptText(ms.getMessage("text.settings.txtFieldScanTime", null, locale));

        //Radio button
        chSec.setText(ms.getMessage("text.settings.chSec", null, locale));
        chMin.setText(ms.getMessage("text.settings.chMin", null, locale));
        chHr.setText(ms.getMessage("text.settings.chHr", null, locale));

        //Choose directory button
        btnChooseDirectory.setText(ms.getMessage("text.settings.directoryChooserTitle", null, locale));
    }

    /**
     * Retrieve a locale from the context
     */
    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
