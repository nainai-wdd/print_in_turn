package task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SyncTask5 implements Runnable {
    // 打印的最大值
    private final int max;
    // 所处任务的序号（执行序号）
    private final int pos;
    // 总共的协作任务数量
    private final int maxPos;
    // 用于任务间传递信号的阻塞队列（传递执行）
    private final ArrayBlockingQueue<Integer>[] queueList;
    // 倒计时器，用于主线程接收任务结束信号
    private final CountDownLatch latchForEnd;
    public SyncTask5(int pos, int max, CountDownLatch latchForEnd, ArrayBlockingQueue<Integer>[] queueList, int maxPos) {
        this.max = max;
        this.pos = pos;
        this.queueList = queueList;
        this.latchForEnd = latchForEnd;
        this.maxPos = maxPos;
    }



    /**
     * 利用阻塞队列，在线程间传递信号，实现线程间的同步和有序执行
     */
    @Override
    public void run() {
        if (pos == 0) {
            queueList[pos].add(1);
        }
        try {
            int i = pos;
            final int nextPost = (pos + 1) % maxPos;
            while (i <= max) {
                queueList[pos].take();
//                System.out.println("T-" + pos + " " + i);
                System.out.println(i);
                i += maxPos;
                queueList[nextPost].add(1);
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            latchForEnd.countDown();
        }
    }

    public static Runnable[] getTaskList(int max, int maxPos, CountDownLatch endLatch) {
        final ArrayBlockingQueue<Integer>[] queueList = new ArrayBlockingQueue[maxPos];
        for (int i = 0; i < maxPos; i++) {
            queueList[i] = new ArrayBlockingQueue<>(1);
        }
        Runnable[] runnables = new Runnable[maxPos];
        for (int i = 0; i < maxPos; i++) {
            runnables[i] = new SyncTask5(i, max, endLatch, queueList, maxPos);
        }
        return runnables;
    }

}