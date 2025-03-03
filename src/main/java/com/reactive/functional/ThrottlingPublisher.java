package com.reactive.functional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Throttling publisher implementation
class ThrottlingPublisher<T> implements Publisher<T> {
    private final Duration minInterval;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<SubscriptionImpl> subscriptions = new ArrayList<>();
    private volatile long lastEmitTime = 0;

    public ThrottlingPublisher(Duration minInterval) {
        this.minInterval = minInterval;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        SubscriptionImpl subscription = new SubscriptionImpl(subscriber);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void publish(T item) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastEmitTime;
        long delay = Math.max(0, minInterval.toMillis() - elapsed);

        if (delay > 0) {
            // Schedule emission after delay
            scheduler.schedule(() -> emitItem(item), delay, TimeUnit.MILLISECONDS);
        } else {
            // Emit immediately
            emitItem(item);
        }
    }

    private void emitItem(T item) {
        lastEmitTime = System.currentTimeMillis();

        for (SubscriptionImpl subscription : subscriptions) {
            if (subscription.isActive()) {
                subscription.emitItem(item);
            }
        }
    }

    private class SubscriptionImpl implements Subscription {
        private final Subscriber<? super T> subscriber;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicLong requested = new AtomicLong(0);

        public SubscriptionImpl(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                subscriber.onError(new IllegalArgumentException("Requested amount must be positive"));
                return;
            }

            requested.addAndGet(n);
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            subscriptions.remove(this);
        }

        public boolean isActive() {
            return !cancelled.get() && requested.get() > 0;
        }

        public void emitItem(T item) {
            if (requested.get() > 0) {
                subscriber.onNext(item);
                requested.decrementAndGet();
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}