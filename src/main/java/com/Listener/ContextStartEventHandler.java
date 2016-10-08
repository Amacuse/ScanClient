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
                Map<String, Long> deserializeMap = SerializationUtils.<Map<String, Long>>deserialize(
                        new BufferedInputStream(
                                new FileInputStream(mapFile)));
                scanner.setFileMap(deserializeMap);

                LOGGER.debug("File map has a size: " + deserializeMap.size());
            } catch (FileNotFoundException e) {
                /*existence has been verified*/
                LOGGER.info("Something has gone wrong when the previous scanned files map has been read: " + e);
            }
        }

        //get the scanning settings
        File scanSettingsFile = new File(recoveryDir, userSettingsFile);
        if (scanSettingsFile.exists() && scanSettingsFile.length() > 0) {
            try {
                ScanSettings deserializeScanSettings = SerializationUtils.<ScanSettings>deserialize(
                        new BufferedInputStream(
                                new FileInputStream(scanSettingsFile)));
                LOGGER.debug(deserializeScanSettings);
                scanSettings.setWorkDirectory(deserializeScanSettings.getWorkDirectory());
                scanSettings.setFileExtension(deserializeScanSettings.getFileExtension());
                scanSettings.setScanTimeOut(deserializeScanSettings.getScanTimeOut());
                scanSettings.setTimeUnit(deserializeScanSettings.getTimeUnit());
                scanSettings.setLocale(deserializeScanSettings.getLocale());
            } catch (FileNotFoundException e) {
                /*existence has been verified*/
                LOGGER.info("Something has gone wrong when the user settings scanSettingsFile has been read: " + e);
            }
        }

        //get the user settings
        File userFile = new File(recoveryDir, this.userFile);
        if (userFile.exists() && userFile.length() > 0) {
            try {
                User deserializeUser = SerializationUtils.<User>deserialize(
                        new BufferedInputStream(
                                new FileInputStream(userFile)));
                LOGGER.debug(deserializeUser);
                user.setId(deserializeUser.getId());
                user.setName(deserializeUser.getName());
                user.setEmail(deserializeUser.getEmail());
                user.setBirthday(deserializeUser.getBirthday());
                user.setPassword(deserializeUser.getPassword());
            } catch (FileNotFoundException e) {
                /*existence has been verified*/
                LOGGER.info("Something has gone wrong when the user scanSettingsFile has been read: " + e);
            }
        }
    }
}