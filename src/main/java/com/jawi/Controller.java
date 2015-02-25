package com.jawi;

import com.jawi.game.GameController;
import com.jawi.usb.UsbProxy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * Created by man9527 on 2015/2/20.
 */
public class Controller {
    @FXML
    private Label clock;

    @FXML
    private Slider calBurn;

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

    @FXML
    private void newGame() {
        System.out.println("new game");
        GameController.get().newGame();
    }
}
