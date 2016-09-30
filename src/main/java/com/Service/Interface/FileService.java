package com.Service.Interface;

import com.Bean.FileDTO;
import com.Exception.TooLargeSizeFileException;
import javafx.scene.control.TableView;

public interface FileService {
    void startScan() throws TooLargeSizeFileException;

    void stopScan();

    void addAllSavedFiles(TableView<FileDTO> tableView);

    void deleteFile(Long file_id);

    FileScanner getScanner();
}
