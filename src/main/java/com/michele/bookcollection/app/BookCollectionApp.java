package com.michele.bookcollection.app;

import com.michele.bookcollection.gui.SceneFactory;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class BookCollectionApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/gui/MainView.fxml"));
        Scene scene = SceneFactory.createScene(root, 1200, 800);
        primaryStage.setTitle("Libreria Personale");
        primaryStage.setScene(scene);
        Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
