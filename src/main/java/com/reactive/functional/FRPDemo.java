package com.reactive.functional;

import java.util.function.Consumer;
import java.time.Duration;


public class FRPDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("---- Functional Publisher Demo ----");
        functionalPublisherDemo();

        System.out.println("\n---- Weather Station Demo ----");
        weatherStationDemo();

        System.out.println("\n---- Buffered Publisher Demo ----");
        bufferedPublisherDemo();

        System.out.println("\n---- Throttling Publisher Demo ----");
        throttlingPublisherDemo();
    }

    private static void functionalPublisherDemo() {
        // Create a publisher for integers
        FunctionalPublisher<Integer> numberPublisher = new FunctionalPublisher<>();

        // Transform the integers to their squares
        FunctionalPublisher<Integer> squaredPublisher = numberPublisher.map(n -> n * n);

        // Filter for even values only
        FunctionalPublisher<Integer> evenSquaresPublisher = squaredPublisher.filter(n -> n % 2 == 0);

        // Subscribe to the final publisher
        evenSquaresPublisher.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println("Received even square: " + item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Processing completed");
            }
        });

        // Emit some values
        for (int i = 1; i <= 5; i++) {
            System.out.println("Emitting: " + i);
            numberPublisher.emit(i);
        }

        // Signal completion
        numberPublisher.complete();
    }

    private static void weatherStationDemo() {
        FunctionalWeatherStation station = new FunctionalWeatherStation();

        // Add a simple observer using lambda
        station.addTemperatureObserver(temp ->
                System.out.println("Observer 1: Temperature is " + temp.getValue() + "°C at " + temp.getTimestamp()));

        // Add another observer that only reports significant changes
        Consumer<FunctionalWeatherStation.Temperature> criticalObserver = temp -> {
            if (temp.getValue() > 30) {
                System.out.println("Observer 2: WARNING! High temperature: " + temp.getValue() + "°C");
            }
        };
        station.addTemperatureObserver(criticalObserver);

        // Simulate temperature changes
        station.setTemperature(25.5);
        station.setTemperature(28.3);
        station.setTemperature(31.2);

        // Remove one observer
        station.removeTemperatureObserver(criticalObserver);

        // One more temperature change
        station.setTemperature(32.5);
    }

    private static void bufferedPublisherDemo() {
        // Create a buffered publisher with max 3 items
        BufferedPublisher<String> bufferedPublisher = new BufferedPublisher<>(3);

        // Subscribe with a slow consumer
        bufferedPublisher.subscribe(new Subscriber<String>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                // Request only one item at a time
                s.request(1);
            }

            @Override
            public void onNext(String item) {
                System.out.println("Received: " + item);

                // Simulate slow processing
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Request the next item
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Complete");
            }
        });

        // Publish items faster than they are consumed
        for (int i = 1; i <= 5; i++) {
            String item = "Item " + i;
            System.out.println("Publishing: " + item);
            bufferedPublisher.publish(item);
        }
    }

    private static void throttlingPublisherDemo() throws InterruptedException {
        // Create a throttling publisher with 1 second minimum interval
        ThrottlingPublisher<Long> throttlingPublisher = new ThrottlingPublisher<>(Duration.ofSeconds(1));

        // Subscribe to receive throttled events
        throttlingPublisher.subscribe(new Subscriber<Long>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Long timestamp) {
                System.out.println("Received at " + timestamp + " (current: " + System.currentTimeMillis() + ")");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Complete");
            }
        });

        // Publish events rapidly
        System.out.println("Publishing rapid events...");
        for (int i = 0; i < 5; i++) {
            throttlingPublisher.publish(System.currentTimeMillis());
            Thread.sleep(200); // Try to publish every 200ms
        }

        // Wait for all events to be processed
        Thread.sleep(5000);
        throttlingPublisher.shutdown();
    }
}