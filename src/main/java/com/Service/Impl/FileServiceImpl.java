package com.Service.Impl;

import com.Bean.FileDTO;
import com.Service.Interface.BackUpService;
import com.Service.Interface.FileScanner;
import com.Service.Interface.FileService;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LogManager.getLogger(FileServiceImpl.class);

    @Autowired
    private ThreadPoolExecutor service;
    @Autowired
    private FileScanner scanner;
    @Autowired
    private BackUpService backUpService;
    @Autowired
    private MessageSource ms;

    @Override
    public void startScan() {
        //check if the scanner already working
        if (!scanner.isScan()) {
            //if server was unavailable chang the availability flag
            if (!backUpService.getAvailable().get()) {
                backUpService.getAvailable().set(true);
            }
            scanner.setScan(true);
            service.submit(scanner);
        }
    }

    @Override
    public void stopScan() {
        scanner.setScan(false);
    }

    @Override
    public void getFiles(TableView<FileDTO> tableView) {
        //create an independent thread for retrieving all the previously saved files
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //clear all previously files to avoid duplication
                    ObservableList<FileDTO> items = tableView.getItems();
                    items.clear();
                    //get the files from the server
                    List<FileDTO> backUpFiles = backUpService.getAllBackUpFiles();
                    //add to the table view
                    items.addAll(backUpFiles);
                } catch (ExecutionException | InterruptedException e) {
                    LOGGER.info(ms.getMessage("fileService.logger.getAllFiles", new Object[]{e}, getLocale()));
                }
            }
        });
    }

    @Override
    public void deleteFile(Long file_id) {
        backUpService.deleteBackUpFile(file_id);
    }

    private Locale getLocale() {
        LOGGER.debug("Current locale is ---> " + LocaleContextHolder.getLocale());
        return LocaleContextHolder.getLocale();
    }
}
