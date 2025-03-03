package com.reactive.functional;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

// Safe publisher with weak references
class SafePublisher<T> implements Publisher<T> {
    private final List<WeakReference<Subscription>> subscriptions = new ArrayList<>();

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        SubscriptionImpl subscription = new SubscriptionImpl(subscriber);
        subscriptions.add(new java.lang.ref.WeakReference<>(subscription));
        subscriber.onSubscribe(subscription);
    }

    public void cleanup() {
        // Remove garbage-collected subscriptions
        subscriptions.removeIf(ref -> ref.get() == null);
    }

    private class SubscriptionImpl implements Subscription {
        private final Subscriber<? super T> subscriber;

        public SubscriptionImpl(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            // Implementation
        }

        @Override
        public void cancel() {
            // Implementation
        }
    }
}