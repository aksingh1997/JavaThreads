package com.abhi.JavaThreads.models;

public class Thread2 extends Thread {
    @Override
    public void run() {
        Data.iterate("t2");
    }
}
