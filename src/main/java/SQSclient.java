import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.regions.Regions;

public class SQSclient {
    private static volatile SQSclient client = null;

    //default constructor
    private SQSclient(){
    }

    public static SQSclient getInstance(){
        if(client==null) {
            synchronized (SQSclient.class) {
                client = new SQSclient();
            }
        }
        return client;

    }

    public AmazonSQS getSQS(Regions region){
        return AmazonSQSClientBuilder.standard().withRegion(region).build();
    }


}
