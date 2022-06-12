import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        String[] taskTypes = new String[]{
                "SyncTask1",
                "SyncTask2",
                "SyncTask3",
                "SyncTask4",
                "SyncTask5",
                "SyncTask6",
                "SyncTask7",
        };
        // 打印总数
        int max = 10000;
        // 协作线程数量
        int maxPos = 20;
        // 取平均值时的窗口大小
        int times = 3;
        // 单线程执行
        final long testSyncMillis = testSync(max);
        // 多次执行，结果取平均值
        final Map<String, Long> reduce = IntStream.range(0, times).mapToObj(i ->
                        // 计算一次所有类型的任务，获取单次执行耗时
                        Arrays.stream(taskTypes).collect(Collectors.toMap(taskType -> taskType,
                                taskType -> test_async_once(taskType, max, maxPos)))
                // 多次计算，获取总耗时
        ).reduce(new HashMap<>(16), (map, next) -> {
            next.forEach((k, v) -> map.put(k, map.getOrDefault(k, 0L) + v));
            return map;
        });
        System.out.println("===================async====================");
        // 打印，获取平均耗时
        reduce.forEach((k, v) -> System.out.println(k + "spend: " + v / times + "ms"));
        // 打印结果
        System.out.println("===================sync====================");
        System.out.println("sync spend: " + testSyncMillis + "ms");
    }

    private static long test_async_once(String taskType, int max, int maxPos) {
        System.out.println("=================start:" + taskType + "====================");
        final CountDownLatch latch = new CountDownLatch(maxPos);
        Runnable[] tasks = getTaskList(taskType, max, latch, maxPos);
        Thread[] threads = Util.createThreadsByRunnables(tasks);
        try {
            final LocalDateTime start = LocalDateTime.now();
            for (Thread thread : threads) {
                thread.start();
            }
            latch.await();
            final LocalDateTime end = LocalDateTime.now();
            return Duration.between(start, end).toMillis();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Runnable[] getTaskList(String taskType, int max, CountDownLatch latch, int maxPos) {
        switch (taskType) {
            case "SyncTask1":
                return SyncTask1.getTaskList(max, maxPos, latch);
            case "SyncTask2":
                return SyncTask2.getTaskList(max, maxPos, latch);
            case "SyncTask3":
                return SyncTask3.getTaskList(max, maxPos, latch);
            case "SyncTask4":
                return SyncTask4.getTaskList(max, maxPos, latch);
            case "SyncTask5":
                return SyncTask5.getTaskList(max, maxPos, latch);
            case "SyncTask6":
                return SyncTask6.getTaskList(max, maxPos, latch);
            case "SyncTask7":
                return SyncTask7.getTaskList(max, maxPos, latch);
            default:
                throw new IllegalArgumentException(taskType + "___taskType is not supported");
        }
    }

    private static long testSync(int max) {
        final LocalDateTime start = LocalDateTime.now();
        for (int i = 0; i < max; i++) {
            System.out.println(i);
        }
        final LocalDateTime end = LocalDateTime.now();
        return Duration.between(start, end).toMillis();
    }

}




