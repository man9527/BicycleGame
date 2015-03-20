package com.jawi.game;

import com.jawi.Controller;
import com.jawi.animation.RotateImageAnimation;
import com.jawi.usb.UsbListener;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;

import javafx.util.Duration;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by man9527 on 2015/2/20.
 */
public class GameController implements UsbListener {

    private static final GameController instance = new GameController();
    private static final String formatStr = "%02d";
    private static final IntegerProperty maxBurnCal=new SimpleIntegerProperty(0);

    private final List<ArrayList<Image>> imageGroups = new ArrayList<>();

    //private ParallelTransition currentTransition;
    private ParallelTransition parallelTransitionLeft1 = new ParallelTransition();
    private ParallelTransition parallelTransitionLeft2 = new ParallelTransition();
    private ParallelTransition parallelTransitionLeft3 = new ParallelTransition();

    private ParallelTransition parallelTransitionRight1 = new ParallelTransition();
    private ParallelTransition parallelTransitionRight2 = new ParallelTransition();
    private ParallelTransition parallelTransitionRight3 = new ParallelTransition();

    private IntegerProperty leftImageIndex = new SimpleIntegerProperty(1);
    private IntegerProperty rightImageIndex = new SimpleIntegerProperty(1);

    private ParallelTransition leftCurrentTransition;
    private ParallelTransition rightCurrentTransition;

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
    //private Timer changePeopleTimer;

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

        this.leftImageIndex.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue,
                                Object newValue) {
                int index = Integer.parseInt(newValue.toString());

                GameController.this.leftCurrentTransition.stop();
                ParallelTransition p = parallelTransitionLeft1;

                if (index==1) {
                    parallelTransitionLeft1.getChildren().removeAll();//=new ParallelTransition();
                    initLeftImages(parallelTransitionLeft1, 0);
                    p=parallelTransitionLeft1;
                } else if (index==2) {
                    parallelTransitionLeft2.getChildren().removeAll();//=new ParallelTransition();
                    initLeftImages(parallelTransitionLeft2, 2);
                    p=parallelTransitionLeft2;
                } else if (index==3) {
                    parallelTransitionLeft3.getChildren().removeAll();//=new ParallelTransition();
                    initLeftImages(parallelTransitionLeft3, 4);
                    p=parallelTransitionLeft3;
                }
                moveOutLeftPeople(p);
            }
        });

        this.rightImageIndex.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue,
                                Object newValue) {
                int index = Integer.parseInt(newValue.toString());
                GameController.this.rightCurrentTransition.stop();
                System.out.println("change event triggered:" + index);
                ParallelTransition p = parallelTransitionRight1;

                if (index==1) {
                    parallelTransitionRight1=new ParallelTransition();
                    initRightImages(parallelTransitionRight1, 1);
                    p=parallelTransitionRight1;
                } else if (index==2) {
                    parallelTransitionRight2=new ParallelTransition();
                    initRightImages(parallelTransitionRight2, 3);
                    p=parallelTransitionRight2;
                } else if (index==3) {
                    parallelTransitionRight3=new ParallelTransition();
                    initRightImages(parallelTransitionRight3, 5);
                    p=parallelTransitionRight3;
                }
                moveOutRightPeople(p);
            }
        });
    }

    public static GameController get() {return instance;}

    public void setView(Controller controller) {
        this.controller=controller;
    }

    public void next(GameState verifyState) {
        System.out.print("From "+this.state);
        synchronized (this.state) {
            if (verifyState==null || this.state.equals(verifyState)) {
                this.state.next();
            }
        }
        System.out.print(" To "+this.state);
    }

    public void newGame() {
        if (state.equals(GameState.RUNNING)) {
            stopGame();
        }

        timePassed = new SimpleIntegerProperty(0);
        if (gameStats==null) {
            gameStats = new GameStats(timePassed);
        } else {
            gameStats = new GameStats(timePassed, gameStats.getEnvTemperature(), gameStats.getHighTemperature(), gameStats.getLowTemperature());
        }
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
        gameStats.setInitHighTemperature(gameStats.getHighTemperature());
        gameStats.setInitLowTemperature(gameStats.getLowTemperature());
        handleGameTime();
        state=GameState.RUNNING;
    }

    private void stopGame() {
        state = GameState.STOPPED;
        updateUITimer.stop();
        gameTimer.stop();
        this.leftCurrentTransition.stop();
        this.rightCurrentTransition.stop();

        controller.getGameResultTextLabel().setText(String.format("%03d", this.gameStats.totalBurnProperty().getValue()));
        this.showHideResultLabel(true);

        //auto restart
        FxTimer.runLater(java.time.Duration.ofSeconds(20), ()-> {
            if (this.state.equals(GameState.STOPPED)) {
                this.next(GameState.STOPPED);
            }
        });
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
        parallelTransitionLeft1 = new ParallelTransition();
        parallelTransitionLeft2 = new ParallelTransition();
        parallelTransitionLeft3 = new ParallelTransition();
        parallelTransitionRight1 = new ParallelTransition();
        parallelTransitionRight2 = new ParallelTransition();
        parallelTransitionRight3 = new ParallelTransition();

        initLeftImages(parallelTransitionLeft1, 0);
        initLeftImages(parallelTransitionLeft2, 2);
        initLeftImages(parallelTransitionLeft3, 4);

        initRightImages(parallelTransitionRight1, 1);
        initRightImages(parallelTransitionRight2, 3);
        initRightImages(parallelTransitionRight3, 5);

        //new ParallelTransition(parallelTransitionLeft1, parallelTransitionRight1).play();

//        int leftIndex = this.determineNextLeftImage();
//        int rightIndex = this.determineNextLeftImage();

//        if (leftIndex==1) {
//            this.leftCurrentTransition=parallelTransitionLeft1;
//        } else if (leftIndex==2) {
//            this.leftCurrentTransition=parallelTransitionLeft2;
//        } else {
//            this.leftCurrentTransition=parallelTransitionLeft3;
//        }
//
//        if (rightIndex==1) {
//            this.rightCurrentTransition=parallelTransitionRight1;
//        } else if (rightIndex==2) {
//            this.rightCurrentTransition=parallelTransitionRight2;
//        } else {
//            this.rightCurrentTransition=parallelTransitionRight3;
//        }

        this.leftCurrentTransition = parallelTransitionLeft1;
        this.rightCurrentTransition = parallelTransitionRight1;

        this.leftCurrentTransition.play();
        this.rightCurrentTransition.play();

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

                        if (gameStats.totalBurnProperty().get()>maxBurnCal.get()) {
                            maxBurnCal.set((int) gameStats.totalBurnProperty().get());
                            controller.getMaxBurnCalLabel().setText(String.valueOf(maxBurnCal.get()));
                        }
                    });

            updateUITimer = FxTimer.runPeriodically(
                    java.time.Duration.ofMillis(1000),
                    () -> {
                        setUI((int) gameStats.getTotalBurn(), gameStats.getRpm(), gameStats.getHighTemperature(), gameStats.getLowTemperature(), gameStats.getEnvTemperature());
                        this.leftImageIndex.set(this.determineNextLeftImage());
                        this.rightImageIndex.set(this.determineNextRightImage());

                    });
        } else {
            gameTimer.restart();
            updateUITimer.restart();
        }
    }

    private int determineNextLeftImage() {
        float diff = gameStats.getLowTemperatureDiff();
        System.out.println("low temp diff:" + diff);
        if (diff<-3.5) {
            return 3;
        } else if (diff>-1.75) {
            return 1;
        } else {
            return 2;
        }
    }

    private int determineNextRightImage() {
        float diff = gameStats.getHighTemperatureDiff();
        System.out.println("high temp diff:"+ diff);
        if (diff < 1.75) {
            return 1;
        } else if (diff>3.5) {
            return 3;
        } else {
            return 2;
        }
    }

    private void initLeftImages(ParallelTransition parallelTransition, int groupNumber) {
        RotateImageAnimation animationLeft = new RotateImageAnimation(controller.getLeftImageView(), imageGroups.get(groupNumber));
        animationLeft.setCycleCount(Animation.INDEFINITE);

        TranslateTransition translateTransition = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getLeftImageView())
                .fromY(0)
                .toY(-502)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        translateTransition.setDelay(Duration.millis(200));

        parallelTransition.getChildren().addAll(
                animationLeft, translateTransition
        );
    }

    private void initRightImages(ParallelTransition parallelTransition, int groupNumber) {
        RotateImageAnimation animationRight = new RotateImageAnimation(controller.getRightImageView(), imageGroups.get(groupNumber));
        animationRight.setCycleCount(Animation.INDEFINITE);

        TranslateTransition translateTransition2 = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getRightImageView())
                .fromY(0)
                .toY(-502)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        translateTransition2.setDelay(Duration.millis(200));

        parallelTransition.getChildren().addAll(
                animationRight, translateTransition2
        );
    }
    private void moveOutRightPeople(final ParallelTransition nextAnim) {
        System.out.println("move out right people");
        if (nextAnim==null) {
            return;
        }

        //this.rightCurrentTransition.stop();
        this.rightCurrentTransition=nextAnim;

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
                translateTransition2
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

    private void moveOutLeftPeople(final ParallelTransition nextAnim) {
        if (nextAnim==null) {
            return;
        }

        //this.leftCurrentTransition.stop();
        this.leftCurrentTransition=nextAnim;

        TranslateTransition translateTransition = TranslateTransitionBuilder.create()
                .duration(Duration.seconds(0.5))
                .node(controller.getLeftImageView())
                .fromY(controller.getLeftImageView().getTranslateY())
                .toY(0)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        ParallelTransition moveOutTransition = new ParallelTransition();
        moveOutTransition.getChildren().addAll(
                translateTransition
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

    private void setUI(int calBurn, int rpm, float highTemperature, float lowTemperature, float envTemperature) {

        if (rpm>0 && rpm/20>0) {
            rpm=rpm+new Random().nextInt(rpm/20);
        }

        controller.getCalBurn().valueProperty().set(calBurn);
        controller.getRpmLabel().setText(String.valueOf(rpm));
        controller.getHighTemperatureLabel().setText(String.format("%.1f", highTemperature)+(char)186+"C");
        controller.getLowTemperatureLabel().setText(String.format("%.1f", lowTemperature)+(char)186+"C");
        controller.getEnvTemperatureLabel().setText(String.format("%.1f", envTemperature)+(char)186+"C");
    }

    @Override
    public void setRpm(int rpm) {
        setReceivingDataTimestamp();
        if (this.gameStats!=null) {
            if (state.equals(GameState.READY)) {
                this.next(GameState.READY);
            }
            this.gameStats.setRpm(rpm);
        }
    }

    private void setReceivingDataTimestamp() {
        receivingDataTimestamp = System.currentTimeMillis();
    }

    @Override
    public void setHighTemperature(float degree) {
        if (this.gameStats!=null) {
            this.gameStats.setHighTemperature(degree);
        }
    }

    @Override
    public void setLowTemperature(float degree) {
        if (this.gameStats!=null) {
            this.gameStats.setLowTemperature(degree);
        }
    }

    @Override
    public void setEnvTemperature(float degree) {
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
