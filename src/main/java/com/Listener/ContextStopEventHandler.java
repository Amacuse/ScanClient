package com.Listener;

import com.Bean.User;
import com.Bean.ScanSettings;
import com.Service.Interface.FileScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

@Component
public class ContextStopEventHandler implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LogManager.getLogger(ContextStopEventHandler.class);

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
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        //try to read the directory where will be saved settings
        File recDir = new File(recoveryDir);
        //if the application is started for the first time, create the directory using system property user.dir
        if (!recDir.exists()) {
            boolean created = recDir.mkdir();
            LOGGER.debug("Was a recovery directory created ---> " + created);
            LOGGER.debug("Absolute path of the created directory is ---> " + recDir);
        }

        //get the current scanned files
        Map<String, Long> fileMap = scanner.getFileMap();

        //if it's not empty, serialize it
        if (!fileMap.isEmpty()) {
            try (ObjectOutputStream objectInputStream = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(
                                    new File(recDir, scannedFilesFile))))) {
                objectInputStream.writeObject(fileMap);
                objectInputStream.flush();
            } catch (IOException e) {
                LOGGER.info("Something has gone wrong: " + e);
            }
        }

        //serialize scan settings
        if (scanSettings.getWorkDirectory() != null) {
            try (ObjectOutputStream objectInputStream = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(
                                    new File(recDir, userSettingsFile))))) {
                if (scanSettings.getLocale() == null) {
                    scanSettings.setLocale(LocaleContextHolder.getLocale());
                }
                objectInputStream.writeObject(scanSettings);
                objectInputStream.flush();
            } catch (IOException e) {
                LOGGER.info("Something has gone wrong: " + e);
            }
        }

        //serialize user
        if (user.getId() != null) {
            try (ObjectOutputStream objectInputStream = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(
                                    new File(recDir, userFile))))) {
                objectInputStream.writeObject(user);
                objectInputStream.flush();
            } catch (IOException e) {
                LOGGER.info("Something has gone wrong: " + e);
            }
        }
    }
}
