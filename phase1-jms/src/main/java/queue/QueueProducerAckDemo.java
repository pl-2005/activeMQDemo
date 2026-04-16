package queue;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class QueueProducerAckDemo {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String QUEUE_NAME = "demo.queue.ack";

    public static void main(String[] args) {
        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);
        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            String[] payloads = {
                    "OK-1",
                    "OK-2",
                    "FAIL_ME",
                    "OK-3",
                    "OK-4"
            };

            for (String body : payloads) {
                TextMessage message = session.createTextMessage(body);
                producer.send(message);
                log.info("Ack_Demo 生产者发送消息: {}", body);
            }
            log.info("Ack_Demo 消息发送完成。");
        } catch (JMSException e) {
            log.error("Ack_Demo 生产者发送消息失败", e);
        }
    }
}
