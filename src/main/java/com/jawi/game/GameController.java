package com.jawi.game;

import com.jawi.Controller;
import com.jawi.usb.UsbListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import java.time.Duration;

/**
 * Created by man9527 on 2015/2/20.
 */
public class GameController implements UsbListener {

    private static final GameController instance = new GameController();
    private static final String formatStr = "%02d";
    private static final IntegerProperty maxBurnCal=new SimpleIntegerProperty(0);

    interface State {
        State next();
    }

    public enum GameState implements State {
        READY {
            @Override
            public State next() {
                GameController.get().runGame();
                return RUNNING;
            }
        }, STOPPED {
            @Override
            public State next() {
                GameController.get().newGame();
                return READY;
            }
        }, RUNNING {
            @Override
            public State next() {
                GameController.get().stopGame();
                return STOPPED;
            }
        }
    }

    private final IntegerProperty totalGameTime = new SimpleIntegerProperty(60);

    // game state control
    private IntegerProperty timePassed;
    private NumberBinding timeLeft;
    private State state = GameState.STOPPED;
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

    public void next() {
        this.state.next();
    }

    public void newGame() {
        if (state.equals(GameState.RUNNING)) {
            stopGame();
        }
        timePassed = new SimpleIntegerProperty(0);
        gameStats = new GameStats(timePassed);
        setUI((int) gameStats.getTotalBurn(), gameStats.getRpm(), gameStats.getHighTemperature(), gameStats.getLowTemperature(), gameStats.getEnvTemperature());
        controller.getClock().setText("60");

        state=GameState.READY;
    }

    public void runGame() {
        handleGameTime();
        state=GameState.RUNNING;
    }

    private void stopGame() {
        state = GameState.STOPPED;
        gameTimer.stop();
        updateUITimer.stop();
    }

    private void handleInterruptGame() {
        long current = System.currentTimeMillis();

        if (current-receivingDataTimestamp>20000) {
            stopGame();
        }
    }

    private void handleGameTime() {
        timeLeft = Bindings.subtract(totalGameTime, timePassed);

        if (gameTimer==null) {
            gameTimer = FxTimer.runPeriodically(
                    Duration.ofMillis(1000),
                    () -> {
                        handleInterruptGame();
                        timePassed.set(timePassed.get() + 1);
                        controller.getClock().setText(String.format(formatStr, timeLeft.getValue()));

                        if (timeLeft.getValue().intValue() == 0) {
                            stopGame();
                        }

                        if (gameStats.getTotalBurn()>maxBurnCal.get()) {
                            maxBurnCal.set((int) gameStats.getTotalBurn());
                            controller.getMaxBurnCalLabel().setText(String.valueOf(maxBurnCal.get()));
                        }
                    });

            updateUITimer = FxTimer.runPeriodically(
                    Duration.ofMillis(100),
                    () -> {
                        setUI((int) gameStats.getTotalBurn(), gameStats.getRpm(), gameStats.getHighTemperature(), gameStats.getLowTemperature(), gameStats.getEnvTemperature());
                    });

        } else {
            gameTimer.restart();
            updateUITimer.restart();
        }
    }

    private void setUI(int calBurn, int rpm, int highTemperature, int lowTemperature, int envTemperature) {
        controller.getCalBurn().valueProperty().set(calBurn);
        controller.getRpmLabel().setText(String.valueOf(rpm));
        controller.getHighTemperatureLabel().setText(String.valueOf(highTemperature)+"°C");
        controller.getLowTemperatureLabel().setText(String.valueOf(lowTemperature)+"°C");
        controller.getEnvTemperatureLabel().setText(String.valueOf(envTemperature)+"°C");
    }

    @Override
    public void setRpm(int rpm) {
        setReceivingDataTimestamp();
        if (this.gameStats!=null) {
            if (state.equals(GameState.READY)) {
                this.runGame();
            }
            this.gameStats.setRpm(rpm);
        }
    }

    private void setReceivingDataTimestamp() {
        receivingDataTimestamp = System.currentTimeMillis();
    }

    @Override
    public void setHighTemperature(int degree) {
        if (this.gameStats!=null) {
            this.gameStats.setHighTemperature(degree);
        }
    }

    @Override
    public void setLowTemperature(int degree) {
        if (this.gameStats!=null) {
            this.gameStats.setLowTemperature(degree);
        }
    }

    @Override
    public void setEnvTemperature(int degree) {
        if (this.gameStats!=null) {
            this.gameStats.setEnvTemperature(degree);
        }
    }

}
