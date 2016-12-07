import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

/**
 * Created by apyreev on 07-Dec-16.
 */
class Training_2 {

    static ReentrantLock lock = new ReentrantLock();
    static private Integer count = 0;

    static private void incrementLock(){
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    synchronized static void incrementSync() {
        count = count + 1;
    }

    static private void increment(){
            count++;
    }

    static void synchronizeOne() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Runnable task = () -> incrementLock();
        IntStream.range(0, 1000000)
                .forEach(i -> executor.submit(task));
        Utils.stop(executor);
        System.out.println(count);
    }

    static void lockMethods() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            lock.lock();
            try {
                Utils.sleep(1);
            } finally {
                lock.unlock();
            }
        });

        executor.submit(() -> {
            System.out.println("Locked: " + lock.isLocked());
            System.out.println("Held by me: " + lock.isHeldByCurrentThread());
            boolean locked = lock.tryLock();
            System.out.println("Lock acquired: " + locked);
        });
    }

    static void readWriteLock() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ReadWriteLock lock = new ReentrantReadWriteLock();
        Map<String, String> map = new HashMap<>();

        Runnable writeTask = () -> {
            lock.writeLock();
            try {
                Utils.sleep(1);
                map.put("foo", "bar");
            } finally {
                lock.writeLock().unlock();
            }
        };

        Runnable readTask = () -> {
            lock.readLock();
            try {
                System.out.println(map.get("foo"));
                Utils.sleep(1);
            } finally {
                lock.readLock().unlock();
            }
        };

        executor.submit(writeTask);
        executor.submit(readTask);
        executor.submit(readTask);

        Utils.stop(executor);
    }

    static void stampedLock() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();
        Map<String, String> map = new HashMap<>();

        Runnable writeTask = () -> {
            long stamp = lock.writeLock();
            try {
                Utils.sleep(1);
                map.put("foo", "bar");
            } finally {
                lock.unlockWrite(stamp);
            }
        };

        Runnable readTask = () -> {
            long stamp = lock.readLock();
            try {
                System.out.println(map.get("foo"));
                Utils.sleep(1);
            } finally {
                lock.unlockRead(stamp);
            }
        };
        executor.submit(writeTask);
        executor.submit(readTask);
        executor.submit(readTask);

        Utils.stop(executor);
    }

    static void optimisticLock() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.tryOptimisticRead();
            try {
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                Utils.sleep(1);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                Utils.sleep(2);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
            } finally {
                lock.unlockRead(stamp);
            }
        });

        executor.submit(() -> {
            long stamp = lock.writeLock();
            try {
                System.out.println("Write Lock acquired");
                Utils.sleep(2);
            } finally {
                lock.unlockWrite(stamp);
                System.out.println("Write done");
            }
        });

        Utils.stop(executor);
    }

    static void convertLock() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.readLock();
            try {
                if(count == 0) {
                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0L) {
                        System.out.println("Could not convert to write lock");
                        stamp = lock.writeLock();
                    }
                    count = 23;
                }
                System.out.println(count);
            } finally {
                lock.unlock(stamp);
            }
        });

        Utils.stop(executor);
    }

    static void semaphore() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Semaphore semaphore = new Semaphore(5);

        Runnable longRunningTask = () -> {
            boolean permit = false;
            try {
                permit = semaphore.tryAcquire(1, TimeUnit.SECONDS);
                if(permit) {
                    System.out.println("Semaphore acquired");
                    Utils.sleep(5);
                } else {
                    System.out.println("Could not acquire semaphore");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (permit) {
                    semaphore.release();
                }
            }
        };

        IntStream.range(0, 10)
                .forEach(i -> executor.submit(longRunningTask));

        Utils.stop(executor);
    }
}
