package com.jawi.game;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Created by man9527 on 2015/2/20.
 */
public class GameStats {

    private IntegerProperty rpm = new SimpleIntegerProperty(0);
    private FloatProperty envTemperature = new SimpleFloatProperty(25);
    private FloatProperty lowTemperature = new SimpleFloatProperty(20);
    private FloatProperty highTemperature = new SimpleFloatProperty(30);

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
        calBurn.set(rpm.get()/4);
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

    public void setEnvTemperature(float degree) {
        envTemperature.set(degree);
    }

    public float getEnvTemperature() {
        return envTemperature.get();
    }

    public FloatProperty envTemperatureProperty() {
        return envTemperature;
    }

    public float getLowTemperature() {
        return lowTemperature.get();
    }

    public FloatProperty lowTemperatureProperty() {
        return lowTemperature;
    }

    public void setLowTemperature(float lowTemperature) {
        this.lowTemperature.set(lowTemperature);
    }

    public float getHighTemperature() {
        return highTemperature.get();
    }

    public FloatProperty highTemperatureProperty() {
        return highTemperature;
    }

    public void setHighTemperature(float highTemperature) {
        this.highTemperature.set(highTemperature);
    }

    public int getRpm() {
        return rpm.get();
    }
}
