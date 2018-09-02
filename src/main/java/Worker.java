import com.rabbitmq.client.*;

import java.io.IOException;

public class Worker {

  private static final String TASK_QUEUE_NAME = "task_queue";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();
    final Channel channel = connection.createChannel();

    channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
    System.out.println("worker01  [*] Waiting for messages. To exit press CTRL+C");

    //一次做多发送5个任务过来，5个任务未处理完不能继续发送
    channel.basicQos(5);

    final Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");

        System.out.println("worker01   [x] Received '" + message + "'");
        try {
          doWork(message);
        } finally {
          System.out.println("worker01   [x] Done");
          //手动自动应答
          channel.basicAck(envelope.getDeliveryTag(), false);
        }
      }
    };
    //第二个参数false标识关闭自动应答
    channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
  }

  private static void doWork(String task) {
    for (char ch : task.toCharArray()) {
      if (ch == '.') {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException _ignored) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}

