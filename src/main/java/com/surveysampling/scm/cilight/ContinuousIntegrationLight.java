package com.surveysampling.scm.cilight;

import com.surveysampling.scm.cilight.ci.ContinuousIntegrationServer;
import com.surveysampling.scm.cilight.ci.ContinuousIntegrationState;
import com.surveysampling.scm.cilight.trafficlight.TrafficLight;
import com.surveysampling.scm.cilight.trafficlight.TrafficLightState;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by cmosher on 3/31/14.
 */
public class ContinuousIntegrationLight {
    private boolean shouldRun = true;
    private boolean testMode = false;

    public static void main(final String... args) throws InterruptedException, ExecutionException {
        ContinuousIntegrationLight light = new ContinuousIntegrationLight();
        light.handleArguments(args);
        light.run();
    }

    private void handleArguments(final String... args) {
        for (final String arg : args) {
            if (arg.startsWith("-")) {
                switch (arg.substring(1).charAt(0)) {
                    case 't':
                        testMode = true;
                        break;
                    default:
                        shouldRun = false;
                        help();
                }
            }
        }
    }

    private void help() {
        System.out.println("java -jar cilight.jar [-t]");
        System.out.println("OPTIONS");
        System.out.println("  -t  test mode (only log messages)");
    }

    private void run() throws ExecutionException, InterruptedException {
        if (!this.shouldRun) {
            return;
        }

        final ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        final TrafficLight trafficLight = new TrafficLight(this.testMode);


        final ScheduledFuture<?> future = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final ContinuousIntegrationState ciState = ci.getState();
                final TrafficLightState state = convertCiStateToTrafficLightState(ciState);
                trafficLight.setState(state);
            }
        }, 0, 15, TimeUnit.SECONDS);


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                trafficLight.setState(TrafficLightState.OFF);
                future.cancel(true);
            }
        });


        /* just wait forever in the main thread */
        future.get();
    }

    private static TrafficLightState convertCiStateToTrafficLightState(ContinuousIntegrationState ciState) {
        switch (ciState) {
            case FAILURE:
                return TrafficLightState.RED;
            case UNSTABLE:
                return TrafficLightState.YELLOW;
            case SUCCESS:
                return TrafficLightState.GREEN;
            default:
                return TrafficLightState.OFF;
        }
    }
}
