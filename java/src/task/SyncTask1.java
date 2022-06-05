package task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;


public class SyncTask1 implements Runnable {
    // 打印的最大值
    private final int max;
    // 所处任务的序号（执行序号）
    private final int pos;
    // 总共的协作任务数量
    private final int maxPos;
    // 当前待执行任务的序号，共享内存来传递信息
    private final AtomicInteger curPos;
    // 倒计时器，用于主线程接收任务结束信号
    private final CountDownLatch latchForEnd;

    public SyncTask1(int pos, int max, AtomicInteger curPos, CountDownLatch latch, int maxPos) {
        this.pos = pos;
        this.max = max;
        this.curPos = curPos;
        this.latchForEnd = latch;
        this.maxPos = maxPos;
    }

    /**
     * 在同步代码块里进行判断，如果是自己的位置，则打印，否则重试
     */
    @Override
    public void run() {
        try {
            int i = pos;
            final int nextPos = (pos + 1) % maxPos;
            while (i <= max) {
                synchronized (curPos) {
                    if (curPos.get() == pos) {
//                        System.out.println("T-" + pos + " " + i);
                        System.out.println(i);
                        i += maxPos;
                        curPos.set(nextPos);
                    }
                }
            }
        } finally {
            latchForEnd.countDown();
        }
    }

    public static Runnable[] getTaskList(int max, int maxPos, CountDownLatch endLatch) {
        Runnable[] runnables = new Runnable[maxPos];
        final AtomicInteger curPos = new AtomicInteger(0);
        for (int i = 0; i < maxPos; i++) {
            runnables[i] = new SyncTask1(i, max, curPos, endLatch, maxPos);
        }
        return runnables;
    }
}