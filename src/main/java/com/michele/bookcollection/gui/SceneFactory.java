package com.michele.bookcollection.gui;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneFactory {
    private static final String CSS_PATH = "/css/styles.css";

    public static Scene createScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(SceneFactory.class.getResource(CSS_PATH).toExternalForm());
        return scene;
    }
}
