package com.reactive.observer.naive;

public class WeatherDisplay {
    private String name;

    public WeatherDisplay(String name) {
        this.name = name;
    }

    public void update(double temperature) {
        System.out.println(name + " displaying temperature: " + temperature + "°C");
    }
}
