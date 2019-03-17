import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.sqs.model.Message;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;




public class Apptier {
    public LocalDateTime lastCheckTime;
    public SQSmonitor sqs;
    public S3assistant s3;
    private static final String url = "http://206.207.50.7/getvideo";
    private static final String dir = "/Users/minghaowei/Desktop/546";
    private static final int BUFFER_SIZE = 4096;


    public Apptier(){
        lastCheckTime = LocalDateTime.now();
        sqs = new SQSmonitor();
        s3 = new S3assistant();
    }

    public static String downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
        String fileName = "";

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        return fileName;

    }

    public static void main(String args[]){
        Apptier app = new Apptier();


        while(true){
            Duration duration = Duration.between(app.lastCheckTime, LocalDateTime.now());
            if(duration.getSeconds() > 5 && !app.sqs.getRequestFlag()){
                System.out.println("System shut down");
                break;
            }
            try{
                Message msg = app.sqs.getRequest();
                if(msg==null){
                    continue;
                }
                System.out.println("Request receive----------------");
                String fname = downloadFile(url, dir);
                System.out.println("File " + fname +" downloaded--------");
                app.s3.upload(app.sqs.requestBody, fname);
                System.out.println("File uploaded to S3-----------------");
                app.sqs.sendResponse(app.sqs.requestBody);
                System.out.println("Response sent\n\n\n");
                app.lastCheckTime = LocalDateTime.now();
                TimeUnit.SECONDS.sleep(1);
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }
}
