package com;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;

public class SpringFxmlLoader {

    private static final ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

    public Object load(String url) {
        //to enable StartEventListener
        ((ConfigurableApplicationContext) context).start();

        try (InputStream fxmlInput = getClass().getResourceAsStream(url)) {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> clazz) {
                    return context.getBean(clazz);
                }
            });
            return loader.load(fxmlInput);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
