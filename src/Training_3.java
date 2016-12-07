import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongBinaryOperator;
import java.util.stream.IntStream;

/**
 * Created by apyreev on 07-Dec-16.
 */
public class Training_3 {

    static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    static void atomicInteger() {

        AtomicInteger atomicInt = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000000)
                .forEach(i -> {
                    Runnable task = () -> atomicInt.updateAndGet(n -> n + 2);
                    executor.submit(task);

                });

        Utils.stop(executor);

        System.out.println(atomicInt);
    }

    static void atomicInteger2() {
        AtomicInteger atomicInt = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> {
                    Runnable task = () -> atomicInt.accumulateAndGet(i, (n, m) -> n + m);
                    executor.submit(task);
                });

        Utils.stop(executor);

        System.out.println(atomicInt);
    }

    static void longAdder() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        LongAdder add = new LongAdder();

        IntStream.range(0, 1000)
                .forEach(i -> executor.submit(add::increment));

        Utils.stop(executor);
        System.out.println(add.sumThenReset());
    }

    static void longAccumulator() {
        LongBinaryOperator op = (x, y) -> x + 2 * y;
        LongAccumulator accumulator = new LongAccumulator(op, 1L);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 10)
                .forEach(i -> executor.submit(() -> accumulator.accumulate(i)));

        Utils.stop(executor);
        System.out.println(accumulator.getThenReset());
    }

    static void init(){
        map.put("foo", "bar");
        map.put("han", "solo");
        map.put("r2", "d2");
        map.put("c3", "p0");
    }

    static void printMap(){
        map.forEach((key, value) -> System.out.printf("%s = %s\n", key, value));
    }
    static void mapFunctions(){
        init();
        //printMap();

        /*String value1 = map.putIfAbsent("c3", "p1");
        System.out.println(value1);

        String value2 = map.getOrDefault("hi", "there");
        System.out.println(value2);

        map.replaceAll((key, value) -> "r2".equals(key) ? "d3" : value);
        System.out.println(map.get("r2"));

        map.compute("foo", (key, value) -> value + value);
        System.out.println(map.get("foo"));

        map.merge("foo", "boo", (oldValue, newValue) -> newValue + " was " + oldValue);
        System.out.println(map.get("foo"));*/

        System.out.println(ForkJoinPool.getCommonPoolParallelism());

        map.forEach(1, (key, value) ->
                System.out.printf("key: %s; value: %s; thread: %s\n",
                        key, value, Thread.currentThread().getName()));

        String result = map.search(1, (key, value) -> {
            System.out.println(Thread.currentThread().getName());
            if ("foo".equals(key)) {
                return value;
            }
            return null;
        });
        System.out.println("Result: " + result);

        String result2 = map.searchValues(1, value -> {
            System.out.println(Thread.currentThread().getName());
            if (value.length() > 3) {
                return value;
            }
            return null;
        });
        System.out.println("Result: " + result2);

        String result3 = map.reduce(1,
                (key, value) -> {
                    System.out.println("Transform: " + Thread.currentThread().getName());
                    return key + "=" + value;
                },
                (s1, s2) -> {
                    System.out.println("Reduce: " + Thread.currentThread().getName());
                    return s1 + ", " + s2;
                });
        System.out.println("Result: " + result3);
    }
}
