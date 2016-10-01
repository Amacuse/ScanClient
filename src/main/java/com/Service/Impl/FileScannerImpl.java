package com.Service.Impl;

import com.Bean.ScanSettings;
import com.ExceptionHandler.ExceptionHandlerForScanner;
import com.Service.Interface.BackUpService;
import com.Service.Interface.FileScanner;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class FileScannerImpl implements FileScanner {

    private static final Logger LOGGER = LogManager.getLogger(FileScannerImpl.class);

    @Value("${maxFileSize}")
    private int maxFileSize;

    @Autowired
    private volatile ScanSettings settings;
    @Autowired
    private BackUpService backUpService;
    @Autowired
    private ExceptionHandlerForScanner exceptionHandlerForScanner;
    @Autowired
    private MessageSource ms;

    private Map<String, Long> fileMap = new HashMap<>();
    private volatile boolean scan;

    /**
     * The primary method of the application
     */
    @Override
    public Void call() throws InterruptedException {
        while (scan) {
            //If the connection wasn't established try to send the previously changed files
            if (!backUpService.getTmpMap().isEmpty()) {
                backUpService.sendDept();
            }

            Collection<File> listFiles = FileUtils.listFiles(new File(settings.getWorkDirectory()),
                    new String[]{settings.getFileExtension()}, true);

            if (fileMap.isEmpty()) {
                for (File file : listFiles) {
                    //stop scanning if the file is too big
                    if (isBig(file)) {
                        return null;
                    }

                    String fileName = file.getPath();
                    long lastModified = file.lastModified();

                    LOGGER.info(ms.getMessage("fileScanner.logger.newFile", new Object[]{fileName}, getLocale()));
                    //send to the server
                    backUpService.backUpFile(file);
                    //save in temporary map
                    fileMap.put(fileName, lastModified);
                }
            } else {
                //create the temporary file list in order to take into consideration deleted files
                Collection<String> listFileName = new HashSet<>(listFiles.size());
                for (File file : listFiles) {
                    //stop scanning if the file is too big
                    if (isBig(file)) {
                        return null;
                    }
                    //get the file name and modified date
                    String fileName = file.getPath();
                    long lastModified = file.lastModified();

                    listFileName.add(fileName);

                    //get the file from the map and if it exists verify its modified date
                    Long storedModifiedDate = fileMap.get(fileName);
                    if (storedModifiedDate == null) {

                        LOGGER.info(ms.getMessage("fileScanner.logger.newFile", new Object[]{fileName}, getLocale()));
                        backUpService.backUpFile(file);
                        fileMap.put(fileName, lastModified);
                    } else {
                        //if the date was modified...
                        if (storedModifiedDate != lastModified) {

                            LOGGER.info(ms.getMessage("fileScanner.logger.modifiedDate", new Object[]{fileName}, getLocale()));
                            //... send the file to the server
                            backUpService.backUpFile(file);
                            fileMap.put(fileName, lastModified);
                        }
                    }
                }
                //if the file was deleted from the root directory then it must be deleted from the 'fileMap'
                fileMap.keySet().retainAll(listFileName);
            }
            Thread.sleep(settings.getScanTimeOutInMills());
        }
        return null;
    }

    /**
     * Verify the file size
     */
    private boolean isBig(File file) {
        if (file.length() > maxFileSize) {
            exceptionHandlerForScanner.fileTooBig(file);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Long> getFileMap() {
        return fileMap;
    }

    @Override
    public void setFileMap(Map<String, Long> fileMap) {
        this.fileMap = fileMap;
    }

    @Override
    public boolean isScan() {
        return scan;
    }

    @Override
    public void setScan(boolean scan) {
        this.scan = scan;
    }

    public Locale getLocale() {
        return settings.getLocale();
    }
}
