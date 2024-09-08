package com.abhi.JavaThreads.models;

import java.util.ArrayList;

public class Data {
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
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        }

    }
}
