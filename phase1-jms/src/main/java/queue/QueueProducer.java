package queue;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class QueueProducer {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String QUEUE_NAME = "demo.queue";

    public static void main(String[] args) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            for (int i = 1; i <= 10; i++) {
                String text = "Hello ActiveMQ Queue, message-" + i;
                TextMessage message = session.createTextMessage(text);
                producer.send(message);
                log.info("生产者发送队列消息: {}", text);
            }

            log.info("发送完毕");

        } catch (JMSException e) {
            log.error("发送队列消息失败", e);
        }
    }
}
