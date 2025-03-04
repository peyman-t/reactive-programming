package com.reactive.reactor;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ProjectReactorDemo {

    public static void main(String[] args) throws Exception {
        ProjectReactorDemo demo = new ProjectReactorDemo();

        System.out.println("\n=== BASIC FLUX EXAMPLE ===");
        demo.basicFluxExample();

        System.out.println("\n=== BASIC MONO EXAMPLE ===");
        demo.basicMonoExample();

        System.out.println("\n=== FLUX OPERATORS EXAMPLE ===");
        demo.fluxOperatorsExample();

        System.out.println("\n=== ERROR HANDLING EXAMPLE ===");
        demo.errorHandlingExample();

        System.out.println("\n=== CUSTOM SUBSCRIBER WITH BACKPRESSURE ===");
        demo.customSubscriberExample();

        System.out.println("\n=== COMBINING PUBLISHERS EXAMPLE ===");
        demo.combiningPublishersExample();

        System.out.println("\n=== HOT VS COLD PUBLISHERS ===");
        demo.hotVsColdExample();

        System.out.println("\n=== REACTOR COIN SIMULATION ===");
        demo.reactorCoinSimulation();
    }

    /**
     * Basic example demonstrating Flux creation and subscription
     */
    public void basicFluxExample() throws IOException {
        System.out.println("Creating a Flux with delayElements to simulate async data");

        // Create a Flux with some delay to simulate async behavior
        Flux<String> animalFlux = Flux.just("dog", "cat", "bird", "fish")
                .delayElements(Duration.ofMillis(500));

        // Subscribe with different variations

        // 1. Basic subscription - just consume the values
        animalFlux.subscribe(animal ->
                System.out.println("Received animal: " + animal));

        // Wait to see the elements (since they're delayed)
        waitForInput(2500);

        // 2. Subscribe with error and completion handlers
        Flux<String> animalsWithSignals = Flux.just("lion", "tiger", "elephant", "giraffe")
                .delayElements(Duration.ofMillis(300));

        animalsWithSignals.subscribe(
                animal -> System.out.println("Received: " + animal),
                error -> System.err.println("Error occurred: " + error.getMessage()),
                () -> System.out.println("Animal stream completed!")
        );

        // Wait to see completion
        waitForInput(1500);

        // Demonstrate async nature with multiple subscribers
        Flux<Long> intervalFlux = Flux.interval(Duration.ofMillis(300)).take(4);

        intervalFlux.subscribe(
                num -> System.out.println("Subscriber 1: " + num)
        );

        // Small delay before second subscriber
        sleep(150);

        intervalFlux.subscribe(
                num -> System.out.println("Subscriber 2: " + num)
        );

        // Wait to see the interleaving outputs
        waitForInput(1500);
    }

    /**
     * Basic examples demonstrating Mono usage
     */
    public void basicMonoExample() {
        // Create and subscribe to a simple Mono
        Mono<String> simpleMono = Mono.just("Hello Reactor!");
        simpleMono.subscribe(
                value -> System.out.println("Received: " + value),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Mono completed")
        );

        // Using empty and justOrEmpty
        Mono<String> emptyMono = Mono.empty();
        emptyMono.subscribe(
                value -> System.out.println("This won't be called for empty Mono"),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Empty Mono completed")
        );

        // Mono with nullable value
        String nullableValue = null;
        Mono.justOrEmpty(nullableValue)
                .defaultIfEmpty("Default Value")
                .subscribe(
                        value -> System.out.println("Nullable mono received: " + value),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Nullable mono completed")
                );

        // Blocking to get result
        String result = simpleMono.block();
        System.out.println("Blocked result: " + result);

        // Blocking with timeout
        try {
            Mono<String> delayedMono = Mono.just("Delayed Value")
                    .delayElement(Duration.ofSeconds(2));

            // This will timeout after 1 second
            String timeoutResult = delayedMono.block(Duration.ofSeconds(1));
            System.out.println("This won't be reached due to timeout");
        }
        catch (Exception e) {
            System.out.println("Timeout occurred as expected: " + e.getClass().getSimpleName());
        }
    }

    /**
     * Examples of various operators on Flux
     */
    public void fluxOperatorsExample() {
        System.out.println("Filtering operators:");
        // Filter operator
        Flux.range(1, 10)
                .filter(n -> n % 2 == 0)
                .subscribe(n -> System.out.println("Even number: " + n));

        System.out.println("\nTransformation operators:");
        // Map operator
        Flux.range(1, 5)
                .map(n -> n * n)
                .subscribe(n -> System.out.println("Squared: " + n));

        // FlatMap operator
        Flux.just("apple", "orange")
                .flatMap(s -> Flux.fromArray(s.split("")))
                .subscribe(s -> System.out.print(s + " "));
        System.out.println();

        System.out.println("\nLimiting operators:");
        // Take and Skip
        Flux.range(1, 10)
                .take(3)
                .subscribe(n -> System.out.println("Take 3: " + n));

        Flux.range(1, 10)
                .skip(7)
                .subscribe(n -> System.out.println("Skip 7: " + n));

        System.out.println("\nReduction operators:");
        // Reduction
        Flux.range(1, 5)
                .reduce(0, (acc, next) -> acc + next)
                .subscribe(sum -> System.out.println("Sum: " + sum));

        // Collect to list
        Flux.range(1, 5)
                .collectList()
                .subscribe(list -> System.out.println("As list: " + list));

        System.out.println("\nPeeking operators:");
        // doOnNext, doOnComplete
        Flux.range(1, 3)
                .doOnSubscribe(s -> System.out.println("Subscribed!"))
                .doOnNext(n -> System.out.println("About to emit: " + n))
                .doOnComplete(() -> System.out.println("About to complete"))
                .subscribe(n -> System.out.println("Received: " + n));
    }

    /**
     * Examples of error handling in Reactor
     */
    public void errorHandlingExample() {
        System.out.println("Basic error:");
        // Create a Flux that will produce an error
        Flux<Integer> fluxWithError = Flux.just(1, 2, 0, 4)
                .map(i -> 10 / i); // Will throw divide by zero for i=0

        // Subscribe and see the error
        fluxWithError.subscribe(
                value -> System.out.println("Value: " + value),
                error -> System.out.println("Error caught: " + error.getMessage()),
                () -> System.out.println("This won't be called due to error")
        );

        System.out.println("\nonErrorReturn example:");
        // Using onErrorReturn to provide a fallback value
        fluxWithError
                .onErrorReturn(-1) // Replace error with -1
                .subscribe(
                        value -> System.out.println("Value with fallback: " + value),
                        error -> System.out.println("This won't be called as error is handled"),
                        () -> System.out.println("Completed with fallback value")
                );

        System.out.println("\nonErrorResume example:");
        // Using onErrorResume to provide an alternative publisher
        fluxWithError
                .onErrorResume(e -> Flux.just(-1, -2, -3)) // Replace with new Flux
                .subscribe(
                        value -> System.out.println("Value with fallback flux: " + value),
                        error -> System.out.println("This won't be called as error is handled"),
                        () -> System.out.println("Completed with fallback flux")
                );

        System.out.println("\ndoFinally example:");
        // Using doFinally for cleanup
        fluxWithError
                .doFinally(signal -> System.out.println("Finally signal: " + signal))
                .onErrorReturn(-1)
                .subscribe(
                        value -> System.out.println("Finally example value: " + value),
                        error -> System.out.println("This won't be called as error is handled"),
                        () -> System.out.println("Completed")
                );
    }

    /**
     * Example of a custom subscriber with backpressure
     */
    public void customSubscriberExample() throws InterruptedException {
        // Create a CountDownLatch to wait for completion
        CountDownLatch latch = new CountDownLatch(1);

        // Create a Flux that emits every 200ms
        Flux<Long> intervalFlux = Flux.interval(Duration.ofMillis(200)).take(10);

        // Create custom subscriber with controlled request rate
        intervalFlux.subscribe(new BaseSubscriber<Long>() {
            private int count = 0;
            private final int BATCH_SIZE = 3;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println("Subscribed with batch size " + BATCH_SIZE);
                request(BATCH_SIZE);
            }

            @Override
            protected void hookOnNext(Long value) {
                count++;
                System.out.println("Received " + value + " (count: " + count + ")");

                // Simulate processing time
                sleep(300);

                // Request next batch when current batch is consumed
                if (count % BATCH_SIZE == 0) {
                    System.out.println("Requesting next batch of " + BATCH_SIZE);
                    request(BATCH_SIZE);
                }
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("Custom subscriber completed");
                latch.countDown();
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                System.err.println("Error in custom subscriber: " + throwable.getMessage());
                latch.countDown();
            }
        });

        // Wait for completion (with timeout)
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Examples of combining multiple publishers
     */
    public void combiningPublishersExample() {
        // Create source Fluxes
        Flux<Integer> evenNumbers = Flux.just(2, 4, 6, 8, 10)
                .delayElements(Duration.ofMillis(250));
        Flux<Integer> oddNumbers = Flux.just(1, 3, 5, 7, 9)
                .delayElements(Duration.ofMillis(400));

        System.out.println("concat example (sequential):");
        // concat - sequential combination
        Flux.concat(evenNumbers, oddNumbers)
                .doOnNext(n -> System.out.println("Concat: " + n))
                .blockLast(); // Block for demonstration purposes

        System.out.println("\nmerge example (interleaved):");
        // merge - interleaved by timing
        Flux.merge(evenNumbers, oddNumbers)
                .doOnNext(n -> System.out.println("Merge: " + n))
                .blockLast(); // Block for demonstration purposes

        System.out.println("\nzip example (paired):");
        // zip - pair items by position
        Flux.zip(
                Flux.just(1, 2, 3),
                Flux.just("one", "two", "three"),
                (number, name) -> number + ":" + name
        ).subscribe(pair -> System.out.println("Zip result: " + pair));
    }

    /**
     * Demonstrates the difference between hot and cold publishers
     */
    public void hotVsColdExample() throws InterruptedException {
        System.out.println("Cold publisher example:");
        // Cold publisher - each subscriber gets all values
        Flux<Integer> coldFlux = Flux.range(1, 5);

        System.out.println("First subscriber:");
        coldFlux.subscribe(n -> System.out.println("Sub1: " + n));

        System.out.println("Second subscriber:");
        coldFlux.subscribe(n -> System.out.println("Sub2: " + n));

        System.out.println("\nHot publisher example:");
        // Hot publisher - subscribers only get values from when they subscribe
        ConnectableFlux<Integer> hotFlux = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(300))
                .publish();

        // Connect to start publishing
        hotFlux.connect();

        // First subscriber starts immediately
        hotFlux.subscribe(n -> System.out.println("HotSub1: " + n));

        // Second subscriber joins after delay - will miss some values
        Thread.sleep(1000);
        hotFlux.subscribe(n -> System.out.println("HotSub2: " + n));

        // Wait for publishing to complete
        Thread.sleep(2000);
    }

    /**
     * Demonstrates the ReactorCoin simulation example
     */
    public void reactorCoinSimulation() {
        // Generate exchange rate data
        List<Double> reactorCoinRates = generateRates();
        List<Double> rxCoinRates = generateRates();

        // Create Flux from rates
        Flux<Double> reactorCoinFlux = Flux.fromIterable(reactorCoinRates)
                .delayElements(Duration.ofMillis(100));

        // First subscriber - warning for low rates
        reactorCoinFlux = Flux.fromIterable(reactorCoinRates)
                .delayElements(Duration.ofMillis(100));

        reactorCoinFlux
                .filter(rate -> rate < 1.0)
                .subscribe(rate ->
                        System.out.println("WARNING: ReactorCoin rate dropped to " + rate)
                );

        // Second subscriber - max integer difference
        Flux.fromIterable(reactorCoinRates)
                .collectList()
                .subscribe(rates -> {
                    int min = rates.stream().mapToInt(Double::intValue).min().orElse(0);
                    int max = rates.stream().mapToInt(Double::intValue).max().orElse(0);
                    System.out.println("ReactorCoin max integer difference: " + (max - min));
                });

        // Create RxCoin Flux
        Flux<Double> rxCoinFlux = Flux.fromIterable(rxCoinRates)
                .delayElements(Duration.ofMillis(100));

        // Compare both coins
        Flux.zip(reactorCoinFlux, rxCoinFlux)
                .take(10) // Only look at first 10 pairs
                .doOnNext(tuple -> {
                    double reactorRate = tuple.getT1();
                    double rxRate = tuple.getT2();

                    System.out.printf("ReactorCoin: %.2f, RxCoin: %.2f, Diff: %.2f%n",
                            reactorRate, rxRate, reactorRate - rxRate);

                    if (reactorRate - rxRate > 2.0) {
                        System.out.println("ALERT: ReactorCoin exceeds RxCoin by more than 2.0!");
                    }
                })
                .blockLast(); // This works because we call it on the Flux, not on the Disposable
    }

    /**
     * Generates a list of simulated exchange rates
     */
    private List<Double> generateRates() {
        List<Double> rates = new ArrayList<>();
        double currentRate = 4.0;
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            rates.add(currentRate);
            double change = (random.nextDouble() - 0.5);
            currentRate = Math.max(0, currentRate + change);
        }

        return rates;
    }

    /**
     * Helper method to pause execution
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Helper method to wait for input or automatically continue after timeout
     */
    private void waitForInput(int timeoutMillis) {
        try {
            System.out.println("Waiting for results... (continuing in " + (timeoutMillis/1000.0) + " seconds)");
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}