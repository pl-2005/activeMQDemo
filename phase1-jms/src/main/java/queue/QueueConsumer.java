package queue;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class QueueConsumer {

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
            MessageConsumer consumer = session.createConsumer(queue);

            log.info("消费者已启动，开始接收队列消息...");

            while (true) {
                Message message = consumer.receive(3000);

                if (message == null) {
                    log.info("3 秒内无新消息，消费者结束。");
                    break;
                }

                if (message instanceof TextMessage textMessage) {
                    log.info("收到消息: {}", textMessage.getText());
                } else {
                    log.info("收到非文本消息: {}", message);
                }
            }

            log.info("消费者已关闭。");
        } catch (JMSException e) {
            log.error("消费队列消息失败", e);
        }
    }
}
