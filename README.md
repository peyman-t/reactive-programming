I'll update the README to remove the reference to the `VirtualTimeExtension` that isn't being used. Here's the corrected version:

# Project Reactor in Java

This project demonstrates the practical application of Project Reactor, a reactive programming library for building non-blocking applications on the JVM, along with a custom implementation of Functional Reactive Programming (FRP) principles in Java.

## Project Overview

The implementation includes:

- Complete examples of Project Reactor usage (Mono, Flux, operators)
- Core reactive streams interfaces (Publisher, Subscriber, Subscription)
- Multiple publisher implementations with different characteristics
- Functional composition through map() and filter() operations
- Backpressure handling strategies
- Demonstration of the Observer pattern with functional extensions
- Hot and Cold publishers
- Error handling patterns
- Comprehensive testing suite for reactive streams

## Project Structure

```
project-reactor-demo/
    ├── com.reactive.reactor
    │   └── ProjectReactorDemo.java  - Main demo for Project Reactor concepts
    │
    ├── com.reactive.functional     - Custom FRP implementations
    │   ├── BufferedPublisher.java    - Publisher with buffer-based backpressure
    │   ├── EnvironmentalReport.java  - Data model for environmental reports
    │   ├── FRPDemo.java              - Demo for custom FRP implementation 
    │   ├── FunctionalPublisher.java  - Core publisher with functional operations
    │   ├── FunctionalWeatherStation.java - Observer pattern with functional callbacks
    │   ├── ImmutableMessage.java     - Immutable message model
    │   ├── SafePublisher.java        - Publisher with weak reference handling
    │   ├── TemperatureProcessor.java - Data processing example
    │   └── ThrottlingPublisher.java  - Publisher with rate limiting
    │
    ├── com.reactive.iterator       - Iterator pattern implementations
    │   ├── IteratorPatternDemo.java  - Iterator pattern demo
    │   └── TemperatureCollection.java - Custom iterable collection
    │
    ├── com.reactive.observer       - Observer pattern implementations
    │   ├── naive                    - Basic observer implementations
    │   └── pattern                  - Advanced observer pattern examples
    │
    └── test
        └── java                     - Test classes
            ├── ReactiveTestingExamples.java - Testing reactive streams
            ├── ChatServiceTest.java - Testing grouping operations
            ├── UserGeneratorTest.java - Testing virtual time
            └── WeatherServiceUnitTest.java - Testing with mocks
```

## Key Components

### Project Reactor Core

- **Mono<T>**: Publisher that emits 0 or 1 element
- **Flux<T>**: Publisher that emits 0 to N elements
- **Operators**: Transformation, filtering, and combination functions
- **Subscription**: Control of data flow with backpressure

### Custom FRP Implementation

- **Publisher<T>**: Source that emits elements to subscribers
- **Subscriber<T>**: Consumer that receives elements from a publisher
- **Subscription**: Connection between publisher and subscriber for flow control
- **FunctionalPublisher<T>**: Enhances Publisher with functional operators (map, filter)
- **BufferedPublisher<T>**: Handles backpressure through buffering
- **ThrottlingPublisher<T>**: Enforces minimum interval between emissions

## Project Reactor Usage Examples

### Basic Flux and Mono

```java
// Creating a Flux
Flux<String> animalFlux = Flux.just("dog", "cat", "bird", "fish")
                .delayElements(Duration.ofMillis(500));

// Subscribing with error and completion handlers
animalFlux.subscribe(
        animal -> System.out.println("Received: " + animal),
error -> System.err.println("Error occurred: " + error.getMessage()),
        () -> System.out.println("Animal stream completed!")
);

// Creating a Mono
Mono<String> simpleMono = Mono.just("Hello Reactor!");
simpleMono.subscribe(
        value -> System.out.println("Received: " + value),
error -> System.err.println("Error: " + error.getMessage()),
        () -> System.out.println("Mono completed")
);
```

### Operators

```java
// Filtering
Flux.range(1, 10)
    .filter(n -> n % 2 == 0)
        .subscribe(n -> System.out.println("Even number: " + n));

// Transformation
        Flux.range(1, 5)
    .map(n -> n * n)
        .subscribe(n -> System.out.println("Squared: " + n));

// Combination
        Flux.zip(
        Flux.just(1, 2, 3), 
    Flux.just("one", "two", "three"),
    (number, name) -> number + ":" + name
).subscribe(pair -> System.out.println("Zip result: " + pair));
```

### Custom Subscriber with Backpressure

```java
Flux<Long> intervalFlux = Flux.interval(Duration.ofMillis(200)).take(10);

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

        // Request next batch when current batch is consumed
        if (count % BATCH_SIZE == 0) {
            System.out.println("Requesting next batch of " + BATCH_SIZE);
            request(BATCH_SIZE);
        }
    }
});
```

## Testing Reactive Streams

The project includes a comprehensive testing suite that demonstrates how to properly test reactive streams:

### Testing with StepVerifier

```java
@Test
void simpleMonoVerifier() {
    Mono<String> mono = Mono.just("dog");

    StepVerifier.create(mono)
            .expectNext("dog")
            .expectComplete()
            .verify();
}
```

### Testing Flux Content

```java
@Test
void checkFluxContent() {
    Flux<Integer> numbersFlux = Flux.just(3, 5, 2, 7, 5, 2);

    StepVerifier.create(numbersFlux)
            .expectNextMatches(v -> v < 4) // check if next value satisfies condition
            .expectNextCount(4) // expect receiving 4 more elements
            .expectNext(2) // check whether next value equals 2
            .expectComplete()
            .verify();
}
```

### Testing Error Handling

```java
@Test
void verifyErrorHandling() {
    Mono<String> errorMono = Mono.error(new IllegalArgumentException("Invalid data"));

    StepVerifier.create(errorMono)
            .expectError(IllegalArgumentException.class)
            .verify();
}
```

### Testing with Virtual Time

```java
@Test
void verifyFluxWithDelayUsingVirtualTime() {
    List<Integer> data = List.of(1, 2, 3);
    StepVerifier.withVirtualTime(() -> Flux.fromIterable(data).delayElements(Duration.ofHours(1)))
            .thenAwait(Duration.ofHours(3)) // simulate waiting for specified time (3h)
            .expectNext(1, 2, 3) // the elements should be ready after that time
            .verifyComplete(); // shortcut for expectComplete().verify()
}
```

### Testing with TestPublisher

```java
@Test
void verifyBehaviorUsingTestPublisher() {
    // create new instance of TestPublisher
    TestPublisher<Integer> numberPublisher = TestPublisher.create();

    // to create converter, flux must be retrieved from publisher
    BinaryConverter binaryConverter = new BinaryConverter(numberPublisher.flux());

    StepVerifier.create(binaryConverter.getBinaryRepresentation())
            .then(() -> numberPublisher.next(5, 6, 7)) // emit 3 elements
            .expectNext("101", "110", "111")
            .then(() -> numberPublisher.complete()) // send onComplete signal
            .verifyComplete();
}
```

## Dependencies

For the Project Reactor and testing:

```gradle
dependencies {
    implementation platform('io.projectreactor:reactor-bom:2024.0.0')
    implementation 'io.projectreactor:reactor-core'
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testImplementation 'org.mockito:mockito-core:5.10.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.10.0'
}
```

## Running the Demos

### Project Reactor Demo

```java
package com.reactive.reactor;

public class ProjectReactorDemo {
    public static void main(String[] args) throws Exception {
        // Implementation examples...
    }
}
```

### Running Tests

```bash
# Using Maven
mvn test

# Using Gradle
gradle test
```

## Further Reading

- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Project Reactor Testing Guide](https://projectreactor.io/docs/core/release/reference/testing.html)
- [Reactive Streams Specification](https://github.com/reactive-streams/reactive-streams-jvm)
- [Introduction to Reactive Programming](https://gist.github.com/staltz/868e7e9bc2a7b8c1f754)

## License

This project is available under the MIT License.