package com.abhi.JavaThreads.models;

public class Thread1 extends Thread {

    @Override
    public void run() {
        Data.iterate("t1");
    }
}
