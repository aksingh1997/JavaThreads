package com.abhi.JavaThreads.Service;

import com.abhi.JavaThreads.util.ThreadUtil;
import com.abhi.JavaThreads.models.Thread1;
import com.abhi.JavaThreads.models.Thread2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

@Service
public class ThreadService {

    ThreadUtil threadUtil;

    Thread t1 = null;
    Thread t2 = null;

    public ThreadService(ThreadUtil threadUtil) {
        this.threadUtil = threadUtil;
        //executeThreadsByExtendingThreadClass();
        //executeThreadsByImplementingRunnable();
        //executeSynchronizedThreads();
        //reentrantLock();
        //waitNotifyThreads();
        //conditionalLock();
        //readWriteLock();
        // To do -> Optimistic read with versioning, Thread pooling - (ExecutorService and ForkJoinPool), volatile, tryLock(), Concurrent collections, Future.
        //threadPoolExecutorService_execute();
        //threadPoolExecutorService_submit();
        forkJoinPool();
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
        Thread r1 = new Thread( () -> { ThreadUtil.iterate("r1"); } );
        Thread r2 = new Thread( () -> { ThreadUtil.iterate("r2"); } );
        r1.start();
        r2.start();
    }

    public void executeSynchronizedThreads() {
        Thread s1 = new Thread(ThreadUtil::syncIterate, "s1");
        Thread s2 = new Thread(ThreadUtil::syncIterate, "s2");
        s1.start();
        s2.start();
    }

    // we can use wait() and notify() for threads
    // producer will only produce when consumer notifies. Otherwise producer thread will wait.
    // Note: Even when thread is in waiting, no other thread is allowed to enter in synchronised block
    public void waitNotifyThreads() {

        for(int i = 0; i < 10; i++) {
            if(i % 2 == 0)
                new Thread(() -> threadUtil.produce(), String.valueOf(i)).start();
            else
                new Thread(() -> threadUtil.consume(), String.valueOf(i)).start();
        }
    }

    public void conditionalLock() {
        for(int i = 0; i < 100; i++) {
            new Thread(() -> threadUtil.consumeBiscuits()).start();
        }
        for(int i = 0; i < 100; i++) {
            new Thread(() -> threadUtil.produceBiscuits()).start();
        }

    }
    // Reentrant locks are used in case of recursive calls and inner method calls. A thread holding a lock of Reentrant lock can acquire it again.
    // Once a thread acquires a Reentrant lock, it can acquire it again, but other threads cannot acquire the same lock.
    // we can take example of gcd()
    public void reentrantLock() {
        BiFunction<Integer, Integer, Integer> printGcd = (x, y) -> threadUtil.gcd(x, y);
        for(int i = 1; i < 10; i++) {
            for(int j = 1; j < 20; j++) {
                // we need to create final variables here as lambda exp wont accept non final variables.
                // Reason = Thread creation line contains gcd(x, y). Here x and y will be replaced by its exact value in lambda at compile time. So it should be constant.
                final int x = i;
                final int y = j;
                new Thread(() -> System.out.println("gcd of " + x + " and " + y + " = " + threadUtil.gcd(x, y) + "\n\n")).start();
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
                    thread = new Thread(() -> threadUtil.withdrawAmount(300));
                    break;
                case 1:
                    thread = new Thread(() -> threadUtil.depositAmount(100));
                    break;
                case 2:
                    thread = new Thread(() -> threadUtil.readBalance());
                    break;
                default:
                    System.out.println("Wrong choice");
                    break;
            }
            thread.start();
        }
    }

    // Thread pool using ThreadPoolExecutor
    public void threadPoolExecutorService_execute() { // using execute() will return void from executor service
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
                        threadUtil.poolThreadsDoHundredTasks(taskNo);
                    } catch(Exception ex) {
                        System.out.println("Exception occured for task no :: " + taskNo);
                    }
                } );
            } catch(Exception ex) {
                failedTask++;
            }
        }
        System.out.println("No of failed task = " + failedTask);
    }

    // submit method returns Future which will contain the result processed by thread pool
    public void threadPoolExecutorService_submit() {
        ExecutorService myThreadPool = new ThreadPoolExecutor(4, 8, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(80));
        List<Future<Integer>> futureList = new ArrayList<>();
        for(int i = 0; i <= 50; i++) {
            final int x = i;
            Future<Integer> future = null;
            try {
                // Once we submit the task in thread pool, one of the threads will pick the task and execute it. The order of execution is upto jvm. Result will be stored in Future
                // The future is immediately returned, we can use isDone() to check the status and get() to block parent's thread execution until we get the result.
                future = myThreadPool.submit(() -> threadUtil.doSquare(x));
            } catch(Exception ex) {
                System.out.println("Exception occurred for no:: " + x);
            }
            futureList.add(future);
        }
        //since we have list of future, here I am checking if we have any thread which is not done using isDone() on each future object, if all are done, proceed to get result, or else loop
        boolean flag = true;
        while(flag) {
            Future<Integer> processingFuture = futureList.stream().filter(x -> !x.isDone()).findAny().orElse(null);
            if(processingFuture == null)
                flag = false;
        }
        futureList.stream().forEach(x -> {
            try {
                System.out.println(x.get());
            } catch (Exception ex) {
                System.out.println("Exception while getting future");
            }
        });
    }

    /*
    * Fork join pool is used for those task that can be broken down further into subtasks, and each subtasks can be executed in parallel.
    * We can use commonPool provided by JVM in ForkJoinPool. We can create our thread pool too by assigning n number of threads to pool.
    * Each thread has its own deque along with a global common deque for all threads. A thread picks task for common deque , it keeps its subtasks in its own deque.
    * Work stealing algo:- If a thread has completed its task and no task is left in its own deque, it can steal work from other thread's deque from tail. This ensures that
    * all threads are busy and work is divided evenly. If thread cannot steal work , it will pick new task from global deque.
    *
    * ForkJoinTask - Object of this class is required as input in forkJoin pool. It has compute() method which needs to be overridden. Two implementations are
    *   i. RecursiveAction -> void compute() {implementation...}
    *   ii. RecursiveTask<T> -> T compute() {implementation...}
    */
    public void forkJoinPool() {
        // ForkJoinPool forkPool = ForkJoinPool.commonPool(); // this is common pool managed by jvm, quick and ready to use;
        ForkJoinPool forkPool = new ForkJoinPool(6);
        int n = 100;
        int[] arr1 = new int[n];
        int[] arr2 = new int[n];
        for(int i = 0; i < n; i++) {
            arr1[i] = new Random().nextInt(1, 100);
            arr2[i] = arr1[i];
        }


        long startTime = System.currentTimeMillis();
        //normal sorting
        threadUtil.mergeSort(arr2, 0, n - 1);
        System.out.println("Normal sorting time taken : " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();

        threadUtil.mergeSort_forkJoin(arr1, 0, n-1);
        System.out.println("Fork join sorting time taken : " + (System.currentTimeMillis() - startTime));
        //Arrays.stream(sortedArr).forEach(System.out::println);
    }
}
