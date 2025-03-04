import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class Message {
    private String userId;
    private String content;

    public Message(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}

class ChatService {
    public Flux<Tuple2<String, Long>> getMessageCountByUser(Flux<Message> messageFlux) {
        return messageFlux
                .groupBy(Message::getUserId)
                .flatMap(group -> group.count()
                        .map(count -> Tuples.of(group.key(), count))
                );
    }
}

class ChatServiceTest {
    @Test
    void testGetMessageCountByUser() {
        Flux<Message> flux = Flux.just(
                new Message("user1", "Hello"),
                new Message("user1", "How are you?"),
                new Message("user2", "Hi"),
                new Message("user2", "Good morning"),
                new Message("user2", "What's up?")
        );

        ChatService service = new ChatService();
        Flux<Tuple2<String, Long>> result = service.getMessageCountByUser(flux);

        StepVerifier.create(result)
                .expectNext(Tuples.of("user1", 2L))
                .expectNext(Tuples.of("user2", 3L))
                .expectComplete()
                .verify();
    }
}