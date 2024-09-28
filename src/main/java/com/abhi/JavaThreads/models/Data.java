package com.abhi.JavaThreads.models;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.locks.*;

@Component
public class Data {

    ReentrantLock gcdLock = new ReentrantLock();
    //ReentrantLock gcdLock = new ReentrantLock(true); // provide true as input as fairness flag, default is false. Fairness means FIFO/FCFS. This may reduce speed.

    // biscuit producer and consumer problem related variables
    int noOfBiscuits = 0;
    int inventorySize = 60;
    Lock biscuitLock = new ReentrantLock();
    Condition notFull = biscuitLock.newCondition();
    Condition notEmpty = biscuitLock.newCondition();

    // banking read write lock variables
    int accountBalance = 1000;
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    Lock readLock = readWriteLock.readLock();
    Lock writeLock = readWriteLock.writeLock();


    public static ArrayList<Integer> ls;
    static {
        ls = new ArrayList<>();
        for(int i = 0; i < 10; i++)
            ls.add(i);
    }

    public static void iterate(String threadName) {
        try {
            for (Integer l : ls) {
                Thread.sleep(500);
                System.out.println(threadName + " " + l);
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void syncIterate() {
        // we are taking class lock instead of object lock as it is static method
        /* a thread entering this segment will acquire lock and release it after exiting from block, This is basic way of preventing multiple threads accessing
        / same resource */
        synchronized(Data.class) {
            try {
                for (Integer l : ls) {
                    Thread.sleep(500);
                    System.out.println(Thread.currentThread().getName() + " " + l);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    //Notice that consumer and producer have same lock applied -> object of this class.
    public synchronized void produce() {
        try {
            wait(); // wait until consumer has consumed and notifies the object lock.
            System.out.println( Thread.currentThread().getName() + " producing-------");
            Thread.sleep(1);
            System.out.println(Thread.currentThread().getName() + " produced-------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public synchronized void consume() {
        try {
            System.out.println(Thread.currentThread().getName() + " consuming--------");
            Thread.sleep(1);
            System.out.println(Thread.currentThread().getName() + " consumed--------");
            notify(); // notify the waiting producers to start producing
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void produceBiscuits() {
        biscuitLock.lock();
        try {
            while(noOfBiscuits == inventorySize) { //Here we are using while loop instead of if , so that whenever a thread wakes up , it should check this condition again and proceed
                notFull.await(); // using await in a condition causes thread to release the lock temporarily until is it awakened by signal or interruption
            }
            Thread.sleep(10); // using sleep does not cause Thread to release the lock. Thread holds the lock during sleep.
            noOfBiscuits++;
            System.out.println("Biscuit produced. Total biscuit = " + noOfBiscuits);
            notEmpty.signal(); // since the biscuit is produced so inventory is not empty now, signal the consumer to consume
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        biscuitLock.unlock();

    }
    public void consumeBiscuits() {
        biscuitLock.lock();
        try {
            while(noOfBiscuits == 0) {
                notEmpty.await();
            }
            Thread.sleep(10);
            noOfBiscuits--;
            System.out.println("Biscuit consumed. Total biscuit = " + noOfBiscuits);
            notFull.signal(); // since the biscuit is consumed so inventory in not full now, signal the producer to produce more
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        biscuitLock.unlock();
    }

    public void reentrantLock() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            for (Integer l : ls) {
                Thread.sleep(500);
                System.out.println(Thread.currentThread().getName() + " " + l);
                
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int gcd(int x, int y) {

        if(y == 0)
            return x;

        // swap if x < y
        gcdLock.lock();
        System.out.println(Thread.currentThread().getName() + " ==> " + gcdLock.getHoldCount());
        if(y > x) {
            x += y;
            y = x - y;
            x = x - y;
        }
        int p = gcd(y, x % y);
        gcdLock.unlock();
        return p;
    }

    // withdraw amount by putting write lock
    public void withdrawAmount(int money) {
        writeLock.lock();
        try {
            if(accountBalance - money < 0) {
                System.out.println("Insufficient balance");
                return;
            }
            Thread.sleep(200);
            accountBalance -= money;
            System.out.println("Withdraw:: " + money + ", Total Balance:: " + accountBalance);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        writeLock.unlock();
    }

    // deposit amount by putting write lock
    public void depositAmount(int money) {
        writeLock.lock();
        try {
            Thread.sleep(350);
            accountBalance += money;
            System.out.println("Deposit:: " + money + ", Total Balance:: " + accountBalance);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        writeLock.unlock();
    }

    // view account balance by putting read lock
    public void readBalance() {
        readLock.lock();
        try {
            Thread.sleep(200);
            System.out.println("Total Balance:: " + accountBalance);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        readLock.unlock();
    }

    // ExecutorService Thread pooling
    public void poolThreadsDoHundredTasks(int taskNumber) throws Exception {
        System.out.println("Started doing task with taskNumber:: " + taskNumber + " threadNo:: " + Thread.currentThread().getName());
        /*
        Lets say task takes 100ms. Now threads in pool can execute the sleep command simultaneously too.
         */
        Thread.sleep(100);
        System.out.println("task completed:: " + taskNumber);
    }
}
