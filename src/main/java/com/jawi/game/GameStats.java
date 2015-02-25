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
    private IntegerProperty lowTemperature = new SimpleIntegerProperty(0);
    private IntegerProperty highTemperature = new SimpleIntegerProperty(0);

    private IntegerProperty totalBurn = new SimpleIntegerProperty(0);
    private NumberBinding calBurn;

    public GameStats(final IntegerProperty timePassed) {
        calBurn = Bindings.multiply(rpm, timePassed);
        calBurn.addListener((event)->{
            totalBurn.set(totalBurn.intValue()+calBurn.intValue());
        });
    }

    public double getTotalBurn() {
        return totalBurn.doubleValue();
    }

    public void setRpm(int rpm) {
        System.out.println(rpm);
        this.rpm.set(rpm);
    }

    public int getRpm() {
        return rpm.get();
    }
}
