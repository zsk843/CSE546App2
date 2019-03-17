import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.File;



public class S3assistant {
    private final static AmazonS3 s3 = AmazonS3ClientBuilder
            .standard().withRegion(Regions.US_WEST_1)
            .build();
    private final static String bucket = "darknetbucket";

    //upload file
    public void upload(String key, File file){
        try {
            s3.putObject(bucket, key, file);
            System.out.println("File uploaded!");
        }catch (AmazonServiceException e){
            System.err.println(e.getErrorMessage());
        }
    }

    //upload content
    public void upload(String key, String content){
        try {
            s3.putObject(bucket, key, content);
            System.out.println("Content uploaded!");
        }catch (AmazonServiceException e){
            System.err.println(e.getErrorMessage());
        }
    }

    public void delete(String key){
        try{
            s3.deleteObject(bucket, key);
            System.out.println(key + " Deleted!");
        }catch(AmazonServiceException e){
            System.err.println(e.getErrorMessage());
        }
    }


    public static void main(String args[]){
        S3assistant obj = new S3assistant();
        obj.upload("firstkey", "HelloWorld");
        try{
            File file = new File("helloworld");
            System.out.println(file.getAbsolutePath());
            obj.upload("firstkey", file);
        }catch (Exception e){
            e.printStackTrace();
        }
        //obj.upload("firstkey", "");
        //obj.upload("thirdkey", file);
        //System.out.println(file.getAbsolutePath());
        //obj.delete("firstkey");
        //obj.delete("secondkey");

    }
}
