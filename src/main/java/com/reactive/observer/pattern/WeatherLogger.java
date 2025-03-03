package com.reactive.observer.pattern;

public class WeatherLogger implements TemperatureObserver {
    @Override
    public void update(double temperature) {
        System.out.println("Logging temperature: " + temperature + "°C");
    }
}