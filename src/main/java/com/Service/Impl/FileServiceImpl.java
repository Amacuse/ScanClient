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
    public void addAllSavedFiles(TableView<FileDTO> tableView) {
        //create independent thread for retrieving all the previously saved files
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ObservableList<FileDTO> items = tableView.getItems();
                    items.clear();
                    List<FileDTO> backUpFiles = backUpService.getAllBackUpFiles();
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

    public Locale getLocale() {
        LOGGER.debug("Current locale is ---> " + LocaleContextHolder.getLocale());
        return LocaleContextHolder.getLocale();
    }
}
