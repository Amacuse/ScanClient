package com.Service.Impl;

import com.Bean.User;
import com.ExceptionHandler.ExceptionHandler;
import com.Service.Interface.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class);

    @Value("${urlForUser}")
    private String url;
    @Autowired
    private ExceptionHandler exceptionHandler;
    @Autowired
    private User user;


    @Override
    public boolean saveUser(User userToVerify) {
        //build header for the user locale setting
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", getLocale().toLanguageTag());
        HttpEntity<User> entity = new HttpEntity<>(userToVerify, headers);
        ResponseEntity<String> response;
        try {
            response = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            String responseMessage = e.getResponseBodyAsString();
            LOGGER.debug("Response error: " + responseMessage);
            exceptionHandler.userNotValid(responseMessage);
            return false;
        } catch (RestClientException e) {
            exceptionHandler.serverUnavailable();
            return false;
        }

        if (response != null && response.getStatusCode() == HttpStatus.CREATED) {
            URI location = response.getHeaders().getLocation();
            String userURL = location.toString();
            int index = userURL.lastIndexOf("/");
            String id = userURL.substring(index + 1);
            user.setId(Long.valueOf(id));
            return true;
        }
        return false;
    }

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
