package com.jawi.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Created by man9527 on 2015/2/20.
 */
public class GameStats {

    private IntegerProperty rpm = new SimpleIntegerProperty(0);
    private IntegerProperty envTemperature = new SimpleIntegerProperty(20);
    private IntegerProperty lowTemperature = new SimpleIntegerProperty(20);
    private IntegerProperty highTemperature = new SimpleIntegerProperty(30);

    private IntegerProperty totalBurn = new SimpleIntegerProperty(0);
    private IntegerProperty calBurn = new SimpleIntegerProperty(0);
    private IntegerProperty timePassed;

    public GameStats(final IntegerProperty timePassed) {
        this.timePassed = timePassed;
        calBurn.addListener((event)->{
            totalBurn.set(totalBurn.intValue()+calBurn.intValue());
        });
    }

    public double getTotalBurn() {
        calBurn.set(rpm.get()/3);
        return totalBurn.doubleValue();
    }

    public IntegerProperty totalBurnProperty() {
        return totalBurn;
    }

    public void setRpm(int rpm) {
        this.rpm.set(rpm);
    }

    public IntegerProperty rpmProperty() {
        return rpm;
    }

    public void setEnvTemperature(int degree) {
        envTemperature.set(degree);
    }

    public int getEnvTemperature() {
        return envTemperature.get();
    }

    public IntegerProperty envTemperatureProperty() {
        return envTemperature;
    }

    public int getLowTemperature() {
        return lowTemperature.get();
    }

    public IntegerProperty lowTemperatureProperty() {
        return lowTemperature;
    }

    public void setLowTemperature(int lowTemperature) {
        this.lowTemperature.set(lowTemperature);
    }

    public int getHighTemperature() {
        return highTemperature.get();
    }

    public IntegerProperty highTemperatureProperty() {
        return highTemperature;
    }

    public void setHighTemperature(int highTemperature) {
        this.highTemperature.set(highTemperature);
    }

    public int getRpm() {
        return rpm.get();
    }
}
