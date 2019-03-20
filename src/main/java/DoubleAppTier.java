public class DoubleAppTier {
    public static void main(String[] args){
        Apptier apptier = new Apptier(args.length == 0);
        Thread t1 = new Thread(apptier);
        Thread t2 = new Thread(apptier);
        t1.start();
        t2.start();
    }
}
