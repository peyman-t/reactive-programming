package com.reactive.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Custom temperature collection that implements the Iterable interface
 * to demonstrate the Iterator pattern
 */
public class TemperatureCollection implements Iterable<Double> {
    private final List<Double> temperatures;

    public TemperatureCollection() {
        this.temperatures = new ArrayList<>();
    }

    // Add a temperature reading to the collection
    public void addTemperature(double temperature) {
        temperatures.add(temperature);
    }

    // Get the number of temperature readings
    public int size() {
        return temperatures.size();
    }

    // Get a temperature at a specific index
    public Double getTemperature(int index) {
        if (index < 0 || index >= temperatures.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + temperatures.size());
        }
        return temperatures.get(index);
    }

    // Standard Iterator implementation using Java's Iterator interface
    @Override
    public Iterator<Double> iterator() {
        return new TemperatureIterator();
    }

    // Custom iterator implementation
    private class TemperatureIterator implements Iterator<Double> {
        private int position = 0;

        @Override
        public boolean hasNext() {
            return position < temperatures.size();
        }

        @Override
        public Double next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return temperatures.get(position++);
        }
    }

    // Custom iterator that only returns temperatures above a threshold
    public Iterator<Double> aboveThresholdIterator(double threshold) {
        return new Iterator<Double>() {
            private int position = 0;
            private Double nextElement = null;

            // Helper method to find the next element above threshold
            private void findNext() {
                // Reset next element
                nextElement = null;

                // Find next temperature above threshold
                while (position < temperatures.size() && nextElement == null) {
                    Double current = temperatures.get(position++);
                    if (current > threshold) {
                        nextElement = current;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                if (nextElement == null) {
                    findNext();
                }
                return nextElement != null;
            }

            @Override
            public Double next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Double result = nextElement;
                nextElement = null; // Reset for the next call to hasNext()
                return result;
            }
        };
    }
}