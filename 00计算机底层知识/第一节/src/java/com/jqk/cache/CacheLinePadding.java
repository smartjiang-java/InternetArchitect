package com.jqk.cache;

import sun.misc.Contended;

/**
 * ClassName:com.jqk.cache.CacheLinePadding
 * Package:PACKAGE_NAME
 * Description:
 *
 * @Date:2020/10/17 19:43
 * @Author:JiangQiKun
 * @Email:1677081700@qq.com
 */

/**
 * 验证缓存行的存在
 */

public class CacheLinePadding {

    public static volatile long[] arr = new long[2];

    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(() -> {
            for (long i = 0; i < 10000_0000L; i++) {
                arr[0] = i;
            }
        });

        Thread t2 = new Thread(() -> {
            for (long i = 0; i < 10000_0000L; i++) {
                arr[1] = i;
            }
        });

        final long start = System.nanoTime();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println((System.nanoTime() - start) / 100_0000);
    }
}
