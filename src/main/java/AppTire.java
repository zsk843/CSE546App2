import com.amazonaws.services.ec2.AmazonEC2;

import java.time.LocalDateTime;

public class AppTire {
    public LocalDateTime lastCheckTime;


    public AppTire(){
        lastCheckTime = LocalDateTime.now();
    }
}
