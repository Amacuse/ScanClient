package com.Service.Impl;

import com.Bean.User;
import com.ExceptionHandler.ExceptionHandlerForScanner;
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

    @Autowired
    private ExceptionHandlerForScanner exceptionHandlerForScanner;
    @Autowired
    private User user;
    @Value("${urlForUser}")
    private String url;


    @Override
    public boolean saveUser(User user) {
        //build header for the user locale setting
        HttpHeaders headers = new HttpHeaders();
        //setup the locale which the user has chosen
        headers.add("accept-language", getLocale().toLanguageTag());
        HttpEntity<User> entity = new HttpEntity<>(user, headers);
        ResponseEntity<String> response;
        try {
            response = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            //get error message and show it to the user
            String responseMessage = e.getResponseBodyAsString();
            LOGGER.debug("Response error: " + responseMessage);
            exceptionHandlerForScanner.userNotValid(responseMessage);
            return false;
        } catch (RestClientException e) {
            exceptionHandlerForScanner.serverUnavailable();
            return false;
        }

        if (response != null && response.getStatusCode() == HttpStatus.CREATED) {
            //get the user id and save it to the user bean
            URI location = response.getHeaders().getLocation();
            String userURL = location.toString();
            int index = userURL.lastIndexOf("/");
            String id = userURL.substring(index + 1);
            this.user.setId(Long.valueOf(id));
            return true;
        }
        return false;
    }

    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }
}
