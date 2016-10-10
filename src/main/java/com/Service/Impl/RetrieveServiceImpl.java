package com.Service.Impl;

import com.Bean.ContentIdAndDate;
import com.Bean.FileDTO;
import com.ExceptionHandler.ExceptionHandlerForScanner;
import com.Service.Interface.RetrieveService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
public class RetrieveServiceImpl extends HeaderServiceImpl implements RetrieveService {

    private static final Logger LOGGER = LogManager.getLogger(RetrieveServiceImpl.class);

    @Autowired
    private ExceptionHandlerForScanner exceptionHandlerForScanner;

    @Value("${urlForFileCopies}")
    private String urlForCopies;
    @Value("${urlForFileContent}")
    private String urlForContent;

    /**
     * Retrieve all saved copies of the file
     */
    @Override
    public List<ContentIdAndDate> getFileCopies(Long file_id) {
        HttpEntity<Object> entity = buildAuthorizationHeader(null);
        ResponseEntity<FileDTO[]> response = null;
        try {
            response = new RestTemplate()
                    .exchange(urlForCopies, HttpMethod.GET, entity, FileDTO[].class, getUser().getId(), file_id);
        } catch (RestClientException e) {
                    /*NOP*/
        }

        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            //construct the dto class for convenience
            List<ContentIdAndDate> result = new ArrayList<>(response.getBody().length);
            for (FileDTO fileDTO : response.getBody()) {
                DateTime backUpDate = fileDTO.getBackUpDate();
                result.add(new ContentIdAndDate(fileDTO.getId(),
                        backUpDate.toString("yyyy-MM-dd"),
                        backUpDate.toString("HH:mm:ss")));
            }
            return result;
        } else {
            exceptionHandlerForScanner.serverUnavailable();
            return Collections.<ContentIdAndDate>emptyList();
        }
    }

    /**
     * Retrieve the file from the server
     */
    @Override
    public FileDTO getSelectedFile(Long content_id) {
        //just need for url
        Long stub = 17L;
        HttpEntity<Object> entity = buildAuthorizationHeader(null);
        ResponseEntity<FileDTO> response = null;
        try {
            response = new RestTemplate()
                    .exchange(urlForContent, HttpMethod.GET, entity, FileDTO.class, getUser().getId(), stub, content_id);
        } catch (RestClientException e) {
                    /*NOP*/
        }

        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            exceptionHandlerForScanner.serverUnavailable();
            return new FileDTO();
        }
    }

    /**
     * Delete the file from the server
     */
    @Override
    public void deleteSelectedFile(Long content_id) {
        //just need for url
        Long stub = 17L;
        HttpEntity<Object> entity = buildAuthorizationHeader(null);
        try {
            new RestTemplate().exchange(urlForContent, HttpMethod.DELETE, entity, String.class, getUser().getId(), stub, content_id);
        } catch (RestClientException e) {
            exceptionHandlerForScanner.serverUnavailable();
        }
    }

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

}
