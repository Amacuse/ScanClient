package com.Service.Interface;

import com.Bean.FileDTO;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public interface BackUpService {
    void backUpFile(File file);

    List<FileDTO> getAllBackUpFiles() throws ExecutionException, InterruptedException;

    void deleteBackUpFile(Long file_id);

    AtomicBoolean getAvailable();

    Map<String, FileDTO> getTmpMap();

    void sendDept();
}
