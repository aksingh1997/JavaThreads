package com.abhi.JavaThreads.Service;

import com.abhi.JavaThreads.models.Data;
import com.abhi.JavaThreads.models.Thread1;
import com.abhi.JavaThreads.models.Thread2;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

@Service
public class ThreadService {

    Data data;

    Thread t1 = null;
    Thread t2 = null;

    public ThreadService(Data data) {
        this.data = data;
        //executeThreadsByExtendingThreadClass();
        //executeThreadsByImplementingRunnable();
        //executeSynchronizedThreads();
        //reentrantLock();
        //waitNotifyThreads();
        //conditionalLock();
        //readWriteLock();
        // To do -> Optimistic read with versioning, Thread pooling - (ExecutorService and ForkJoinPool), volatile, tryLock(), Concurrent collections, Future.
        threadPoolExecutorService();
    }

    public void executeThreadsByExtendingThreadClass() {
        t1 = new Thread1();
        t2 = new Thread2();
        t1.start();
        t2.start();
    }

    // better approach
    public void executeThreadsByImplementingRunnable() {
        // providing implementation of Runnable
        Thread r1 = new Thread( () -> { Data.iterate("r1"); } );
        Thread r2 = new Thread( () -> { Data.iterate("r2"); } );
        r1.start();
        r2.start();
    }

    public void executeSynchronizedThreads() {
        Thread s1 = new Thread(Data::syncIterate, "s1");
        Thread s2 = new Thread(Data::syncIterate, "s2");
        s1.start();
        s2.start();
    }

    // we can use wait() and notify() for threads
    // producer will only produce when consumer notifies. Otherwise producer thread will wait.
    // Note: Even when thread is in waiting, no other thread is allowed to enter in synchronised block
    public void waitNotifyThreads() {

        for(int i = 0; i < 10; i++) {
            if(i % 2 == 0)
                new Thread(() -> data.produce(), String.valueOf(i)).start();
            else
                new Thread(() -> data.consume(), String.valueOf(i)).start();
        }
    }

    public void conditionalLock() {
        for(int i = 0; i < 100; i++) {
            new Thread(() -> data.consumeBiscuits()).start();
        }
        for(int i = 0; i < 100; i++) {
            new Thread(() -> data.produceBiscuits()).start();
        }

    }
    // Reentrant locks are used in case of recursive calls and inner method calls. A thread holding a lock of Reentrant lock can acquire it again.
    // Once a thread acquires a Reentrant lock, it can acquire it again, but other threads cannot acquire the same lock.
    // we can take example of gcd()
    public void reentrantLock() {
        BiFunction<Integer, Integer, Integer> printGcd = (x, y) -> data.gcd(x, y);
        for(int i = 1; i < 10; i++) {
            for(int j = 1; j < 20; j++) {
                // we need to create final variables here as lambda exp wont accept non final variables.
                // Reason = Thread creation line contains gcd(x, y). Here x and y will be replaced by its exact value in lambda at compile time. So it should be constant.
                final int x = i;
                final int y = j;
                new Thread(() -> System.out.println("gcd of " + x + " and " + y + " = " + data.gcd(x, y) + "\n\n")).start();
                // same thread will put lock on gcd method in recursion, increases its counter on each call, and decreases the lock counter while returning.

                // Expected output is of type -
                /*
                    Thread-151 ==> 1
                    Thread-151 ==> 2
                    Thread-151 ==> 3
                    gcd of 8 and 15 = 1
                */

                // But sometimes we can also get output like -
                /*
                    Thread-155 ==> 1
                    Thread-155 ==> 2
                    Thread-155 ==> 3
                    Thread-155 ==> 4
                    Thread-157 ==> 1
                    Thread-157 ==> 2
                    gcd of 9 and 2 = 1
                 */

                // this is due to reason that lock is released by Thread 155 and the time when result comes back and prints it in ThreadService class, Thread-157 has
                // acquired lock already.
            }
        }
    }

    public void readWriteLock() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++) {
            int p = rand.nextInt(3);
            Thread thread = null;
            switch (p) {
                case 0:
                    thread = new Thread(() -> data.withdrawAmount(300));
                    break;
                case 1:
                    thread = new Thread(() -> data.depositAmount(100));
                    break;
                case 2:
                    thread = new Thread(() -> data.readBalance());
                    break;
                default:
                    System.out.println("Wrong choice");
                    break;
            }
            thread.start();
        }
    }

    // Thread pool using ThreadPoolExecutor
    public void threadPoolExecutorService() {
        // The easiest and simple way to initialize a thread pool using thread pool executor, we can use other constructors too for better control over threads.
        /*
        * Let's see each param of constructor
        * 1. corePoolSize - no of threads pool should begin with. They are always kept alive. Should be equal to actual no of cores for CPU-intensive work. Can be 2x/4x for IO intensive
        * 2. maximumPoolSize - Once all the corePool threads are busy and queue is full, new threads are spawned which can go upto maximumPoolSize. They may die if idle.
        * 3 & 4. keepAliveTime and TimeUnit - Amount of time for which a non-core thread can remain idle in pool before getting discarded.
        * 5. BlockingQueue<Runnable> - We can provide different implementation of this interface. This is to hold the threads in queue when other threads in thread pool
        * are busy processing. This can be of two types -> Unbounded[no fixed size] - LinkedBlockingQueue && Bounded[fixed size] - ArrayBlockingQueue. ArrayBlockingQueue
        * is preferred as it performs well, and we can set boundary for no of threads that should remain in queue
        *
        * Note :: If a thread comes in a situation when pool is working on maximumPoolSize with all threads busy and queue is full, the incoming thread will be discarded.
        * There are approaches of how those discarded threads are handled. Four policies are -
        * 1. AbortPolicy(default) - Throws exception on each discarded policy so that it can be handled and logged
        * 2. CallerRunsPolicy - The calling(parent) thread that submits the task runs the rejected task itself. Used to avoid any loss of tasks
        * 3. DiscardPolicy - Discard the task silently without throwing exception
        * 4. DiscardOldestPolicy - replace the oldest task from queue with new task. Used when new tasks have higher priority than older ones.
        */
        ExecutorService myThreadPool = new ThreadPoolExecutor(4, 8, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(80));

        // submitting 100 tasks in thread pool, lets see how many will get discarded
        int failedTask = 0;
        for(int i = 0; i < 100; i++) {
            final int taskNo = i;
            try {
                //Thread.sleep(10); // Thread incoming rate in pool is 10ms. Uncomment this line to reduce the failed task to 0.
                myThreadPool.execute( () -> {
                    try {
                        data.poolThreadsDoHundredTasks(taskNo);
                    } catch(Exception ex) {
                        System.out.println("Exception occured for task no :: " + taskNo);
                    }
                } );
            } catch(Exception ex) {
                failedTask++;
            }
        }
        System.out.println("No of failed task = " + failedTask);

        // No of cor
    }
}
