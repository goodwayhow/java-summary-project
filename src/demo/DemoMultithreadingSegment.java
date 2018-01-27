/*
 * <summary></summary>
 * <author>hankcs</author>
 * <email>me@hankcs.com</email>
 * <create-date>2015/5/11 12:46</create-date>
 *
 * <copyright file="DemoMultithreadingSegment.java">
 * Copyright (c) 2003-2015, hankcs. All Right Reserved, http://www.hankcs.com/
 * </copyright>
 */
package demo;

import hanlp.HanLP;
import hanlp.seg.Viterbi.ViterbiSegment;
import hanlp.seg.Config;
import hanlp.seg.Segment;

/**
 * 演示多线程并行分词
 * 由于HanLP的任何分词器都是线程安全的，所以用户只需调用一个配置接口就可以启用任何分词器的并行化
 *
 * @author hankcs
 */
public class DemoMultithreadingSegment
{
    public static void main(String[] args)
    {
        
        
        Segment segment=new ViterbiSegment().enableAllNamedEntityRecognize(true);
        
        String text="宮崎駿在1941年1月5日出生於東京都文京區，宮崎駿在1941年1月5日出生於東京都文京區，在四個兄弟中排名第二，父親是宮崎家族經營的「宮崎航空興學」的職員。直到1945年第二次世界大戰結束為止宮崎駿度過了相當自由的幼年生活。";
        
        System.out.printf("text長度:%d",text.length());
        HanLP.Config.ShowTermNature=true;
        System.out.println(segment.seg(text));
        //////////////////////////////////////
        int pressure = 100;
        StringBuilder sbBigText = new StringBuilder(text.length() * pressure);
        for (int i = 0; i < pressure; i++)
        {
            sbBigText.append(text);
        }
        text = sbBigText.toString();
        System.gc();

        long start;
        double costTime;
        // 测个速度
        /*
        segment.enableMultithreading(false);
        start = System.currentTimeMillis();
        segment.seg(text);
        costTime = (System.currentTimeMillis() - start) / (double) 100;
        System.out.printf("单线程分词速度：%.2f字每秒\n", text.length() / costTime);
        System.gc();
        */
       
        
        segment.enableMultithreading(true); // 或者 segment.enableMultithreading(4);
        start = System.currentTimeMillis();
        segment.seg(text);
        costTime = (System.currentTimeMillis() - start) / (double) 100;
        System.out.printf("多线程分词速度：%.2f字每秒\n", text.length() / costTime);
        System.gc();
        /*
        System.out.printf("長度:%d\n",text.length());
        if(Config.flagthread==true) {System.out.println("MultiThreading有啟動");}//這裡長度還要多除以100 才等於實際字串長度
        if(Config.flagthread==false) {System.out.println("MultiThreading沒有啟動");}*/
        // Note:
        // 内部的并行化机制可以对1万字以上的大文本开启多线程分词
        // 另一方面，HanLP中的任何Segment本身都是线程安全的。
        // 你可以开10个线程用同一个CRFSegment对象切分任意文本，不需要任何线程同步的措施，每个线程都可以得到正确的结果。
    }
}
