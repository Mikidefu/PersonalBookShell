package com.michele.bookcollection.gui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

    public static void fadeIn(Node node, int durationMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    public static void fadeOut(Node node, int durationMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    public static void moveTo(Node node, double toX, double toY, int durationMillis) {
        TranslateTransition move = new TranslateTransition(Duration.millis(durationMillis), node);
        move.setToX(toX);
        move.setToY(toY);
        move.play();
    }

    public static void scale(Node node, double scaleX, double scaleY, int durationMillis) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMillis), node);
        scale.setToX(scaleX);
        scale.setToY(scaleY);
        scale.play();
    }

    public static void rotate(Node node, double angle, int durationMillis) {
        RotateTransition rotate = new RotateTransition(Duration.millis(durationMillis), node);
        rotate.setByAngle(angle);
        rotate.play();
    }
}
