package task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class SyncTask7 implements Runnable {
    // 打印的最大值
    private final int max;
    // 所处任务的序号（执行序号）
    private final int pos;
    // 总共的协作任务数量
    private final int maxPos;
    // 当前待执行任务的序号，共享内存来传递信息
    private final AtomicInteger curPos;
    // 任务对应的线程，利用LockSupport手动控制线程的状态
    private volatile Thread[] threads;
    // 倒计时器，用于主线程接收任务结束信号
    private final CountDownLatch latchForEnd;

    public SyncTask7(int pos, int max, AtomicInteger curPos, CountDownLatch latch, int maxPos) {
        this.pos = pos;
        this.max = max;
        this.curPos = curPos;
        this.latchForEnd = latch;
        this.maxPos = maxPos;
    }

    public void setThreads(Thread[] threads) {
        this.threads = threads;
    }

    /**
     * 将整个循环放到同步代码块里，循环判断.
     * 如果是自己的位置，则打印，并且通过LockSupport.unpark唤醒下一个待办任务对应的线程，随后LockSupport.park()释放线程执行权
     * 如果不是自己的位置，则LockSupport.park()释放线程执行权
     */
    @Override
    public void run() {
        try {
            int i = pos;
            final int nextPost = (pos + 1) % maxPos;
            while (i <= max) {
                if (curPos.get() == pos) {
//                    System.out.println("T-" + pos + " " + i);
                    System.out.println(i);
                    i += maxPos;
                    curPos.set(nextPost);
                    LockSupport.unpark(threads[nextPost]);
                } else {
                    LockSupport.park();
                }
            }
        } finally {
            latchForEnd.countDown();
        }
    }

    public static Thread[] getTaskList(int max, int maxPos, CountDownLatch endLatch) {
        final AtomicInteger curPos = new AtomicInteger(0);
        final SyncTask7[] runnables = new SyncTask7[maxPos];
        for (int i = 0; i < maxPos; i++) {
            runnables[i] = new SyncTask7(i, max, curPos, endLatch, maxPos);
        }
        final Thread[] threads = new Thread[maxPos];
        for (int i = 0; i < maxPos; i++) {
            threads[i] = new Thread(runnables[i]);
        }
        for (int i = 0; i < maxPos; i++) {
            runnables[i].setThreads(threads);
        }
        return threads;
    }

}