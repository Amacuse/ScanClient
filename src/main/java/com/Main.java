package com;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ThreadPoolExecutor;

public class Main extends Application {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final SpringFxmlLoader LOADER = new SpringFxmlLoader();
    private static final int MIN_HEIGHT = 470;
    private static final int MIN_WIDTH = 450;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = (Parent) LOADER.load("/fx/main.fxml");
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setTitle("Scanning application");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, MIN_WIDTH, MIN_HEIGHT));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        ThreadPoolExecutor service = SpringFxmlLoader.getContext().getBean("service", ThreadPoolExecutor.class);
        service.shutdownNow();

        LOGGER.info("Program is stopped");

        //to enable StopEventListener
        ((ConfigurableApplicationContext) SpringFxmlLoader.getContext()).close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
