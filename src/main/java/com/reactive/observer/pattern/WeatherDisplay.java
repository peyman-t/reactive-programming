package com.reactive.observer.pattern;

public class WeatherDisplay implements TemperatureObserver {
    private String name;

    public WeatherDisplay(String name) {
        this.name = name;
    }

    @Override
    public void update(double temperature) {
        System.out.println(name + " displaying temperature: " + temperature + "°C");
    }

    public String getName() {
        return name;
    }
}