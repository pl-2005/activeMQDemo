package topic;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class TopicSubscriberA {

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
            MessageConsumer consumer = session.createConsumer(topic);

            log.info("订阅者A已启动，等待 Topic 消息...");

            while (true) {
                Message message = consumer.receive(5000);

                if (message == null) {
                    log.info("订阅者A：5秒内无新消息，结束监听。");
                    break;
                }

                if (message instanceof TextMessage textMessage) {
                    log.info("订阅者A收到 Topic 消息: {}", textMessage.getText());
                } else {
                    log.info("订阅者A收到非文本消息: {}", message);
                }
            }

        } catch (JMSException e) {
            log.error("连接失败", e);
        }
    }
}
