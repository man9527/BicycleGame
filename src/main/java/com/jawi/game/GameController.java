package com.jawi.game;

import com.jawi.Controller;
import com.jawi.usb.UsbListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.reactfx.Subscription;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import java.time.Duration;

/**
 * Created by man9527 on 2015/2/20.
 */
public class GameController implements UsbListener {

    private static GameController instance = new GameController();

    public enum GameState {
        STOPPED, RUNNING
    }

    private final IntegerProperty totalGameTime = new SimpleIntegerProperty(60);

    // game state control
    private IntegerProperty timePassed;
    private NumberBinding timeLeft;
    private GameState state = GameState.STOPPED;
    private long receivingDataTimestamp;
    Timer gameTimer;
    Timer updateUITimer;
    private GameStats gameStats;

    // View Controller
    private Controller controller;

    private GameController() {}
    public static GameController get() {return instance;}

    public void setView(Controller controller) {
        this.controller=controller;
    }

    public void newGame() {
        handleGameTime();
        gameStats = new GameStats(timePassed);
        configSlider();
    }


    private void handleInterruptGame() {
        long current = System.currentTimeMillis();

        if (current-receivingDataTimestamp>20000) {
            stopGame();
        }
    }

    private void stopGame() {
        gameTimer.stop();
        updateUITimer.stop();
    }

    private void handleGameTime() {
        timePassed = new SimpleIntegerProperty(0);
        timeLeft = Bindings.subtract(totalGameTime, timePassed);

        if (gameTimer==null) {
            gameTimer = FxTimer.runPeriodically(
                    Duration.ofMillis(1000),
                    () -> {
                        handleInterruptGame();
                        timePassed.set(timePassed.get() + 1);
                        controller.getClock().setText(String.valueOf(timeLeft.getValue()));

                        if (timeLeft.getValue().intValue() == 0) {
                            stopGame();
                        }
                    });

            updateUITimer = FxTimer.runPeriodically(
                    Duration.ofMillis(100),
                    () -> {
                        controller.getCalBurn().valueProperty().set(gameStats.getTotalBurn());
                    });
        } else {
            gameTimer.restart();
            updateUITimer.restart();
        }
    }

    private void configSlider() {
        //controller.getCalBurn().valueProperty().bind(gameStats.getCalBurn());
    }

    @Override
    public void setRpm(int rpm) {
        receivingDataTimestamp = System.currentTimeMillis();
        if (this.gameStats!=null) {
            this.gameStats.setRpm(rpm);
        }
    }

    @Override
    public void setHighTemperature(int degree) {
        receivingDataTimestamp = System.currentTimeMillis();
    }

    @Override
    public void setLowTemperature(int degree) {
        receivingDataTimestamp = System.currentTimeMillis();
    }
}
