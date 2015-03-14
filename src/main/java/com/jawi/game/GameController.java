package com.jawi.game;

import com.jawi.Controller;
import com.jawi.animation.RotateImageAnimation;
import com.jawi.usb.UsbListener;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;

import javafx.util.Duration;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by man9527 on 2015/2/20.
 */
public class GameController implements UsbListener {

    private static final GameController instance = new GameController();
    private static final String formatStr = "%02d";
    private static final IntegerProperty maxBurnCal=new SimpleIntegerProperty(0);

    private final List<ArrayList<Image>> imageGroups = new ArrayList<>();

    private ParallelTransition currentTransition;
    private ParallelTransition parallelTransition = new ParallelTransition();
    private ParallelTransition parallelTransition2 = new ParallelTransition();
    private ParallelTransition parallelTransition3 = new ParallelTransition();

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
    private volatile State state = GameState.STOPPED;
    private long receivingDataTimestamp;
    private Timer gameTimer;
    private Timer updateUITimer;
    private Timer changePeopleTimer;

    private GameStats gameStats;

    // View Controller
    private Controller controller;

    private GameController() {
        ArrayList<Image> group1 = new ArrayList<>();
        group1.add(new Image(this.getClass().getResource("/anib011.png").toString()));
        group1.add(new Image(this.getClass().getResource("/anib012.png").toString()));

        ArrayList<Image> group2 = new ArrayList<>();
        group2.add(new Image(this.getClass().getResource("/anig011.png").toString()));
        group2.add(new Image(this.getClass().getResource("/anig012.png").toString()));

        ArrayList<Image> group3 = new ArrayList<>();
        group3.add(new Image(this.getClass().getResource("/anib021.png").toString()));
        group3.add(new Image(this.getClass().getResource("/anib022.png").toString()));

        ArrayList<Image> group4 = new ArrayList<>();
        group4.add(new Image(this.getClass().getResource("/anig021.png").toString()));
        group4.add(new Image(this.getClass().getResource("/anig022.png").toString()));

        ArrayList<Image> group5 = new ArrayList<>();
        group5.add(new Image(this.getClass().getResource("/anib031.png").toString()));
        group5.add(new Image(this.getClass().getResource("/anib032.png").toString()));

        ArrayList<Image> group6 = new ArrayList<>();
        group6.add(new Image(this.getClass().getResource("/anig031.png").toString()));
        group6.add(new Image(this.getClass().getResource("/anig032.png").toString()));

        imageGroups.add(group1);
        imageGroups.add(group2);
        imageGroups.add(group3);
        imageGroups.add(group4);
        imageGroups.add(group5);
        imageGroups.add(group6);
    }

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

        controller.getLeftImageView().setTranslateY(0);
        controller.getRightImageView().setTranslateY(0);
        controller.getLeftImageView().setImage(imageGroups.get(0).get(0));
        controller.getRightImageView().setImage(imageGroups.get(1).get(0));
        this.showHideResultLabel(false);
        state=GameState.READY;
    }

    public void runGame() {
        System.out.println("game run");
        handleGameTime();
        state=GameState.RUNNING;
    }

    private void stopGame() {
        System.out.println("game stopped");
        state = GameState.STOPPED;
        gameTimer.stop();
        updateUITimer.stop();
        changePeopleTimer.stop();
        currentTransition.stop();

        controller.getGameResultTextLabel().setText(String.format(formatStr, this.gameStats.totalBurnProperty().getValue()));
        this.showHideResultLabel(true);
    }

    private void handleInterruptGame() {
        long current = System.currentTimeMillis();

        if (current-receivingDataTimestamp>20000) {
            stopGame();
        }
    }

    private void handleGameTime() {
        receivingDataTimestamp = System.currentTimeMillis();
        timeLeft = Bindings.subtract(totalGameTime, timePassed);
        parallelTransition = new ParallelTransition();
        parallelTransition2 = new ParallelTransition();
        parallelTransition3 = new ParallelTransition();

        initImages(parallelTransition, 0);
        initImages(parallelTransition2, 2);
        initImages(parallelTransition3, 4);

        currentTransition = parallelTransition;

        if (true) {
            gameTimer = FxTimer.runPeriodically(
                    java.time.Duration.ofMillis(1000),
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
                    java.time.Duration.ofMillis(1000),
                    () -> {
                        setUI((int) gameStats.getTotalBurn(), gameStats.getRpm(), gameStats.getHighTemperature(), gameStats.getLowTemperature(), gameStats.getEnvTemperature());
                    });

            changePeopleTimer = FxTimer.runPeriodically(
                    java.time.Duration.ofMillis(20000),
                    () -> {
                        ParallelTransition nextTransition=null;
                        System.out.println(timePassed.get());
                        if (timePassed.get()>=20 && timePassed.get()<40) {
                            System.out.println("ani2");
                            nextTransition = parallelTransition2;
                        } else if (timePassed.get()>=40 && timePassed.get()<60) {
                            System.out.println("ani3");
                            nextTransition = parallelTransition3;
                        } else if (timePassed.get()==60){
                            System.out.println("ani0"+timePassed.get());
                            nextTransition = parallelTransition;
                        }
                        currentTransition.stop();
                        moveOutPeople(nextTransition);
                        currentTransition = nextTransition;
                    });
        } else {
            gameTimer.restart();
            updateUITimer.restart();
            changePeopleTimer.restart();
        }

        currentTransition.playFromStart();
    }

    private void initImages(ParallelTransition parallelTransition, int groupNumber) {
        RotateImageAnimation animationLeft = new RotateImageAnimation(controller.getLeftImageView(), imageGroups.get(groupNumber));
        RotateImageAnimation animationRight = new RotateImageAnimation(controller.getRightImageView(), imageGroups.get(groupNumber+1));
        animationLeft.setCycleCount(Animation.INDEFINITE);
        animationRight.setCycleCount(Animation.INDEFINITE);

        TranslateTransition translateTransition = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getLeftImageView())
                .fromY(controller.getLeftImageView().getTranslateY())
                .toY(-512)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        TranslateTransition translateTransition2 = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getRightImageView())
                .fromY(controller.getRightImageView().getTranslateY())
                .toY(-512)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        parallelTransition.getChildren().addAll(
                animationLeft, animationRight, translateTransition, translateTransition2
        );
    }

    private void moveOutPeople(final ParallelTransition nextAnim) {
        if (nextAnim==null) {
            return;
        }
        TranslateTransition translateTransition = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getLeftImageView())
                .fromY(controller.getLeftImageView().getTranslateY())
                .toY(0)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        TranslateTransition translateTransition2 = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getRightImageView())
                .fromY(controller.getRightImageView().getTranslateY())
                .toY(0)
                .cycleCount(1)
                .autoReverse(false)
                .build();
        ParallelTransition moveOutTransition = new ParallelTransition();
        moveOutTransition.getChildren().addAll(
                translateTransition, translateTransition2
        );
        moveOutTransition.setOnFinished(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        nextAnim.playFromStart();
                    }
                }
        );
        moveOutTransition.play();
    }

    private void setUI(int calBurn, int rpm, int highTemperature, int lowTemperature, int envTemperature) {
        controller.getCalBurn().valueProperty().set(calBurn);
        controller.getRpmLabel().setText(String.valueOf(rpm));
        controller.getHighTemperatureLabel().setText(String.valueOf(highTemperature)+(char)186+"C");
        controller.getLowTemperatureLabel().setText(String.valueOf(lowTemperature)+(char)186+"C");
        controller.getEnvTemperatureLabel().setText(String.valueOf(envTemperature)+(char)186+"C");
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

    public void showHideResultLabel(boolean isShow) {
        if (isShow) {
           TranslateTransition translateTransition = TranslateTransitionBuilder.create()
                    .duration(Duration.seconds(0.5))
                    .node(controller.getGameResultLabel())
                    .toY(0)
                    .cycleCount(1)
                    .autoReverse(false)
                    .build();
            translateTransition.play();
        } else {
            controller.getGameResultLabel().setTranslateY(500);
        }
    }
}
