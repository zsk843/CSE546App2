import java.util.concurrent.TimeUnit;

public class DoubleAppTier {
    public static void main(String[] args) throws InterruptedException {
        Apptier apptier = new Apptier(args.length == 0);
        Thread t1 = new Thread(apptier);
        Thread t2 = new Thread(apptier);
        t1.start();
        TimeUnit.SECONDS.sleep(1);
        t2.start();
    }
}
