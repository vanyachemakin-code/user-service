package notification;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = NotificationServiceApplication.class)
public abstract class TestKafkaAndMail {

    protected static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    protected static final GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP);

    static {
        kafkaContainer.start();
        greenMail.setUser("test", "password");
        greenMail.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> String.valueOf(greenMail.getSmtp().getPort()));
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "password");
    }

    @BeforeEach
    void resetEmailServer() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @AfterAll
    static void tearDown() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }
}