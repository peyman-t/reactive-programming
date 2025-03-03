package com.reactive.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Publisher interface from Reactive Streams
interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}

// Subscriber interface from Reactive Streams
interface Subscriber<T> {
    void onSubscribe(Subscription subscription);
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}

// Subscription interface from Reactive Streams
interface Subscription {
    void request(long n);
    void cancel();
}

// A simple Publisher implementation with functional capabilities
public class FunctionalPublisher<T> implements Publisher<T> {
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        subscribers.add(subscriber);
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                // Implementation of backpressure
            }

            @Override
            public void cancel() {
                subscribers.remove(subscriber);
            }
        });
    }

    // Functional method to transform the publisher
    public <R> FunctionalPublisher<R> map(Function<T, R> mapper) {
        FunctionalPublisher<R> result = new FunctionalPublisher<>();
        this.subscribe(new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                for (Subscriber<? super R> s : result.subscribers) {
                    s.onNext(mapper.apply(item));
                }
            }

            @Override
            public void onError(Throwable t) {
                for (Subscriber<? super R> s : result.subscribers) {
                    s.onError(t);
                }
            }

            @Override
            public void onComplete() {
                for (Subscriber<? super R> s : result.subscribers) {
                    s.onComplete();
                }
            }
        });
        return result;
    }

    // Method to filter items
    public FunctionalPublisher<T> filter(java.util.function.Predicate<T> predicate) {
        FunctionalPublisher<T> result = new FunctionalPublisher<>();
        this.subscribe(new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                if (predicate.test(item)) {
                    for (Subscriber<? super T> s : result.subscribers) {
                        s.onNext(item);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                for (Subscriber<? super T> s : result.subscribers) {
                    s.onError(t);
                }
            }

            @Override
            public void onComplete() {
                for (Subscriber<? super T> s : result.subscribers) {
                    s.onComplete();
                }
            }
        });
        return result;
    }

    // Method to emit a value to all subscribers
    public void emit(T value) {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onNext(value);
        }
    }

    // Method to signal completion to all subscribers
    public void complete() {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onComplete();
        }
    }

    // Method to signal error to all subscribers
    public void error(Throwable throwable) {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onError(throwable);
        }
    }
}















