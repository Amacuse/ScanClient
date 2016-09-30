package com.Service.Interface;

import java.util.Map;
import java.util.concurrent.Callable;

public interface FileScanner extends Callable<Void> {

    Map<String, Long> getFileMap();

    void setFileMap(Map<String, Long> fileMap);

    boolean isScan();

    void setScan(boolean scan);
}
