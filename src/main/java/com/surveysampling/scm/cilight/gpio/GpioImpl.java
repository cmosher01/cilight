package com.surveysampling.scm.cilight.gpio;

import com.pi4j.io.gpio.*;

/**
 * Created by cmosher on 4/1/14.
 */
class GpioImpl implements Gpio {
    private final GpioPinDigitalOutput pin;

    public GpioImpl(final Pin idPin, final String namePin) {
        this.pin = com.pi4j.io.gpio.GpioFactory.getInstance().provisionDigitalOutputPin(idPin, namePin, PinState.LOW);
    }

    @Override
    public void set(final boolean turnOn) {
        this.pin.setState(turnOn);
    }
}
