package com.reactive.observer.naive;

public class WeatherStation {
    private WeatherDisplay display1;
    private WeatherDisplay display2;
    private WeatherLogger logger;
    private double temperature;

    public WeatherStation() {
        this.display1 = new WeatherDisplay("Display 1");
        this.display2 = new WeatherDisplay("Display 2");
        this.logger = new WeatherLogger();
        this.temperature = 0.0;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        display1.update(temperature);
        display2.update(temperature);
        logger.update(temperature);
    }
}