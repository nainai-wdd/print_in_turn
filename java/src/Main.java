import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
        };
        // 打印总数
        int max = 10000;
        // 协作线程数量
        int maxPos = 3;
        // 取平均值时的窗口大小
        int times = 5;
        // 多次执行，结果取平均值
        final List<String> resultList = Arrays.stream(taskTypes)
                .map(taskType -> {
                    final double avg = IntStream.range(0, times)
                            .mapToLong(i -> test_async_once(taskType, max, maxPos)).average().orElse(0L);
                    return taskType + " spend: " + avg + "ms";
                })
                .collect(Collectors.toList());
        // 单线程执行
        final long testSyncMillis = testSync(max);
        // 打印结果
        System.out.println("=================aync====================");
        resultList.forEach(System.out::println);
        System.out.println("===================sync====================");
        System.out.println("sync spend: " + testSyncMillis + "ms");
    }

    private static long test_async_once(String taskType, int max, int maxPos) {
        System.out.println("=================start:" + taskType + "====================");
        final CountDownLatch latch = new CountDownLatch(maxPos);
        final Runnable[] tasks = getTaskList(taskType, max, latch, maxPos);
        final ExecutorService threadPool = Executors.newFixedThreadPool(maxPos);
        try {
            final LocalDateTime start = LocalDateTime.now();
            for (Runnable task : tasks) {
                threadPool.submit(task);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final LocalDateTime end = LocalDateTime.now();
            return Duration.between(start, end).toMillis();
        } finally {
            threadPool.shutdown();
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




