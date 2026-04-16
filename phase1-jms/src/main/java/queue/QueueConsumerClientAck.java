package queue;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
public class QueueConsumerClientAck {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final String QUEUE_NAME = "demo.queue.ack";

    public static void main(String[] args) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);
        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(queue);

            log.info("Client_Ack 消费者已启动，监听队列: {}", QUEUE_NAME);
            while (true) {
                Message message = consumer.receive(5000);
                if (message == null) {
                    log.info("Client_Ack：5秒内无新消息，结束监听。");
                    break;
                }
                if (message instanceof TextMessage textMessage) {
                    String body = textMessage.getText();
                    log.info("Client_Ack 收到消息: {}", body);

                    /*if ("FAIL_ME".equals(body)) {
                        log.error("命中 FAIL_ME，模拟业务失败，不进行 ack。");
                        throw new RuntimeException("模拟消费失败");
                    }*/
                    // 先保留“正常路径”：处理成功后手动 ack
                    message.acknowledge();
                    log.info("Client_Ack 已确认消息: {}", body);
                } else {
                    log.warn("Client_Ack 收到非文本消息: {}", message);
                    message.acknowledge();
                }
            }

            log.info("Client_Ack 消费者已关闭。");
        } catch (JMSException e) {
            log.error("创建连接失败", e);
        }
    }
}
