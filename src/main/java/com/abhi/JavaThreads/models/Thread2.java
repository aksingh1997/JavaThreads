package com.abhi.JavaThreads.models;

import com.abhi.JavaThreads.util.ThreadUtil;

public class Thread2 extends Thread {
    @Override
    public void run() {
        ThreadUtil.iterate("t2");
    }
}
