package com.Service.Interface;

import org.springframework.http.HttpEntity;

public interface HeaderService {

    <T> HttpEntity<T> buildAuthorizationHeader(T entityBody);
}
