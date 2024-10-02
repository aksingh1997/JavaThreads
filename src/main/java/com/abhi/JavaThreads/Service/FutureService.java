package com.abhi.JavaThreads.Service;

import com.abhi.JavaThreads.util.FutureUtil;

public class FutureService {

    public static void main(String[] args) {
        FutureUtil futureUtil = new FutureUtil();
        //System.out.println(futureUtil.getHello());
        futureUtil.supplyAsync();
    }
}
