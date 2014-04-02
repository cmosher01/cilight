package com.surveysampling.scm.cilight.gpio;

import com.pi4j.io.gpio.Pin;

/**
 * Created by cmosher on 4/1/14.
 */
public class GpioFactory {
    public static Gpio create(final Pin idPin, final String namePin, final boolean testMode) {
        if (testMode) {
            return new GpioMock(idPin, namePin);
        } else {
            return new GpioImpl(idPin, namePin);
        }
    }
}
