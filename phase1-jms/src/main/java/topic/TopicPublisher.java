package topic;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class TopicPublisher {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String TOPIC_NAME = "demo.topic";

    public static void main(String[] args) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);
            MessageProducer producer = session.createProducer(topic);

            for (int i = 0; i < 10; i++) {
                String text = "Hello ActiveMQ Topic, message-" + i;
                TextMessage message = session.createTextMessage(text);
                producer.send(message);
                log.info("发布者发送 Topic 消息: {}", text);
            }

            log.info("Topic 发布完毕。");
        } catch (JMSException e) {
            log.error("连接失败", e);
        }
    }
}
