package com.reactive.iterator;

import java.util.Iterator;

/**
 * Demonstration of the Iterator pattern
 *
 * The key concept demonstrated is how the Iterator pattern
 * provides a way to access elements of a collection sequentially
 * without exposing the underlying implementation. This supports
 * the "Pull" model where the consumer decides when to retrieve
 * the next element, contrasting with the "Push" model used in the
 * Observer pattern.
 */
public class IteratorPatternDemo {
    public static void main(String[] args) {
        // Create a collection of temperature readings
        TemperatureCollection temperatures = new TemperatureCollection();

        // Add some temperature readings
        temperatures.addTemperature(18.4);
        temperatures.addTemperature(18.5);
        temperatures.addTemperature(18.7);
        temperatures.addTemperature(18.8);
        temperatures.addTemperature(18.6);

        System.out.println("Using standard for-each loop (implicit iterator):");
        for (Double temperature : temperatures) {
            System.out.println("Temperature: " + temperature + "°C");
        }

        System.out.println("\nUsing explicit iterator:");
        Iterator<Double> iterator = temperatures.iterator();
        while (iterator.hasNext()) {
            System.out.println("Temperature: " + iterator.next() + "°C");
        }

        // Behind the scenes, the for-each loop works exactly like the explicit iterator example

        System.out.println("\nComparing the two iterator examples from the lecture:");

        // Example 1: Using foreach loop (as shown in the lecture)
        System.out.println("\nUsing foreach loop:");
        for (Double temperature : temperatures) {
            System.out.println(temperature);
        }

        // Example 2: Using iterator directly (as shown in the lecture)
        System.out.println("\nUsing iterator directly:");
        Iterator<Double> tempIterator = temperatures.iterator();
        while (tempIterator.hasNext()) {
            System.out.println(tempIterator.next());
        }

        // Demonstrating a specialized iterator that filters values
        System.out.println("\nUsing specialized iterator (temperatures above 18.6°C):");
        Iterator<Double> aboveThresholdIterator = temperatures.aboveThresholdIterator(18.6);
        while (aboveThresholdIterator.hasNext()) {
            System.out.println("Temperature: " + aboveThresholdIterator.next() + "°C");
        }

        // Demonstrate the "Pull" model of communication that the lecture mentions
        System.out.println("\nDemonstrating the 'Pull' model of Iterator pattern:");
        Iterator<Double> pullIterator = temperatures.iterator();
        System.out.println("Consumer: Let me check if there's data available");
        if (pullIterator.hasNext()) {
            System.out.println("Consumer: Yes, there is! Let me pull the next value");
            Double value = pullIterator.next();
            System.out.println("Consumer: I pulled the value: " + value + "°C");
        }
    }
}