package com.jawi;

import com.jawi.game.GameController;
import com.jawi.usb.UsbProxy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

/**
 * Created by man9527 on 2015/2/20.
 */
public class Controller {
    @FXML
    private Label clock;

    @FXML
    private Slider calBurn;

    @FXML
    private Label testLabel;

    @FXML
    private Label highTemperatureLabel;

    @FXML
    private Label lowTemperatureLabel;

    @FXML
    private Label rpmLabel;

    @FXML
    private Label envTemperatureLabel;

    @FXML
    private Label maxBurnCalLabel;

    @FXML
    private ImageView leftImageView;

    @FXML
    private ImageView rightImageView;

    public Label getClock() {
        return clock;
    }

    public Slider getCalBurn() {
        return calBurn;
    }

    @FXML
    private void beginUsbValue() {
        UsbProxy.get().startGetValue();
    }

    @FXML
    private void stopUsbValue() {
        UsbProxy.get().stopGetValue();
    }

    public Label getTestLabel() {
        return testLabel;
    }

    public Label getHighTemperatureLabel() {
        return highTemperatureLabel;
    }

    public Label getLowTemperatureLabel() {
        return lowTemperatureLabel;
    }

    public Label getRpmLabel() {
        return rpmLabel;
    }

    public Label getEnvTemperatureLabel() {
        return envTemperatureLabel;
    }

    public Label getMaxBurnCalLabel() {
        return maxBurnCalLabel;
    }

    public ImageView getLeftImageView() {
        return leftImageView;
    }

    public ImageView getRightImageView() {
        return rightImageView;
    }

    @FXML
    private void newGame() {
        GameController.get().newGame();
    }

    @FXML
    private void runGame() {
        GameController.get().runGame();
    }
}
