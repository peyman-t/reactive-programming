import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.mockito.Mockito.when;

class Tuple2<T1, T2> {
    private final T1 t1;
    private final T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T1 getT1() {
        return t1;
    }

    public T2 getT2() {
        return t2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(t1, tuple2.t1) &&
                Objects.equals(t2, tuple2.t2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }

    @Override
    public String toString() {
        return "Tuple2{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                '}';
    }
}

class Tuples {
    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<>(t1, t2);
    }
}

@ExtendWith(MockitoExtension.class)
class WeatherServiceUnitTest {
    @Mock
    private WeatherRepository repository;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void testGetAverageTemperature() {
        // Setup test data
        WeatherReading r1 = new WeatherReading("NYC", 22.5, 60.0, System.currentTimeMillis());
        WeatherReading r2 = new WeatherReading("NYC", 23.5, 65.0, System.currentTimeMillis());
        WeatherReading r3 = new WeatherReading("NYC", 21.5, 62.0, System.currentTimeMillis());

        // Mock repository behavior
        when(repository.findByLocation("NYC"))
                .thenReturn(Flux.just(r1, r2, r3));

        // Test and verify
        StepVerifier.create(weatherService.getAverageTemperature("NYC"))
                .expectNext(22.5) // (22.5 + 23.5 + 21.5) / 3 = 22.5
                .verifyComplete();
    }
}

interface WeatherRepository {
    Flux<WeatherReading> findByLocation(String location);
    Mono<WeatherReading> save(WeatherReading reading);
}

class WeatherReading {
    private final String location;
    private final double temperature;
    private final double humidity;
    private final long timestamp;

    public WeatherReading(String location, double temperature, double humidity, long timestamp) {
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

class WeatherService {
    private final WeatherRepository repository;

    WeatherService(WeatherRepository repository) {
        this.repository = repository;
    }

    // Get average temperature for a location
    Mono<Double> getAverageTemperature(String location) {
        return repository.findByLocation(location)
                .map(WeatherReading::getTemperature)
                .collectList()
                .map(temperatures -> {
                    if (temperatures.isEmpty()) {
                        return 0.0;
                    }
                    return temperatures.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                });
    }
}