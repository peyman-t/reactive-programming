package com.reactive.observer.pattern;

import java.util.ArrayList;
import java.util.List;

// Subject (Publisher)
public class WeatherStation {
    private List<TemperatureObserver> observers;
    private double temperature;

    public WeatherStation() {
        this.observers = new ArrayList<>();
        this.temperature = 0.0;
    }

    public void addObserver(TemperatureObserver observer) {
        observers.add(observer);
        System.out.println("Added observer: " +
                (observer instanceof WeatherDisplay ?
                        ((WeatherDisplay)observer).getName() : observer.getClass().getSimpleName()));
    }

    public void removeObserver(TemperatureObserver observer) {
        observers.remove(observer);
        System.out.println("Removed observer: " +
                (observer instanceof WeatherDisplay ?
                        ((WeatherDisplay)observer).getName() : observer.getClass().getSimpleName()));
    }

    public void setTemperature(double temperature) {
        System.out.println("\nWeather station: Temperature changed to " + temperature + "°C");
        this.temperature = temperature;
        notifyObservers();
    }

    private void notifyObservers() {
        for (TemperatureObserver observer : observers) {
            observer.update(temperature);
        }
    }
}