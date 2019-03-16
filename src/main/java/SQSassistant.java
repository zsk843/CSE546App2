import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.sqs.model.Message;

import java.util.List;

public class SQSassistant {
    private AmazonSQS sqs;

    public SQSassistant(){
        this.sqs = SQSclient.getInstance().getSQS(Regions.US_WEST_1);
    }

    //delete a specific message
    public void deleteMessage(String sqsurl, String message){
        sqs.deleteMessage(sqsurl, message);
    }

    //send one message
    public void sendMessage(String sqsurl, String message){
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(sqsurl)
                .withMessageBody(message);
        sqs.sendMessage(request);
    }

    //receive one message
    public List<Message> getMessage(String sqsurl, int numberOfMessage){
        ReceiveMessageRequest messageRequest = new ReceiveMessageRequest()
                .withQueueUrl(sqsurl)
                .withMaxNumberOfMessages(numberOfMessage);
        return sqs.receiveMessage(messageRequest).getMessages();
    }

    public static void main(String args[]){
        SQSassistant obj = new SQSassistant();
        obj.sendMessage("https://us-west-1.queue.amazonaws.com/738913945831/request-queue", "New Message For Test!!!!");
        List<Message> mylist = obj.getMessage("https://us-west-1.queue.amazonaws.com/738913945831/request-queue", 1);
        System.out.println(mylist.get(0).getBody().toString());
        obj.deleteMessage("https://us-west-1.queue.amazonaws.com/738913945831/request-queue",mylist.get(0).getReceiptHandle());
    }
}
