/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/8/22 15:58</create-date>
 *
 * <copyright file="TextRank.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package hanlp.summary;


import hanlp.HanLP;
import hanlp.dictionary.stopword.CoreStopWordDictionary;
import hanlp.seg.Config;
import hanlp.seg.common.Term;
import hanlp.tokenizer.StandardTokenizer;



import java.util.*;

/**
 * TextRank 自动摘要
 *
 * @author hankcs
 */
public class TextRankSentence
{
    /**
     * 阻尼系数（ＤａｍｐｉｎｇＦａcｔｏｒ），一般取值0.85
     */
    final static double d = 0.85;
    /**
     * 最大迭代次数
     */
    final static int max_iter = 200;
    final static double min_diff = 0.001;
    /**
     * 文档句子的个数
     */
    int D;
    /**
     * 拆分为[句子[单词]]形式的文档
     */
    List<List<String>> docs;
    /**
     * 排序后的最终结果 score <-> index
     */
    TreeMap<Double, Integer> top;

    /**
     * 句子和其他句子的相关程度
     */
    double[][] weight;
    /**
     * 该句子和其他句子相关程度之和
     */
    double[] weight_sum;
    /**
     * 迭代之后收敛的权重// 最後的PageRanking值
     */
    double[] vertex;

    /**
     * BM25相似度
     */
    BM25 bm25;
    
              //计算BM25相关性矩阵
    public TextRankSentence(List<List<String>> docs)
    {
        this.docs = docs;
        bm25 = new BM25(docs);
        D = docs.size();
        weight = new double[D][D];
        weight_sum = new double[D];
        vertex = new double[D];
        top = new TreeMap<Double, Integer>(Collections.reverseOrder());//降序排列
        solve();
    }
    ///迭代投票
    private void solve()
    {
        int cnt = 0;
        for (List<String> sentence : docs)//把docs裡的List<String>放入sentence
        {
            double[] scores = bm25.simAll(sentence);
            /////////////////////////////////////////////印出經過bm25的計算後陣列數值
            //System.out.println(Arrays.toString(scores));
            //////////////////////////////////////////////
            weight[cnt] = scores;
            weight_sum[cnt] = sum(scores) - scores[cnt]; // 减掉自己，自己跟自己肯定最相似
            ///////////
            //System.out.println(weight_sum[cnt]);
            //////////
            vertex[cnt] = 1.0;
      
            ++cnt;
        }

        for (int _ = 0; _ < max_iter; ++_)
        {
            double[] m = new double[D];
            double max_diff = 0;
            for (int i = 0; i < D; ++i)
            {
                m[i] = 1 - d;
                for (int j = 0; j < D; ++j)
                {
                    if (j == i || weight_sum[j] == 0) continue;
                    m[i] += (d * weight[j][i] / weight_sum[j] * vertex[j]);//(由于是有权图，PageRank公式)
                }
                double diff = Math.abs(m[i] - vertex[i]);
                if (diff > max_diff)
                {
                    max_diff = diff;
                }
            }
            vertex = m;
            
            if (max_diff <= min_diff) break;
        }
        // 我们来排个序吧
        for (int i = 0; i < D; ++i)
        {
            top.put(vertex[i], i);

        }
        ///////////
        System.out.println(top.keySet());
        System.out.println(top.values());
        //////////
    }

    /**
     * 获取前几个关键句子
     *
     * @param size 要几个
     * @return 关键句子的下标
     */
    public int[] getTopSentence(int size)
    {
        Collection<Integer> values = top.values();//即找vertex[i]的 i 索引
        size = Math.min(size, values.size());
        int[] indexArray = new int[size];
        Iterator<Integer> it = values.iterator();
        for (int i = 0; i < size; ++i)
        {
            indexArray[i] = it.next();
        }
        return indexArray;
    }
    
    /////////////////////////////////////
    
    /**
     * 简单的求和
     *
     * @param array
     * @return
     */
    private static double sum(double[] array)
    {
        double total = 0;
        for (double v : array)
        {
            total += v;
        }
        return total;
    }
    

   
    //
    /**
     * 将文章分割为句子
     *
     * @param document
     * @return
     */
    static List<String> spiltSentence(String document)//斷句
    {
        List<String> sentences = new ArrayList<String>();
        for (String line : document.split("[\r\n]"))
        {
            line = line.trim();
            if (line.length() == 0) continue;
            for (String sent : line.split("[，,。:：“”？?！!；;]"))
            {
                sent = sent.trim();
                if (sent.length() == 0) continue;
                sentences.add(sent);
            }
        }

        return sentences;
    }
    ////////////////////////////////////////////////
    //分詞(Segment)在這裡做完成//////////////////////
    /**
     * 将句子列表转化为文档
     *
     * @param sentenceList
     * @return
     */
    
    private static List<List<String>> convertSentenceListToDocument(List<String> sentenceList)//分詞和過濾停用詞
    {
        List<List<String>> docs = new ArrayList<List<String>>(sentenceList.size());
        
        int num=1; double sum=0,sum1=0; 
        for (String sentence : sentenceList)//把sentenceList裡的句子依序丟進去sentence做處理
        {   
        	System.out.println('\n'+sentence);
        	//-------------------------------------------------------------------------------------------
            /*
            int pressure = 1000;
            StringBuilder sbBigText = new StringBuilder(sentence.length() * pressure);
            for (int i = 0; i < pressure; i++)
            {
                sbBigText.append(sentence);
            }
            String sentence_test= sbBigText.toString();
            System.gc();
            */
        	///////////////////////////////////////
        	//第一種分詞         	       	
        	//一段一段的sentence下去做分词，做完後 會把詞性 分析出來並  儲存到List<Term> termList裡面          
        	//////////////////////////////////////
            
            long start = System.currentTimeMillis(); 
            List<Term> termList=StandardTokenizer.segment(sentence);
            double costTime = (System.currentTimeMillis() - start)/ (double)1000 ;
            System.out.printf("StandTokenizer 分词速度 第 %d句：%.5f字每秒\n",num++, sentence.length()  / costTime/1000);
            System.gc();
             
        	sum+=costTime;
        	
        	//-----------------------------------------------------------------------------------------------------

        	
        	//---------------------------------------------------------------------------------------------------

        	      System.out.printf("StandTokenizer 分词時間:%.5f\n", costTime);        	        	      
                  System.out.printf("長度:%s\n",sentence.length());


            System.out.println("\n");
        	/////////////////////////////////////////////////////////////// 
            List<String> wordList = new LinkedList<String>();
            /////////////////////////////////////////////
            //List<Term> termList=StandardTokenizer.segment(sentence);
            ////////////////////////////////////////////
            for (Term term : termList)
            {   
            	System.out.print(term.word+"  ");
                if (CoreStopWordDictionary.shouldInclude(term))//過濾不要的詞，加入需要分析的詞進去wordList
                {
                    wordList.add(term.word);
                }
            }
            System.out.println(wordList);
            docs.add(wordList);
        }
        
    	if(Config.flagthread==true) {
    		System.out.println("[MultiThreading有啟動]");
    		}
    	if(Config.flagthread==false) {
    		System.out.println("[MultiThreading沒有啟動]");
    		}
        Config.flagthread=false;       
        
        System.out.printf("StandTokenizer 分词總時間:%.5f\n", sum); 
        return docs;
    }

    /**
     * 一句话调用接口
     *
     * @param document 目标文档
     * @param size     需要的关键句的个数
     * @return 关键句列表
     */
    
    //自動摘要主要使用的部分
    public static List<String> getTopSentenceList(String document, int size)
    {   
    	 //////////////////////////因為裡面coredictionary是簡體字典 所以先做轉換
	    String text_simplified = HanLP.convertToSimplifiedChinese(document);
	    ///////////////////////////////////////////////////
	    
        List<String> sentenceList = spiltSentence(text_simplified);//斷句(簡體字置入)
        List<List<String>> docs = convertSentenceListToDocument(sentenceList);//分詞和過濾停用詞
        TextRankSentence textRank = new TextRankSentence(docs);//计算BM25相关性矩阵
        
        int[] topSentence = textRank.getTopSentence(size);
        ////////////////////////////////////////////////////
        List<String> sentenceList_traditional=spiltSentence(document); //再做一次，抓原先繁體句子的index
        ////////////////////////////////////////////////////
        List<String> resultList = new LinkedList<String>();
        for (int i : topSentence)
        {   

        	resultList.add(sentenceList_traditional.get(i)+"        ");//get(i)取得句子的index
        }
        return resultList;
        
        
    }
    
    
  

}
