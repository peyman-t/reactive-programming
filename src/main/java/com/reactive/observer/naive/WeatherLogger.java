package com.reactive.observer.naive;

public class WeatherLogger {
    public void update(double temperature) {
        System.out.println("Logging temperature: " + temperature + "°C");
    }
}