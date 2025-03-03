# Functional Reactive Programming in Java

This project demonstrates a custom implementation of Functional Reactive Programming (FRP) principles in Java. It provides a lightweight reactive streams framework that combines the functional programming paradigm with reactive principles.

## Project Overview

The implementation includes:

- Core reactive streams interfaces (Publisher, Subscriber, Subscription)
- Multiple publisher implementations with different characteristics
- Functional composition through map() and filter() operations
- Backpressure handling strategies
- Demonstration of the Observer pattern with functional extensions

## Package Structure

```
com.reactive.functional   - Core FRP implementations
    ├── BufferedPublisher.java       - Publisher with buffer-based backpressure
    ├── EnvironmentalReport.java     - Data model for environmental reports
    ├── FRPDemo.java                 - Main demo application
    ├── FunctionalPublisher.java     - Core publisher with functional operations
    ├── FunctionalWeatherStation.java - Observer pattern with functional callbacks
    ├── Humidity.java                - Data model for humidity readings
    ├── ImmutableMessage.java        - Immutable message model
    ├── SafePublisher.java           - Publisher with weak reference handling
    ├── SensorData.java              - Combined sensor data model
    ├── Temperature.java             - Data model for temperature readings
    ├── TemperatureProcessor.java    - Data processing example
    └── ThrottlingPublisher.java     - Publisher with rate limiting
com.reactive.iterator     - Iterator pattern implementations
    ├── IteratorPatternDemo.java     - Iterator pattern demo
    └── TemperatureCollection.java   - Custom iterable collection
com.reactive.observer     - Observer pattern implementations
    ├── naive              - Basic observer implementations
    └── pattern            - Advanced observer pattern examples
```

## Key Components

### Core Interfaces

- **Publisher<T>**: Source that emits elements to subscribers
- **Subscriber<T>**: Consumer that receives elements from a publisher
- **Subscription**: Connection between publisher and subscriber for flow control

### Publisher Implementations

- **FunctionalPublisher<T>**: Enhances Publisher with functional operators (map, filter)
- **BufferedPublisher<T>**: Handles backpressure through buffering
- **ThrottlingPublisher<T>**: Enforces minimum interval between emissions
- **SafePublisher<T>**: Prevents memory leaks with weak references

### Functional Observer Example

- **FunctionalWeatherStation**: Uses functional interfaces for temperature observation

## Usage Examples

### Transforming Data Streams

```java
// Create a source publisher
FunctionalPublisher<Integer> source = new FunctionalPublisher<>();

// Create a transformation pipeline
FunctionalPublisher<String> pipeline = source
    .map(n -> n * 2)                  // Double each number
    .filter(n -> n > 10)              // Keep only values > 10
    .map(n -> "Value: " + n);         // Convert to strings

// Subscribe to the result
pipeline.subscribe(new Subscriber<String>() {...});

// Emit values to the source
source.emit(5);
source.emit(8);
source.emit(2);
source.complete();
```

### Weather Station Observer

```java
// Create a weather station
FunctionalWeatherStation station = new FunctionalWeatherStation();

// Add temperature observer using lambda
station.addTemperatureObserver(temp -> 
    System.out.println("Temperature: " + temp.getValue() + "°C"));

// Set temperature values
station.setTemperature(25.5);
station.setTemperature(26.8);
```

### Backpressure Handling

```java
// Create a buffered publisher
BufferedPublisher<String> publisher = new BufferedPublisher<>(10);

// Create slow consumer
Subscriber<String> slowConsumer = new Subscriber<String>() {...};
publisher.subscribe(slowConsumer);

// Publish faster than consumption rate
for (int i = 0; i < 100; i++) {
    publisher.publish("Item " + i);
}
```

## Running the Demo

The `FRPDemo` class contains a main method with demonstrations of the key features:

1. **Functional Publisher Demo**: Shows transformation and filtering
2. **Weather Station Demo**: Demonstrates the observer pattern with lambdas
3. **Buffered Publisher Demo**: Shows backpressure handling
4. **Throttling Publisher Demo**: Demonstrates rate limiting

To run the demo:

```
javac com/reactive/functional/FRPDemo.java
java com.reactive.functional.FRPDemo
```

## Implementation Notes

- This is a minimal implementation for educational purposes
- No external dependencies are required
- The code uses Java's functional interfaces introduced in Java 8
- Proper error handling and resource management are demonstrated
- Thread safety is maintained where necessary using atomic variables

## Further Reading

- [Reactive Streams Specification](https://github.com/reactive-streams/reactive-streams-jvm)
- [Introduction to Reactive Programming](https://gist.github.com/staltz/868e7e9bc2a7b8c1f754)
- [Functional Programming in Java](https://www.oracle.com/technical-resources/articles/java/java8-lambdas.html)

## License

This project is available under the MIT License.