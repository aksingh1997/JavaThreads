package com.abhi.JavaThreads.Service;

import com.abhi.JavaThreads.models.Data;
import com.abhi.JavaThreads.models.Thread1;
import com.abhi.JavaThreads.models.Thread2;
import org.springframework.stereotype.Service;

@Service
public class ThreadService {

    Thread t1 = null;
    Thread t2 = null;

    public ThreadService() {
        //executeThreadsByExtendingThreadClass();
        //executeThreadsByImplementingRunnable();
        executeSynchronizedThreads();
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

    public void Lo
}
