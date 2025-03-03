package com.reactive.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Buffered publisher implementation
class BufferedPublisher<T> implements Publisher<T> {
    private final Queue<T> buffer = new ConcurrentLinkedQueue<>();
    private final int maxBufferSize;
    private final List<SubscriptionImpl> subscriptions = new ArrayList<>();

    public BufferedPublisher(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        SubscriptionImpl subscription = new SubscriptionImpl(subscriber);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void publish(T item) {
        if (buffer.size() >= maxBufferSize) {
            // Buffer full - apply strategy (drop, block, etc.)
            return;
        }

        buffer.add(item);

        // Notify all subscriptions of new item
        for (SubscriptionImpl subscription : subscriptions) {
            subscription.tryDispatch();
        }
    }

    private class SubscriptionImpl implements Subscription {
        private final Subscriber<? super T> subscriber;
        private final AtomicLong requested = new AtomicLong(0);
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

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
            tryDispatch();
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            subscriptions.remove(this);
        }

        public void tryDispatch() {
            // Check if cancelled
            if (cancelled.get()) {
                return;
            }

            // Dispatch as many items as requested
            while (requested.get() > 0 && !buffer.isEmpty()) {
                T item = buffer.poll();
                if (item != null) {
                    subscriber.onNext(item);
                    requested.decrementAndGet();
                }
            }
        }
    }
}