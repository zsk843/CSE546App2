import com.amazonaws.services.sqs.model.Message;

import java.util.List;

public class SQSmonitor {
    private final static String REQUEST_QUEUE = "https://sqs.us-west-1.amazonaws.com/738913945831/request-queue";
    private final static String RESPONSE_QUEUE = "https://sqs.us-west-1.amazonaws.com/738913945831/response-queue";
    public String receiptHandle;
    public String requestBody;
    private boolean requestFlag;

    private SQSassistant sqs;


    public SQSmonitor(){
        this.sqs = new SQSassistant();
        requestFlag = false;
        receiptHandle = "";
        requestBody = "";
    }

    public void sendResponse(String message){
        if(requestFlag) {
            sqs.sendMessage(RESPONSE_QUEUE, message);
            requestFlag = false;
        }else{
            System.out.println("No Request To Return");
        }
    }

    public boolean getRequestFlag(){
        return requestFlag;
    }

    public Message getRequest(){
        if(!requestFlag) {
            List<Message> msglist = sqs.getMessage(REQUEST_QUEUE, 1);
            if (msglist.size() > 0) {
                requestFlag = true;
                Message msg = msglist.get(0);
                //System.out.println("Request received----------------------");
                //System.out.println(msg.getBody().toString());
                //System.out.println("--------------------------------------");
                requestBody = msg.getBody().toString();
                receiptHandle = msg.getReceiptHandle();
                sqs.deleteMessage(REQUEST_QUEUE, receiptHandle);
                return msg;
            }else{
                return null;
            }
        }else{
            System.out.println("Request Handling");
            return null;
        }
    }




}
