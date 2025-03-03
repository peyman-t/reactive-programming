package com.reactive.functional;

class EnvironmentalReport {
    private final double temperature;
    private final double humidity;

    public EnvironmentalReport(double temperature, double humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }
}