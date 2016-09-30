package com.Listener;

import com.Bean.User;
import com.Bean.ScanSettings;
import com.Service.Interface.FileScanner;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

@Component
public class ContextStartEventHandler implements ApplicationListener<ContextStartedEvent> {

    private static final Logger LOGGER = LogManager.getLogger(ContextStartedEvent.class);

    @Autowired
    private FileScanner scanner;
    @Autowired
    private ScanSettings scanSettings;
    @Autowired
    private User user;

    @Value("${recoveryDir}")
    private String recoveryDir;
    @Value("${scannedFilesFile}")
    private String scannedFilesFile;
    @Value("${userSettingsFile}")
    private String userSettingsFile;
    @Value("${userFile}")
    private String userFile;

    @Override
    public void onApplicationEvent(ContextStartedEvent contextStartedEvent) {
        //get the last scanning files
        File mapFile = new File(recoveryDir, scannedFilesFile);
        if (mapFile.exists() && mapFile.length() > 0) {
            try {
                Map<String, Long> deserialize = SerializationUtils.<Map<String, Long>>deserialize(
                        new BufferedInputStream(
                                new FileInputStream(mapFile)));
                scanner.setFileMap(deserialize);

                LOGGER.debug("File map has a size: " + deserialize.size());
            } catch (FileNotFoundException e) {
                /*existence has been verified*/
                LOGGER.info("Something has gone wrong when the previous scanned files map has been read: " + e);
            }
        }

        //get the scanning settings
        File file = new File(recoveryDir, userSettingsFile);
        if (file.exists() && file.length() > 0) {
            try {
                ScanSettings deserialize = SerializationUtils.<ScanSettings>deserialize(
                        new BufferedInputStream(
                                new FileInputStream(file)));
                LOGGER.info(deserialize);
                scanSettings.setWorkDirectory(deserialize.getWorkDirectory());
                scanSettings.setFileExtension(deserialize.getFileExtension());
                scanSettings.setScanTimeOut(deserialize.getScanTimeOut());
                scanSettings.setTimeUnit(deserialize.getTimeUnit());
                scanSettings.setLocale(deserialize.getLocale());
            } catch (FileNotFoundException e) {
                /*existence has been verified*/
                LOGGER.info("Something has gone wrong when the user settings file has been read: " + e);
            }
        }

        //Get the user settings
        File userFile = new File(recoveryDir, this.userFile);
        if (userFile.exists() && userFile.length() > 0) {
            try {
                User deserialize = SerializationUtils.<User>deserialize(
                        new BufferedInputStream(
                                new FileInputStream(userFile)));
                LOGGER.info("User name: " + deserialize.getName());
                LOGGER.info("User email: " + deserialize.getEmail());
                LOGGER.info("User birthday: " + deserialize.getBirthday());
                user.setId(deserialize.getId());
                user.setName(deserialize.getName());
                user.setEmail(deserialize.getEmail());
                user.setBirthday(deserialize.getBirthday());
            } catch (FileNotFoundException e) {
                /*existence has been verified*/
                LOGGER.info("Something has gone wrong when the user file has been read: " + e);
            }
        }
    }
}