package com.reactive.functional;

// Temperature processor example
class TemperatureProcessor {
    // Convert Celsius to Fahrenheit and filter values above a threshold
    public Publisher<Double> processTemperatures(Publisher<Double> source, double threshold) {
        FunctionalPublisher<Double> result = new FunctionalPublisher<>();

        source.subscribe(new Subscriber<Double>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Double celsius) {
                double fahrenheit = celsius * 9/5 + 32;
                if (fahrenheit > threshold) {
                    result.emit(fahrenheit);
                }
            }

            @Override
            public void onError(Throwable t) {
                result.error(t);
            }

            @Override
            public void onComplete() {
                result.complete();
            }
        });

        return result;
    }
}

