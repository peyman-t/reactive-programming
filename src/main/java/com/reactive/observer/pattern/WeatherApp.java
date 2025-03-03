package com.reactive.observer.pattern;

public class WeatherApp {
    public static void main(String[] args) {
        // Create the subject (publisher)
        WeatherStation station = new WeatherStation();

        // Create observers
        WeatherDisplay display1 = new WeatherDisplay("Living Room Display");
        WeatherDisplay display2 = new WeatherDisplay("Bedroom Display");
        WeatherLogger logger = new WeatherLogger();

        // Register observers with the subject
        station.addObserver(display1);
        station.addObserver(display2);
        station.addObserver(logger);

        // Simulate temperature changes
        station.setTemperature(22.5);

        // Remove an observer
        station.removeObserver(display2);

        // Set temperature again - notice display2 doesn't get notified
        station.setTemperature(23.8);

        // Add a new observer at runtime
        WeatherDisplay mobileDisplay = new WeatherDisplay("Mobile App Display");
        station.addObserver(mobileDisplay);

        // Set temperature again - the new observer gets notified
        station.setTemperature(21.3);
    }
}
