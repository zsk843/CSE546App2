import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.sqs.model.Message;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import java.io.FileOutputStream;
import java.io.BufferedReader;


public class Apptier {
    public LocalDateTime lastCheckTime;
    public SQSmonitor sqs;
    public S3assistant s3;
    private static final String url = "http://206.207.50.7/getvideo";
    private static final String dir = "/home/ubuntu/darknet";
    private static final String weight_path = "/home/ubuntu/darknet/tiny.weights";
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

        Process process = null;
        try {
            process = Runtime.getRuntime().exec("Xvfb :1 & export DISPLAY=:1");
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

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
                String currentLine = "";
                BufferedReader bufrIn= new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                BufferedReader bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                StringBuilder result = new StringBuilder();
                try {
                    String command = "/home/ubuntu/darknet/darknet detector demo cfg/coco.data cfg/yolov3-tiny.cfg " + weight_path +" "
                            + dir + "/" + fname + "  -dont_show > result";
                    process = Runtime.getRuntime().exec(command);
                    process.waitFor();


                    // 读取输出
                    String line = null;
                    while ((line = bufrIn.readLine()) != null) {
                        result.append(line).append('\n');
                    }
                    while ((line = bufrError.readLine()) != null) {
                        result.append(line).append('\n');
                    }
                    System.out.println(result.toString());
                    process = Runtime.getRuntime().exec("python " + dir + "/darknet.py");
                    process.waitFor();
                    BufferedReader reader = new BufferedReader(new FileReader(dir + "/result_label"));
                    currentLine = reader.readLine();
                    reader.close();
                    System.out.println(currentLine);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }


                app.sqs.sendResponse(app.sqs.requestBody+","+fname+","+currentLine);
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
