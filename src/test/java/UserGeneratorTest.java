import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class User {
    String name;
    int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', age=" + age + '}';
    }
}

class UserGenerator {
    public Flux<User> generateUsers(int numberOfUsers, int minAge, int maxAge) {
        if (numberOfUsers < 1 || minAge < 0 || maxAge < minAge) {
            return Flux.error(
                    new IllegalArgumentException("invalid arguments passed"));
        }
        return Flux.range(1, numberOfUsers)
                .delayElements(Duration.ofSeconds(1))
                .map(i -> new User("User" + i, randomAge(minAge, maxAge)))
                .sort(Comparator.comparingInt(User::getAge));
    }

    private int randomAge(int minAge, int maxAge) {
        Random rand = new Random();
        return rand.nextInt((maxAge - minAge) + 1) + minAge;
    }
}

class UserGeneratorTest {
    @Test
    void testUserGeneratorWithVirtualTime() {
        int numberOfUsers = 100; // Large number of users
        int minAge = 18;
        int maxAge = 65;

        UserGenerator generator = new UserGenerator();

        StepVerifier.withVirtualTime(() -> generator.generateUsers(numberOfUsers, minAge, maxAge))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(numberOfUsers)) // Advance virtual time
                .recordWith(ArrayList::new)
                .expectNextCount(numberOfUsers)
                .consumeRecordedWith(users -> {
                    // Verify all ages are within range
                    for (User user : users) {
                        assertTrue(user.getAge() >= minAge && user.getAge() <= maxAge,
                                "User age should be within specified range");
                    }

                    // Verify users are sorted by age
                    List<User> userList = new ArrayList<>(users);
                    List<User> sortedList = new ArrayList<>(users);
                    sortedList.sort(Comparator.comparingInt(User::getAge));

                    assertEquals(sortedList, userList, "Users should be sorted by age");
                })
                .verifyComplete();
    }

    @Test
    void testUserGeneratorWithInvalidParameters() {
        UserGenerator generator = new UserGenerator();

        // Test with invalid number of users
        StepVerifier.create(generator.generateUsers(0, 18, 65))
                .expectError(IllegalArgumentException.class)
                .verify();

        // Test with invalid age range
        StepVerifier.create(generator.generateUsers(10, 30, 20))
                .expectError(IllegalArgumentException.class)
                .verify();

        // Test with negative minimum age
        StepVerifier.create(generator.generateUsers(10, -5, 20))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}