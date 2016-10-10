package com.Service.Impl;

import com.Bean.User;
import com.Service.Interface.HeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class HeaderServiceImpl implements HeaderService {

    @Autowired
    private User user;

    public <T> HttpEntity<T> buildAuthorizationHeader(T entityBody) {
        HttpHeaders headers = new HttpHeaders();
        //setup user email and password for authorization
        String password = Base64.getEncoder().encodeToString((
                user.getEmail() + ":" + String.valueOf(user.getPassword())).getBytes());
        headers.add("Authorization", "Basic " + password);
        return new HttpEntity<>(entityBody, headers);
    }

    public User getUser() {
        return user;
    }
}
