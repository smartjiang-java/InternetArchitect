package com.jqk.cache; /**
 * ClassName:com.jqk.cache.CutCacheLinePadding
 * Package:PACKAGE_NAME
 * Description:
 *
 * @Date:2020/10/17 19:47
 * @Author:JiangQiKun
 * @Email:1677081700@qq.com
 */

/**
 * 优化缓存行
 */
public class CutCacheLinePadding {
    public static volatile long[] arr = new long[16];

    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(()->{
            for (long i = 0; i < 10000_0000L; i++) {
                arr[0] = i;
            }
        });

        Thread t2 = new Thread(()->{
            for (long i = 0; i < 10000_0000L; i++) {
                arr[8] = i;
            }
        });

        final long start = System.nanoTime();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println((System.nanoTime() - start)/100_0000);
    }
}
