package task;

public class Util {

    public static Thread[] createThreadsByRunnables(Runnable[] runnables){
        final Thread[] threads = new Thread[runnables.length];
        for (int i = 0; i < runnables.length; i++) {
            if (runnables[i] instanceof Thread) {
                threads[i] = (Thread) runnables[i];
            } else {
                threads[i] = new Thread(runnables[i]);
            }
        }
        return threads;
    }
}
