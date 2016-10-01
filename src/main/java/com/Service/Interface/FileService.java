package com.Service.Interface;

import com.Bean.FileDTO;
import javafx.scene.control.TableView;

public interface FileService {
    void startScan();

    void stopScan();

    void getFiles(TableView<FileDTO> tableView);

    void deleteFile(Long file_id);
}
