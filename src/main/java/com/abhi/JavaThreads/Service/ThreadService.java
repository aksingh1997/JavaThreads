package com.abhi.JavaThreads.Service;

import com.abhi.JavaThreads.models.Data;
import com.abhi.JavaThreads.models.Thread1;
import com.abhi.JavaThreads.models.Thread2;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

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
        readWriteLock();

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
}
