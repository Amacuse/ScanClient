package com.Service.Impl;

import com.Bean.FileDTO;
import com.Bean.User;
import com.ExceptionHandler.ExceptionHandlerForScanner;
import com.Service.Interface.BackUpService;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BackUpServiceImpl implements BackUpService {

    private static final Logger LOGGER = LogManager.getLogger(BackUpServiceImpl.class);

    @Value("${urlForSaveFile}")
    private String url;
    @Value("${urlForFileDelete}")
    private String urlForDelete;

    @Autowired
    private User user;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private ExceptionHandlerForScanner exceptionHandlerForScanner;
    @Autowired
    private MessageSource ms;

    private Map<String, FileDTO> tmpMap = new ConcurrentHashMap<>();
    private AtomicBoolean available = new AtomicBoolean(true);

    /**
     * If for now the server is unavailable save the files in temporary map and try to send them later
     */
    @Override
    public void sendDept() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, FileDTO> entry : tmpMap.entrySet()) {
                    ResponseEntity<String> response = null;
                    try {
                        response = new RestTemplate()
                                .postForEntity(url, entry.getValue(), String.class, user.getId());
                    } catch (RestClientException e) {
                    /*NOP*/
                    }
                    //if everything is ok remove the files from the map
                    if (response != null && response.getStatusCode() == HttpStatus.OK) {
                        tmpMap.remove(entry.getKey());
                    }
                }
            }
        });
    }

    /**
     * Send file to the server
     */
    @Override
    public void backUpFile(File file) {
        //create independent thread for sending modified file to the server
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String fileName = file.getPath();
                byte[] bytes = null;
                try {
                    //retrieve file content
                    bytes = FileUtils.readFileToByteArray(file);
                } catch (IOException e) {
                    LOGGER.error(ms.getMessage(
                            "backUpService.logger.deletedFile", new Object[]{fileName, e}, getLocale()));
                }
                FileDTO fileDTO = new FileDTO(fileName, bytes);
                ResponseEntity<String> response = null;
                try {
                    response = new RestTemplate()
                            .postForEntity(url, fileDTO, String.class, user.getId());
                } catch (RestClientException e) {
                    /*NOP*/
                }
                if (response == null || response.getStatusCode() != HttpStatus.CREATED) {
                    //server is unavailable.Put modified files in the temporary map
                    //for further sending to the server
                    tmpMap.put(fileName + DateTime.now().getMillis(), fileDTO);
                    //avoid multipopup windows
                    if (available.getAndSet(false)) {
                        exceptionHandlerForScanner.serverUnavailableScan();
                    }
                }
            }
        });
    }

    /**
     * Retrieve the files from the server
     */
    @Override
    public List<FileDTO> getAllBackUpFiles() throws ExecutionException, InterruptedException {
        ResponseEntity<FileDTO[]> response = null;
        try {
            response = new RestTemplate().getForEntity(
                    url, FileDTO[].class, user.getId());
        } catch (RestClientException e) {
            /*NOP*/
        }
        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            return new ArrayList<>(Arrays.asList(response.getBody()));
        } else {
            exceptionHandlerForScanner.serverUnavailable();
            return Collections.<FileDTO>emptyList();
        }
    }

    /**
     * Remove the files from the server
     */
    @Override
    public void deleteBackUpFile(Long file_id) {
        try {
            new RestTemplate().delete(urlForDelete, user.getId(), file_id);
        } catch (RestClientException e) {
            exceptionHandlerForScanner.serverUnavailable();
        }

    }

    public AtomicBoolean getAvailable() {
        return available;
    }

    public Map<String, FileDTO> getTmpMap() {
        return tmpMap;
    }

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
