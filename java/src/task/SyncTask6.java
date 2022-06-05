package task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class SyncTask6 implements Runnable {
    // 打印的最大值
    private final int max;
    // 所处任务的序号（执行序号）
    private final int pos;
    // 总共的协作任务数量
    private final int maxPos;
    // 用于任务间传递信号的信号量（传递执行）
    private final Semaphore[] semaphores;
    // 倒计时器，用于主线程接收任务结束信号
    private final CountDownLatch latchForEnd;
    public SyncTask6(int pos, int max, CountDownLatch latchForEnd, Semaphore[] semaphores, int maxPos) {
        this.max = max;
        this.pos = pos;
        this.semaphores = semaphores;
        this.latchForEnd = latchForEnd;
        this.maxPos = maxPos;
    }



    /**
     * 利用信号量，在线程间传递信号，实现线程间的同步和有序执行
     */
    @Override
    public void run() {
        if (pos == 0) {
            semaphores[pos].release();
        }
        try {
            int i = pos;
            final int nextPost = (pos + 1) % maxPos;
            while (i <= max) {
                semaphores[pos].acquire();
//                System.out.println("T-" + pos + " " + i);
                System.out.println(i);
                i += maxPos;
                semaphores[nextPost].release();
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            latchForEnd.countDown();
        }
    }

    public static Runnable[] getTaskList(int max, int maxPos, CountDownLatch endLatch) {
        final Semaphore[] queueList = new Semaphore[maxPos];
        for (int i = 0; i < maxPos; i++) {
            queueList[i] = new Semaphore(1);
            queueList[i].release();
        }
        Runnable[] runnables = new Runnable[maxPos];
        for (int i = 0; i < maxPos; i++) {
            runnables[i] = new SyncTask6(i, max, endLatch, queueList, maxPos);
        }
        return runnables;
    }

}