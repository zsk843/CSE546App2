import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;

import java.util.*;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

public class AWSScaling{
    private static final String QUEUE_NAME = "bbQueue";
    private static final int MAX_NUM_INSTANCES = 5;

    private List<String> appList = new ArrayList<String>();
    private Map<String, Boolean> status = new HashMap<String, Boolean>();
    private static String IMAGE_ID = "ami-0b30c5f01b371639f";

    public void scaleApplication() {

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        int queueSize = getSQSLength();
        int requiredNum = Math.min(queueSize, MAX_NUM_INSTANCES);

        List<Instance> instances = this.getAllInstances();
        int runningNum = 0;
        List<Instance> stoppedInstance = new LinkedList<Instance>();
        for(Instance i: instances){
            String stateName = i.getState().getName().toLowerCase();
            if(stateName.equals( "running"))
                runningNum ++;
            else if(stateName.equals("stopping")|| stateName.equals("stopped"))
                instances.add(i);
        }



        System.out.println("create an instance");

          //image id of the instance
        int minInstanceCount = 1; //create 1 instance
        int maxInstanceCount = 1;

        RunInstancesRequest rir = new RunInstancesRequest(imageId,
                minInstanceCount, maxInstanceCount);
        rir.setInstanceType("t2.micro"); //set instance type

        RunInstancesResult result = ec2.runInstances(rir);

        List<Instance> resultInstance =
                result.getReservation().getInstances();

        for(Instance ins : resultInstance) {
            System.out.println("New instance has been created:" +
                    ins.getInstanceId());//print the instance ID
        }
    }



    public void createQueue() {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        try {
            CreateQueueRequest createQueueRequest =
                    new CreateQueueRequest(QUEUE_NAME);
            String myQueueUrl = sqs.createQueue(createQueueRequest)
                    .getQueueUrl();
            System.out.println(myQueueUrl);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw e;
            }
        }
    }

    public void sendMsg() {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody("hello world");
        sqs.sendMessage(send_msg_request);
        System.out.println("send a msg");
    }

    public void receiveMsg() {
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
        for (Message m : messages) {
            System.out.println(m.getBody());
            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
        }

        System.out.println("deleted msgs");
    }

    public int getSQSLength(){
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        GetQueueAttributesRequest request = new GetQueueAttributesRequest().withAttributeNames("ApproximateNumberOfMessages").withQueueUrl(QUEUE_NAME);
        GetQueueAttributesResult res = sqs.getQueueAttributes(request);
        Map<String,String> res_s = res.getAttributes();
        return Integer.parseInt(res_s.get("ApproximateNumberOfMessages"));

    }

    public List<Instance> getAllInstances(){

        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        List<Instance> instances = new ArrayList<Instance>();

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while (true) {

            DescribeInstancesResult response = ec2.describeInstances(request);
            for (Reservation rev: response.getReservations()) {
                instances.addAll(rev.getInstances());
            }

            request.setNextToken(response.getNextToken());
            if (response.getNextToken() == null)
                break;

        }

        return instances;


    }

    public static void main(String[] args)
    {
        AWSScaling sqsExample = new AWSScaling();
        //sqsExample.createQueue();
//       sqsExample.sendMsg();
//       sqsExample.sendMsg();
        sqsExample.getInstancesDes();


       

    }
}

