import java.time.LocalDateTime;

public class AppTire {
    public LocalDateTime lastCheckTime;

    public AppTire(){
        lastCheckTime = LocalDateTime.now();
    }
}
