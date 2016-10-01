package com.ExceptionHandler;

import com.Controller.MainController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Locale;

@Component
public class ExceptionHandlerForScanner {

    private static final Logger LOGGER = LogManager.getLogger(ExceptionHandlerForScanner.class);

    @Autowired
    private MainController mainController;

    @Autowired
    private MessageSource ms;

    @Value("${maxFileSize}")
    private int maxFileSize;

    /**
     * Need to stop scan if the server doesn't work
     */
    public void serverUnavailableScan() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainController.stopScanning();
                serverCrack();
            }
        });
    }

    /**
     * Just inform user that server doesn't work
     */
    public void serverUnavailable() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                serverCrack();
            }
        });
    }

    /**
     * Inform a user that a file or files have a size which isn't allowed
     * Allowed size is defined in the scan.property
     */
    public void fileTooBig(File file) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LOGGER.info(ms.getMessage("logger.fileSizeLogger", new Object[]{file.getName()}, getLocale()));
                Alert alert = new Alert(Alert.AlertType.ERROR);
                setAlertTitles(alert,
                        ms.getMessage(
                                "error.fileSizeTitle", null, getLocale()),
                        ms.getMessage(
                                "error.fileSizeHeader", null, getLocale()),
                        ms.getMessage(
                                "error.fileSizeContent", new Object[]{file.getName(), file.length(), maxFileSize / 1000},
                                getLocale()));
                alert.showAndWait();
            }
        });
    }

    /**
     * User try to register the email which has already existed
     */
    public void userNotValid(String responseBody) {
        LOGGER.info("Bad email " + responseBody);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        setAlertTitles(alert,
                "Error", "Email Error", responseBody);
        alert.showAndWait();
    }

    private void serverCrack() {
        LOGGER.info(ms.getMessage("logger.serverUnavailableLogger", null, getLocale()));

        Alert alert = new Alert(Alert.AlertType.ERROR);
        setAlertTitles(alert,
                ms.getMessage("error.serverUnavailableTitle", null, getLocale()),
                ms.getMessage("error.serverUnavailableHeader", null, getLocale()),
                ms.getMessage("error.serverUnavailableContent", null, getLocale()));
        alert.showAndWait();
    }

    private void setAlertTitles(Alert alert, String title, String header, String content) {
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
    }

    public Locale getLocale() {
        LOGGER.debug("Current locale is ---> " + LocaleContextHolder.getLocale());
        return LocaleContextHolder.getLocale();
    }
}
