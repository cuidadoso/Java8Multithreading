import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by apyreev on 07-Dec-16.
 */
public class Utils {

    static void stop(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("termination interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
                System.err.println("killed non-finished tasks");
            }
        }
    }

    static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
