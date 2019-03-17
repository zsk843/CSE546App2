import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScaleDownTest {

    public LocalDateTime dateTime = null;

    public List<Message> receiveMsg() {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrl = sqs.getQueueUrl("bbQueue").getQueueUrl();

        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
        System.out.println(messages.size());
        for (Message m : messages) {
            System.out.println(m.getBody());
            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
        }

        return messages;
    }


    public static void main(String[] args){

        ScaleDownTest sample = new ScaleDownTest();
        sample.dateTime = LocalDateTime.now();

        while(true) {
            List<Message> res = sample.receiveMsg();
            if (res.size() >0) {
                try {
                    Log.Log("Running App");
                    TimeUnit.SECONDS.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sample.dateTime = LocalDateTime.now();
            }
            else{

                long l = Duration.between( sample.dateTime,LocalDateTime.now()).getSeconds();
                Log.Log(Long.toString(l));
                if( l> 3){
                    System.out.println("=== Shutting down! ");
                    try {
                        Runtime.getRuntime().exec("sudo shutdown -h now");
                        System.exit(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }



        }

    }
}
