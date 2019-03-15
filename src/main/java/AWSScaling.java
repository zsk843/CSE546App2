import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;


public class AWSScaling{
    private static final String QUEUE_NAME = "bbQueue";
    private static final int MAX_NUM_INSTANCES = 5;

    private static final String IMAGE_ID = "ami-0b30c5f01b371639f";
    private AmazonEC2 ec2;

    public AWSScaling(){

        ec2 = AmazonEC2ClientBuilder.defaultClient();
    }

    public void scaleApplication() {

        int queueSize = getSQSLength();
        int requiredNum = Math.min(queueSize, MAX_NUM_INSTANCES);

        List<Instance> instances = this.getAllInstances();
        int runningNum = 0;
        LinkedList<Instance> stoppedInstance = new LinkedList<Instance>();
        for(Instance i: instances){
            String stateName = i.getState().getName().toLowerCase();
            if(stateName.equals( "running"))
                runningNum ++;
            else if(stateName.equals("stopping")|| stateName.equals("stopped"))
                stoppedInstance.add(i);
        }

        if(runningNum < requiredNum){
            int addNum = requiredNum - runningNum;
            while(stoppedInstance.size()>0) {
                Instance i = stoppedInstance.get(0);
                stoppedInstance.removeFirst();
                if(i.getState().getName().toLowerCase().equals("stopping")) {
                    String status = "stopping";
                    do {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        DescribeInstancesRequest desRequest = new DescribeInstancesRequest().withInstanceIds(i.getInstanceId());
                        DescribeInstancesResult res = ec2.describeInstances(desRequest);

                        status = res.getReservations().get(0).getInstances().get(0).getState().getName().toLowerCase();
                    }while (status.equals("stopping"));
                }

                StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(i.getInstanceId());
                ec2.startInstances(request);
                Log.Log("Restarting Instance");
                addNum--;
                if(addNum <= 0)
                    break;
            }
            while(addNum > 0){
                RunInstancesRequest rir = new RunInstancesRequest(IMAGE_ID,
                        1, 1);
                rir.setInstanceType("t2.micro");
                ec2.runInstances(rir);

                addNum -- ;
            }
        }

    }

//    public void createQueue() {
//        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//        try {
//            CreateQueueRequest createQueueRequest =
//                    new CreateQueueRequest(QUEUE_NAME);
//            String myQueueUrl = sqs.createQueue(createQueueRequest)
//                    .getQueueUrl();
//            System.out.println(myQueueUrl);
//        } catch (AmazonSQSException e) {
//            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
//                throw e;
//            }
//        }
//    }

//    public void sendMsg() {
//        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
//        SendMessageRequest send_msg_request = new SendMessageRequest()
//                .withQueueUrl(queueUrl)
//                .withMessageBody("hello world");
//        sqs.sendMessage(send_msg_request);
//        System.out.println("send a msg");
//    }

//    public void receiveMsg() {
//        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
//        String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
//
//        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
//        for (Message m : messages) {
//            System.out.println(m.getBody());
//            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
//        }
//
//        System.out.println("deleted msgs");
//    }

    public int getSQSLength(){
        final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        GetQueueAttributesRequest request = new GetQueueAttributesRequest().withAttributeNames("ApproximateNumberOfMessages").withQueueUrl(QUEUE_NAME);
        GetQueueAttributesResult res = sqs.getQueueAttributes(request);
        java.util.Map<java.lang.String, java.lang.String> res_s = res.getAttributes();
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
            for(Instance i:instances){
                System.out.println(i.getTags());
            }

        }

        return instances;


    }

    public static void main(String[] args)
    {
        AWSScaling sqsExample = new AWSScaling();
        sqsExample.scaleApplication();


       

    }
}

