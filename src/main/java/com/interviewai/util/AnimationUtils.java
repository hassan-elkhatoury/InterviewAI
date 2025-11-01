package com.interviewai.util;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Common UI animations.
 * TODO: Use for subtle transitions between views.
 */
public class AnimationUtils {
    public static void fadeIn(Node node, double seconds) {
        FadeTransition ft = new FadeTransition(Duration.seconds(seconds), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}
