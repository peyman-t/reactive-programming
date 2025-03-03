package com.reactive.functional;

class SensorData {
    private final Temperature temperature;
    private final Humidity humidity;

    public SensorData(Temperature temperature, Humidity humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Humidity getHumidity() {
        return humidity;
    }
}