package com.surveysampling.scm.cilight.gpio;

import com.pi4j.io.gpio.*;

/**
 * Created by cmosher on 4/1/14.
 */
class GpioMock implements Gpio {
    private final String name;

    public GpioMock(final Pin idPin, final String name) {
        this.name = name;
    }

    @Override
    public void set(final boolean turnOn) {
        System.out.println("turning "+(turnOn ? "ON  "+this.name.toUpperCase() : "off "+this.name.toLowerCase()));
    }
}
