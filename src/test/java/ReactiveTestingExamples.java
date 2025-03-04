import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ReactiveTestingExamples {

    @Test
    void simpleMonoVerifier() {
        Mono<String> mono = Mono.just("dog");

        StepVerifier.create(mono)
                .expectNext("dog")
                .expectComplete()
                .verify();
    }

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

    @Test
    void verifyFluxByRecordingValues() {
        Flux<Integer> numbersFlux = Flux.range(1, 10);

        StepVerifier.create(numbersFlux)
                .recordWith(ArrayList::new) // start collecting elements into list
                .expectNextCount(10) // check whether stream has at least 10 elements
                .consumeRecordedWith(values -> {
                    assertTrue(values.stream().allMatch(value -> value < 20)); // check whether every value match condition
                })
                .expectComplete()
                .verify();
    }

    @Test
    void verifyErrorHandling() {
        // create mono with just an error inside
        Mono<String> errorMono = Mono.error(new IllegalArgumentException("Invalid data"));

        StepVerifier.create(errorMono)
                .expectError(IllegalArgumentException.class)
                .verify();


        // create flux and append an error at the end
        Flux<String> errorFlux = Flux.just("A", "B").concatWith(Flux.error(new IllegalArgumentException("Not a letter")));

        StepVerifier.create(errorFlux)
                .expectNext("A", "B") // process first two elements
                .expectErrorMatches(throwable -> // takes Predicate as argument which maps element into Boolean
                        // check if error type and message are correct and return true/false
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Not a letter")
                )
                .verify();

        // same as above, using expectErrorSatisfies()
        StepVerifier.create(errorFlux)
                .expectNext("A", "B") // process first two elements
                .expectErrorSatisfies(throwable -> { // takes Consumer as argument
                    assertInstanceOf(IllegalArgumentException.class, throwable);
                    assertEquals("Not a letter", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void verifyFallbackBehavior() {
        // base flux with two letters and exception at the end
        Flux<String> letterFlux = Flux.just("A", "B").concatWith(Mono.error(new RuntimeException()));

        // fallback value is returned from stream on error
        Flux<String> fallbackValueFlux = letterFlux.onErrorReturn("C");

        StepVerifier.create(fallbackValueFlux)
                .expectNext("A", "B", "C")
                .expectComplete() // stream is considered complete because error was handled by the publisher
                .verify();

        // fallback stream is returned from stream on error
        Flux<String> fallbackStreamFlux = letterFlux.onErrorResume(throwable -> Flux.just("C", "D"));

        StepVerifier.create(fallbackStreamFlux)
                .expectNext("A", "B", "C", "D")
                .expectComplete()
                .verify();
    }

    @Test
    void verifyFluxWithDelayByWaiting() {
        List<Integer> data = List.of(1, 2, 3);
        Flux<Integer> delayedFlux = Flux.fromIterable(data).delayElements(Duration.ofMillis(100));

        StepVerifier.create(delayedFlux)
                .expectNext(1, 2, 3)
                .expectComplete()
                .verify(); // we don't need an artificial "wait" method, verify does it for us
    }

    @Test
    void verifyFluxWithDelayUsingVirtualTime() {
        List<Integer> data = List.of(1, 2, 3);
        StepVerifier.withVirtualTime(() -> Flux.fromIterable(data).delayElements(Duration.ofHours(1))) // duration between elements is 1 hour!
                .thenAwait(Duration.ofHours(3)) // simulate waiting for specified time (3h)
                .expectNext(1, 2, 3) // the elements should be ready after that time
                .verifyComplete(); // shortcut for expectComplete().verify()
    }

    @Test
    void verifyFluxWithDelayUsingVirtualTimeExpectNoEventForSomeTime() {
        List<Integer> data = List.of(1, 2, 3);
        StepVerifier.withVirtualTime(() -> Flux.fromIterable(data).delayElements(Duration.ofHours(1)))
                .expectSubscription() // subscription is also an event, so it needs to be checked before calling `expectNoEvent()`
                .expectNoEvent(Duration.ofHours(1)) // during first 1 hour no element is sent
                .expectNext(1) // this element will be emitted after exactly 1 hour
                .thenAwait(Duration.ofHours(2)) // wait for the rest of the data
                .expectNext(2, 3)
                .verifyComplete();
    }

    @Test
    void verifyInfiniteFluxWithDelay() {
        // simulate time to avoid waiting
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1)))
                .thenAwait(Duration.ofSeconds(3)) // wait for 3 elements to arrive
                .expectNext(0L, 1L, 2L)
                .thenCancel()
                .verify();
    }

    @Test
    void testWithImmediateScheduler() {
        try {
            // Replace all schedulers with immediate scheduler
            Schedulers.onScheduleHook("test", r -> () -> r.run());

            Flux<Integer> flux = Flux.range(1, 5)
                    .publishOn(Schedulers.parallel()) // This will actually use immediate scheduler
                    .map(i -> i * 10);

            StepVerifier.create(flux)
                    .expectNext(10, 20, 30, 40, 50)
                    .verifyComplete();
        } finally {
            // Restore the original hook
            Schedulers.resetOnScheduleHook("test");
        }
    }

    @Test
    void testDelayWithScheduler() {
        StepVerifier.withVirtualTime(() ->
                        Flux.range(1, 3)
                                .delayElements(Duration.ofSeconds(1), Schedulers.parallel())
                )
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(1)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(2)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(3)
                .verifyComplete();
    }

    @Test
    void testParallelProcessing() {
        Flux<Integer> parallelProcessed = Flux.range(1, 10)
                .parallel(4) // Split into 4 rails
                .runOn(Schedulers.parallel())
                .map(i -> i * 10)
                .sequential(); // Merge back to sequential

        StepVerifier.create(parallelProcessed.collectList())
                .consumeNextWith(list -> {
                    assertEquals(10, list.size());
                    assertTrue(list.containsAll(Arrays.asList(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)));
                })
                .verifyComplete();
    }

    class BinaryConverter {
        private final Flux<Integer> source;

        public BinaryConverter(Flux<Integer> source) {
            this.source = source;
        }

        public Flux<String> getBinaryRepresentation() {
            return source
                    .map(number -> Integer.toBinaryString(number));
        }
    }

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

    @Test
    void testErrorHandlingWithNonCompliantPublisher() {
        // Create a regular TestPublisher
        TestPublisher<String> errorPublisher = TestPublisher.create();

        // Component under test that handles nulls explicitly
        Flux<String> handledFlux = errorPublisher.flux()
                .map(value -> {
                    if (value == null) {
                        throw new NullPointerException("Null values not allowed");
                    }
                    return value;
                })
                .onErrorReturn("Error occurred");

        // Verify behavior when error occurs
        StepVerifier.create(handledFlux)
                .then(() -> errorPublisher.error(new NullPointerException("Null values not allowed")))
                .expectNext("Error occurred")
                .verifyComplete();
    }

    @Test
    void testEmptyStreamHandling() {
        // Create handler that returns default values for empty streams
        Function<Flux<String>, Flux<String>> emptyHandler = flux ->
                flux.switchIfEmpty(Flux.just("No data available"));

        // Test with empty Flux
        StepVerifier.create(emptyHandler.apply(Flux.empty()))
                .expectNext("No data available")
                .verifyComplete();
    }

    @Test
    void testConcurrentEmissions() {
        // Create two concurrent Fluxes
        Flux<String> flux1 = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        Flux<String> flux2 = Flux.just("X", "Y", "Z")
                .delayElements(Duration.ofMillis(100));

        // Merge them and verify the result has all elements
        // (order might not be predictable)
        StepVerifier.create(Flux.merge(flux1, flux2).collectList())
                .consumeNextWith(list -> {
                    assertEquals(6, list.size());
                    assertTrue(list.containsAll(Arrays.asList("A", "B", "C", "X", "Y", "Z")));
                })
                .verifyComplete();
    }
}


