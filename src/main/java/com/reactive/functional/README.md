# Functional Reactive Programming Architecture Explained

This implementation demonstrates a comprehensive Functional Reactive Programming (FRP) architecture in Java. Let me explain the key components, their relationships, and how they work together.

## Architecture Overview

The code implements a minimalist reactive streams framework with these main components:

1. **Core Interfaces** - Define the reactive contract
2. **Publisher Implementations** - Different ways to emit data
3. **Functional Extensions** - Higher-order functions for composition
4. **Specialized Components** - Domain-specific implementations
5. **Demo Application** - Shows how everything works together

The architecture follows the Observer pattern but enhances it with functional programming concepts and backpressure handling.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Core Reactive Interfaces                   │
│  ┌──────────┐           ┌───────────┐          ┌─────────────┐  │
│  │ Publisher│◄──────────┤ Subscriber│◄─────────┤ Subscription│  │
│  └──────────┘           └───────────┘          └─────────────┘  │
└─────────────────────────────────────────────────────────────────┘
              ▲                   ▲                   ▲
              │                   │                   │
              │                   │                   │
              │                   │                   │
┌─────────────┴───────────────────┴───────────────────┴───────────┐
│                      Functional Extensions                      │
│  ┌────────────────┐  ┌─────────┐  ┌────────┐  ┌──────────────┐  │
│  │map(), filter() │  │Consumer │  │Function│  │Predicate     │  │
│  └────────────────┘  └─────────┘  └────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────────┘
              ▲                   ▲                   ▲
              │                   │                   │
              │                   │                   │
              │                   │                   │
┌─────────────┴───────────────────┴───────────────────┴───────────┐
│                   Publisher Implementations                     │
│  ┌───────────────────┐ ┌─────────────────┐ ┌───────────────────┐│
│  │FunctionalPublisher│ │BufferedPublisher│ │ThrottlingPublisher││
│  └───────────────────┘ └─────────────────┘ └───────────────────┘│
└─────────────────────────────────────────────────────────────────┘
              │                   │                   │
              │                   │                   │
              │                   │                   │
              ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Demo Applications                           │
└─────────────────────────────────────────────────────────────────┘
```

## Core Interfaces

The foundation consists of three interfaces from the Reactive Streams specification:

1. **Publisher\<T\>**: The source of data that can be subscribed to
2. **Subscriber\<T\>**: The consumer of data from a Publisher
3. **Subscription**: The connection between Publisher and Subscriber

These interfaces establish the reactive contract:
- Publishers emit data through Subscribers
- Subscribers request data through Subscriptions (backpressure)
- Data flows asynchronously with proper completion/error signaling

## Publisher Implementations

### 1. FunctionalPublisher\<T\>

This is the core implementation that brings functional programming to reactive streams:

- **Chaining transformations**: map() and filter() operators
- **Functional composition**: Creates new publishers when transformed
- **Event propagation**: emit(), complete(), and error() methods

```java
// Flow example: Transformation chain
Publisher<String> result = sourcePublisher
    .map(n -> n * 2)             // Transform data
    .filter(n -> n > 10)         // Filter data
    .map(n -> "Number: " + n);   // Transform again
```

### 2. BufferedPublisher\<T\>

Implements a buffering strategy for backpressure:

- **Queue-based buffer**: Stores items when consumers can't keep up
- **Size limitation**: Prevents unbounded memory growth
- **Demand tracking**: Uses AtomicLong to track requested items

### 3. ThrottlingPublisher\<T\>

Implements a time-based rate-limiting strategy:

- **Minimum interval enforcement**: Uses scheduling for time gaps
- **Delayed emission**: Items arriving too quickly are scheduled for later
- **Cancellation support**: Cleans up resources when no longer needed

## Specialized Components

### FunctionalWeatherStation

Demonstrates the observer pattern with functional interfaces:

- **Functional observers**: Uses Consumer\<Temperature\> for callbacks
- **Immutable data**: Temperature objects are immutable
- **Simple API**: addTemperatureObserver(), removeTemperatureObserver()

## How It All Works

### Data Flow Example

Let's trace an example from the `functionalPublisherDemo()`:

1. **Source emission**: `numberPublisher.emit(i)` is called with values 1-5
2. **Transformation**: Each value goes through map operation: `n -> n * n`
3. **Filtering**: Squared values filtered by `n % 2 == 0` (only 4, 16 pass)
4. **Subscription**: These values reach the subscriber's `onNext()` method
5. **Completion**: `numberPublisher.complete()` signals end of stream

### Backpressure Handling

The `BufferedPublisher` demonstrates backpressure:

1. **Limited requests**: Subscriber requests one item at a time
2. **Buffer filling**: Publisher emits faster than consumer processes
3. **Controlled consumption**: Consumer processes at its own pace
4. **Request signaling**: Consumer requests next item only when ready

### Rate Limiting

The `ThrottlingPublisher` shows rate limiting:

1. **Rapid publishing**: Attempts to publish every 200ms
2. **Rate enforcement**: Ensures emissions are at least 1 second apart
3. **Delayed delivery**: Uses scheduler to manage delayed emissions
4. **Timestamp recording**: Tracks when items were actually emitted

## Advanced Concepts Demonstrated

1. **Functional composition**: Building complex streams from simple operations
2. **Immutability**: Using immutable data for thread safety
3. **Backpressure**: Multiple strategies for handling fast producers/slow consumers
4. **Asynchronous processing**: Non-blocking processing with proper signaling

## Key Design Patterns

1. **Observer Pattern**: The foundation of reactive streams
2. **Builder Pattern**: Method chaining for constructing stream pipelines
3. **Strategy Pattern**: Different backpressure handling strategies
4. **Decorator Pattern**: Adding behavior to publishers via transformation

This implementation provides a lightweight but complete demonstration of Functional Reactive Programming principles, showing how they can be applied to create robust, composable, and responsive data processing pipelines.