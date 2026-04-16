package queue;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class QueueConsumerAutoAck {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    // 任务 C 专用队列，避免影响任务 A/B
    private static final String QUEUE_NAME = "demo.queue.ack";

    public static void main(String[] args) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(queue);

            log.info("Auto_Ack 消费者已启动，监听队列: {}", QUEUE_NAME);

            while (true) {
                Message message = consumer.receive(5000);
                if (message == null) {
                    log.info("Auto_Ack：5秒内无新消息，结束监听。");
                    break;
                }
                if (message instanceof TextMessage textMessage) {
                    String body = textMessage.getText();
                    log.info("Auto_Ack 收到消息: {}", body);
                } else {
                    log.warn("Auto_Ack 收到非文本消息: {}", message);
                }
            }

            log.info("Auto_Ack 消费者已关闭。");
        } catch (JMSException e) {
            log.error("Auto_Ack 消费失败", e);
        }

    }
}
