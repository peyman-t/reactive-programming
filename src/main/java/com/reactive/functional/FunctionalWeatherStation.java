package com.reactive.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Weather station implementation with functional approach
class FunctionalWeatherStation {
    private final List<Consumer<Temperature>> temperatureObservers = new ArrayList<>();
    private double currentTemperature;

    // Register observer using functional interface
    public void addTemperatureObserver(Consumer<Temperature> observer) {
        temperatureObservers.add(observer);
    }

    // Remove observer
    public void removeTemperatureObserver(Consumer<Temperature> observer) {
        temperatureObservers.remove(observer);
    }

    // Update temperature and notify observers
    public void setTemperature(double value) {
        currentTemperature = value;
        Temperature temperature = new Temperature(value);

        // Notify all observers
        temperatureObservers.forEach(observer -> observer.accept(temperature));
    }

    // Temperature value object
    public static class Temperature {
        private final double value;
        private final long timestamp;

        public Temperature(double value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public double getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}