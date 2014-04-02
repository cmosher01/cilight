package com.surveysampling.scm.cilight.trafficlight;

import com.pi4j.io.gpio.RaspiPin;
import com.surveysampling.scm.cilight.gpio.Gpio;
import com.surveysampling.scm.cilight.gpio.GpioFactory;

import java.util.Date;

/**
 * Created by cmosher on 3/31/14.
 */
public class TrafficLight {
    private final boolean testMode;
    private final Gpio red;
    private final Gpio yellow;
    private final Gpio green;

    public TrafficLight(final boolean testMode) {
        this.testMode = testMode;
        this.red = GpioFactory.create(RaspiPin.GPIO_00, "RED", testMode);
        this.yellow = GpioFactory.create(RaspiPin.GPIO_01, "YELLOW", testMode);
        this.green = GpioFactory.create(RaspiPin.GPIO_02, "GREEN", testMode);
    }

    public void setState(final TrafficLightState state) {
        /*
        Turn on the light we want to be on (if any), and
        turn off all the other lights.
         */
        this.red.set(state.equals(TrafficLightState.RED));
        this.yellow.set(state.equals(TrafficLightState.YELLOW));
        this.green.set(state.equals(TrafficLightState.GREEN));
        if (this.testMode) {
            System.out.println("--------------------"+new Date());
        }
    }
}
