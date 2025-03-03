package com.reactive.observer.naive;

public class WeatherApp {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();

        // Simulate temperature changes
        station.setTemperature(22.5);
        station.setTemperature(23.8);

        // Problem: We can't easily add new observers or remove existing ones
        // Problem: WeatherStation is tightly coupled to specific implementation classes
    }
}