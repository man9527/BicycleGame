package com.jawi.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.List;

/**
 * Created by man9527 on 2015/3/12.
 */
public class RotateImageAnimation extends Transition {
    private final ImageView imageView;
    private int count = 0;
    private final List<Image> images;

    public RotateImageAnimation(ImageView imageView, List<Image> images) {
        this.imageView = imageView;
        this.images = images;
        setCycleDuration(Duration.millis(300));
        setInterpolator(Interpolator.LINEAR);
    }


    @Override
    protected void interpolate(double frac) {
        if (frac==1.0) {
            imageView.setImage(images.get(count % images.size()));
            count++;
        }
    }
}
